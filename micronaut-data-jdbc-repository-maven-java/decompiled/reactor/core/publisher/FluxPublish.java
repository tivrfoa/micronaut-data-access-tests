package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxPublish<T> extends ConnectableFlux<T> implements Scannable {
   final Flux<? extends T> source;
   final int prefetch;
   final Supplier<? extends Queue<T>> queueSupplier;
   volatile FluxPublish.PublishSubscriber<T> connection;
   static final AtomicReferenceFieldUpdater<FluxPublish, FluxPublish.PublishSubscriber> CONNECTION = AtomicReferenceFieldUpdater.newUpdater(
      FluxPublish.class, FluxPublish.PublishSubscriber.class, "connection"
   );

   FluxPublish(Flux<? extends T> source, int prefetch, Supplier<? extends Queue<T>> queueSupplier) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("bufferSize > 0 required but it was " + prefetch);
      } else {
         this.source = (Flux)Objects.requireNonNull(source, "source");
         this.prefetch = prefetch;
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
      }
   }

   @Override
   public void connect(Consumer<? super Disposable> cancelSupport) {
      FluxPublish.PublishSubscriber<T> s;
      while(true) {
         s = this.connection;
         if (s != null && !s.isTerminated()) {
            break;
         }

         FluxPublish.PublishSubscriber<T> u = new FluxPublish.PublishSubscriber<>(this.prefetch, this);
         if (CONNECTION.compareAndSet(this, s, u)) {
            s = u;
            break;
         }
      }

      boolean doConnect = s.tryConnect();
      cancelSupport.accept(s);
      if (doConnect) {
         this.source.subscribe(s);
      }

   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      FluxPublish.PublishInner<T> inner = new FluxPublish.PublishInner<>(actual);
      actual.onSubscribe(inner);

      while(!inner.isCancelled()) {
         FluxPublish.PublishSubscriber<T> c = this.connection;
         if (c == null || c.isTerminated()) {
            FluxPublish.PublishSubscriber<T> u = new FluxPublish.PublishSubscriber<>(this.prefetch, this);
            if (!CONNECTION.compareAndSet(this, c, u)) {
               continue;
            }

            c = u;
         }

         if (c.add(inner)) {
            if (inner.isCancelled()) {
               c.remove(inner);
            } else {
               inner.parent = c;
            }

            c.drain();
            break;
         }
      }

   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   abstract static class PubSubInner<T> implements InnerProducer<T> {
      final CoreSubscriber<? super T> actual;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxPublish.PubSubInner> REQUESTED = AtomicLongFieldUpdater.newUpdater(FluxPublish.PubSubInner.class, "requested");

      PubSubInner(CoreSubscriber<? super T> actual) {
         this.actual = actual;
      }

      @Override
      public final void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCapCancellable(REQUESTED, this, n);
            this.drainParent();
         }

      }

      @Override
      public final void cancel() {
         long r = this.requested;
         if (r != Long.MIN_VALUE) {
            r = REQUESTED.getAndSet(this, Long.MIN_VALUE);
            if (r != Long.MIN_VALUE) {
               this.removeAndDrainParent();
            }
         }

      }

      final boolean isCancelled() {
         return this.requested == Long.MIN_VALUE;
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.isCancelled();
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.isCancelled() ? 0L : this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      abstract void drainParent();

      abstract void removeAndDrainParent();
   }

   static final class PublishInner<T> extends FluxPublish.PubSubInner<T> {
      FluxPublish.PublishSubscriber<T> parent;

      PublishInner(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      void drainParent() {
         FluxPublish.PublishSubscriber<T> p = this.parent;
         if (p != null) {
            p.drain();
         }

      }

      @Override
      void removeAndDrainParent() {
         FluxPublish.PublishSubscriber<T> p = this.parent;
         if (p != null) {
            p.remove(this);
            p.drain();
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key != Scannable.Attr.TERMINATED) {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         } else {
            return this.parent != null && this.parent.isTerminated();
         }
      }
   }

   static final class PublishSubscriber<T> implements InnerConsumer<T>, Disposable {
      final int prefetch;
      final FluxPublish<T> parent;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxPublish.PublishSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxPublish.PublishSubscriber.class, Subscription.class, "s"
      );
      volatile FluxPublish.PubSubInner<T>[] subscribers;
      static final AtomicReferenceFieldUpdater<FluxPublish.PublishSubscriber, FluxPublish.PubSubInner[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
         FluxPublish.PublishSubscriber.class, FluxPublish.PubSubInner[].class, "subscribers"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxPublish.PublishSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxPublish.PublishSubscriber.class, "wip"
      );
      volatile int connected;
      static final AtomicIntegerFieldUpdater<FluxPublish.PublishSubscriber> CONNECTED = AtomicIntegerFieldUpdater.newUpdater(
         FluxPublish.PublishSubscriber.class, "connected"
      );
      static final FluxPublish.PubSubInner[] INIT = new FluxPublish.PublishInner[0];
      static final FluxPublish.PubSubInner[] CANCELLED = new FluxPublish.PublishInner[0];
      static final FluxPublish.PubSubInner[] TERMINATED = new FluxPublish.PublishInner[0];
      volatile Queue<T> queue;
      int sourceMode;
      volatile boolean done;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxPublish.PublishSubscriber, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxPublish.PublishSubscriber.class, Throwable.class, "error"
      );

      PublishSubscriber(int prefetch, FluxPublish<T> parent) {
         this.prefetch = prefetch;
         this.parent = parent;
         SUBSCRIBERS.lazySet(this, INIT);
      }

      boolean isTerminated() {
         return this.subscribers == TERMINATED;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> f = (Fuseable.QueueSubscription)s;
               int m = f.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = m;
                  this.queue = f;
                  this.drain();
                  return;
               }

               if (m == 2) {
                  this.sourceMode = m;
                  this.queue = f;
                  s.request(Operators.unboundedOrPrefetch(this.prefetch));
                  return;
               }
            }

            this.queue = (Queue)this.parent.queueSupplier.get();
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            if (t != null) {
               Operators.onNextDropped(t, this.currentContext());
            }

         } else if (this.sourceMode == 2) {
            this.drain();
         } else {
            if (!this.queue.offer(t)) {
               Throwable ex = Operators.onOperatorError(
                  this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.currentContext()
               );
               if (!Exceptions.addThrowable(ERROR, this, ex)) {
                  Operators.onErrorDroppedMulticast(ex, this.subscribers);
                  return;
               }

               this.done = true;
            }

            this.drain();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDroppedMulticast(t, this.subscribers);
         } else {
            if (Exceptions.addThrowable(ERROR, this, t)) {
               this.done = true;
               this.drain();
            } else {
               Operators.onErrorDroppedMulticast(t, this.subscribers);
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.drain();
         }
      }

      @Override
      public void dispose() {
         if (SUBSCRIBERS.get(this) != TERMINATED) {
            if (FluxPublish.CONNECTION.compareAndSet(this.parent, this, null)) {
               Operators.terminate(S, this);
               if (WIP.getAndIncrement(this) != 0) {
                  return;
               }

               this.disconnectAction();
            }

         }
      }

      void disconnectAction() {
         FluxPublish.PubSubInner<T>[] inners = (FluxPublish.PubSubInner[])SUBSCRIBERS.getAndSet(this, CANCELLED);
         if (inners.length > 0) {
            this.queue.clear();
            CancellationException ex = new CancellationException("Disconnected");

            for(FluxPublish.PubSubInner<T> inner : inners) {
               inner.actual.onError(ex);
            }
         }

      }

      boolean add(FluxPublish.PublishInner<T> inner) {
         FluxPublish.PubSubInner<T>[] a;
         FluxPublish.PubSubInner<?>[] b;
         do {
            a = this.subscribers;
            if (a == TERMINATED) {
               return false;
            }

            int n = a.length;
            b = new FluxPublish.PubSubInner[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = inner;
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

         return true;
      }

      public void remove(FluxPublish.PubSubInner<T> inner) {
         while(true) {
            FluxPublish.PubSubInner<T>[] a = this.subscribers;
            if (a != TERMINATED && a != CANCELLED) {
               int n = a.length;
               int j = -1;

               for(int i = 0; i < n; ++i) {
                  if (a[i] == inner) {
                     j = i;
                     break;
                  }
               }

               if (j < 0) {
                  return;
               }

               FluxPublish.PubSubInner<?>[] b;
               if (n == 1) {
                  b = CANCELLED;
               } else {
                  b = new FluxPublish.PubSubInner[n - 1];
                  System.arraycopy(a, 0, b, 0, j);
                  System.arraycopy(a, j + 1, b, j, n - j - 1);
               }

               if (!SUBSCRIBERS.compareAndSet(this, a, b)) {
                  continue;
               }

               return;
            }

            return;
         }
      }

      FluxPublish.PubSubInner<T>[] terminate() {
         return (FluxPublish.PubSubInner<T>[])SUBSCRIBERS.getAndSet(this, TERMINATED);
      }

      boolean tryConnect() {
         return this.connected == 0 && CONNECTED.compareAndSet(this, 0, 1);
      }

      final void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;

            while(true) {
               boolean d = this.done;
               Queue<T> q = this.queue;
               boolean empty = q == null || q.isEmpty();
               if (this.checkTerminated(d, empty)) {
                  return;
               }

               FluxPublish.PubSubInner<T>[] a = this.subscribers;
               if (a != CANCELLED && !empty) {
                  long maxRequested = Long.MAX_VALUE;
                  int len = a.length;
                  int cancel = 0;

                  for(FluxPublish.PubSubInner<T> inner : a) {
                     long r = inner.requested;
                     if (r >= 0L) {
                        maxRequested = Math.min(maxRequested, r);
                     } else {
                        ++cancel;
                     }
                  }

                  if (len == cancel) {
                     T v;
                     try {
                        v = (T)q.poll();
                     } catch (Throwable var17) {
                        Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this.s, var17, this.currentContext()));
                        d = true;
                        v = null;
                     }

                     if (this.checkTerminated(d, v == null)) {
                        return;
                     }

                     if (this.sourceMode != 1) {
                        this.s.request(1L);
                     }
                     continue;
                  }

                  int e;
                  for(e = 0; (long)e < maxRequested && cancel != Integer.MIN_VALUE; ++e) {
                     d = this.done;

                     T v;
                     try {
                        v = (T)q.poll();
                     } catch (Throwable var16) {
                        Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this.s, var16, this.currentContext()));
                        d = true;
                        v = null;
                     }

                     empty = v == null;
                     if (this.checkTerminated(d, empty)) {
                        return;
                     }

                     if (empty) {
                        if (this.sourceMode == 1) {
                           this.done = true;
                           this.checkTerminated(true, true);
                        }
                        break;
                     }

                     for(FluxPublish.PubSubInner<T> inner : a) {
                        inner.actual.onNext(v);
                        if (Operators.producedCancellable(FluxPublish.PubSubInner.REQUESTED, inner, 1L) == Long.MIN_VALUE) {
                           cancel = Integer.MIN_VALUE;
                        }
                     }
                  }

                  if (e != 0 && this.sourceMode != 1) {
                     this.s.request((long)e);
                  }

                  if (maxRequested != 0L && !empty) {
                     continue;
                  }
               } else if (this.sourceMode == 1) {
                  this.done = true;
                  if (this.checkTerminated(true, empty)) {
                     break;
                  }
               }

               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  break;
               }
            }

         }
      }

      boolean checkTerminated(boolean d, boolean empty) {
         if (this.s == Operators.cancelledSubscription()) {
            this.disconnectAction();
            return true;
         } else {
            if (d) {
               Throwable e = this.error;
               if (e != null && e != Exceptions.TERMINATED) {
                  FluxPublish.CONNECTION.compareAndSet(this.parent, this, null);
                  e = Exceptions.terminate(ERROR, this);
                  this.queue.clear();

                  for(FluxPublish.PubSubInner<T> inner : this.terminate()) {
                     inner.actual.onError(e);
                  }

                  return true;
               }

               if (empty) {
                  FluxPublish.CONNECTION.compareAndSet(this.parent, this, null);

                  for(FluxPublish.PubSubInner<T> inner : this.terminate()) {
                     inner.actual.onComplete();
                  }

                  return true;
               }
            }

            return false;
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Override
      public Context currentContext() {
         return Operators.multiSubscribersContext(this.subscribers);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.isTerminated();
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public boolean isDisposed() {
         return this.s == Operators.cancelledSubscription() || this.done;
      }
   }
}
