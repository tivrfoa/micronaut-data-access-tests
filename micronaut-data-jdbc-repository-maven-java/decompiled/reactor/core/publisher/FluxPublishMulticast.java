package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxPublishMulticast<T, R> extends InternalFluxOperator<T, R> implements Fuseable {
   final Function<? super Flux<T>, ? extends Publisher<? extends R>> transform;
   final Supplier<? extends Queue<T>> queueSupplier;
   final int prefetch;

   FluxPublishMulticast(
      Flux<? extends T> source, Function<? super Flux<T>, ? extends Publisher<? extends R>> transform, int prefetch, Supplier<? extends Queue<T>> queueSupplier
   ) {
      super(source);
      if (prefetch < 1) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.prefetch = prefetch;
         this.transform = (Function)Objects.requireNonNull(transform, "transform");
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      FluxPublishMulticast.FluxPublishMulticaster<T> multicast = new FluxPublishMulticast.FluxPublishMulticaster<>(
         this.prefetch, this.queueSupplier, actual.currentContext()
      );
      Publisher<? extends R> out = (Publisher)Objects.requireNonNull(this.transform.apply(multicast), "The transform returned a null Publisher");
      if (out instanceof Fuseable) {
         out.subscribe(new FluxPublishMulticast.CancelFuseableMulticaster<>(actual, multicast));
      } else {
         out.subscribe(new FluxPublishMulticast.CancelMulticaster<>(actual, multicast));
      }

      return multicast;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class CancelFuseableMulticaster<T> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final FluxPublishMulticast.PublishMulticasterParent parent;
      Fuseable.QueueSubscription<T> s;

      CancelFuseableMulticaster(CoreSubscriber<? super T> actual, FluxPublishMulticast.PublishMulticasterParent parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
         this.parent.terminate();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = Operators.as(s);
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
         this.parent.terminate();
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
         this.parent.terminate();
      }

      @Override
      public int requestFusion(int requestedMode) {
         return this.s.requestFusion(requestedMode);
      }

      @Nullable
      public T poll() {
         return (T)this.s.poll();
      }

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public int size() {
         return this.s.size();
      }

      public void clear() {
         this.s.clear();
      }
   }

   static final class CancelMulticaster<T> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final FluxPublishMulticast.PublishMulticasterParent parent;
      Subscription s;

      CancelMulticaster(CoreSubscriber<? super T> actual, FluxPublishMulticast.PublishMulticasterParent parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
         this.parent.terminate();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
         this.parent.terminate();
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
         this.parent.terminate();
      }

      @Override
      public int requestFusion(int requestedMode) {
         return 0;
      }

      public void clear() {
      }

      public boolean isEmpty() {
         return false;
      }

      public int size() {
         return 0;
      }

      @Nullable
      public T poll() {
         return null;
      }
   }

   static final class FluxPublishMulticaster<T> extends Flux<T> implements InnerConsumer<T>, FluxPublishMulticast.PublishMulticasterParent {
      final int limit;
      final int prefetch;
      final Supplier<? extends Queue<T>> queueSupplier;
      Queue<T> queue;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxPublishMulticast.FluxPublishMulticaster, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxPublishMulticast.FluxPublishMulticaster.class, Subscription.class, "s"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxPublishMulticast.FluxPublishMulticaster> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxPublishMulticast.FluxPublishMulticaster.class, "wip"
      );
      volatile FluxPublishMulticast.PublishMulticastInner<T>[] subscribers;
      static final AtomicReferenceFieldUpdater<FluxPublishMulticast.FluxPublishMulticaster, FluxPublishMulticast.PublishMulticastInner[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
         FluxPublishMulticast.FluxPublishMulticaster.class, FluxPublishMulticast.PublishMulticastInner[].class, "subscribers"
      );
      static final FluxPublishMulticast.PublishMulticastInner[] EMPTY = new FluxPublishMulticast.PublishMulticastInner[0];
      static final FluxPublishMulticast.PublishMulticastInner[] TERMINATED = new FluxPublishMulticast.PublishMulticastInner[0];
      volatile boolean done;
      volatile boolean connected;
      Throwable error;
      final Context context;
      int produced;
      int sourceMode;

      FluxPublishMulticaster(int prefetch, Supplier<? extends Queue<T>> queueSupplier, Context ctx) {
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
         this.queueSupplier = queueSupplier;
         SUBSCRIBERS.lazySet(this, EMPTY);
         this.context = ctx;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Override
      public Context currentContext() {
         return this.context;
      }

      @Override
      public void subscribe(CoreSubscriber<? super T> actual) {
         FluxPublishMulticast.PublishMulticastInner<T> pcs = new FluxPublishMulticast.PublishMulticastInner<>(this, actual);
         actual.onSubscribe(pcs);
         if (this.add(pcs)) {
            if (pcs.requested == Long.MIN_VALUE) {
               this.remove(pcs);
               return;
            }

            this.drain();
         } else {
            Throwable ex = this.error;
            if (ex != null) {
               actual.onError(ex);
            } else {
               actual.onComplete();
            }
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> qs = (Fuseable.QueueSubscription)s;
               int m = qs.requestFusion(3);
               if (m == 1) {
                  this.sourceMode = m;
                  this.queue = qs;
                  this.done = true;
                  this.connected = true;
                  this.drain();
                  return;
               }

               if (m == 2) {
                  this.sourceMode = m;
                  this.queue = qs;
                  this.connected = true;
                  s.request(Operators.unboundedOrPrefetch(this.prefetch));
                  return;
               }
            }

            this.queue = (Queue)this.queueSupplier.get();
            this.connected = true;
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.context);
         } else if (this.sourceMode != 2 && !this.queue.offer(t)) {
            this.onError(
               Operators.onOperatorError(
                  this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.context
               )
            );
         } else {
            this.drain();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.context);
         } else {
            this.error = t;
            this.done = true;
            this.drain();
         }
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            if (this.sourceMode == 1) {
               this.drainSync();
            } else {
               this.drainAsync();
            }

         }
      }

      void drainSync() {
         int missed = 1;

         do {
            if (this.connected) {
               if (this.s == Operators.cancelledSubscription()) {
                  this.queue.clear();
                  return;
               }

               Queue<T> queue = this.queue;
               FluxPublishMulticast.PublishMulticastInner<T>[] a = this.subscribers;
               int n = a.length;
               if (n != 0) {
                  long r = Long.MAX_VALUE;

                  for(int i = 0; i < n; ++i) {
                     long u = a[i].requested;
                     if (u != Long.MIN_VALUE) {
                        r = Math.min(r, u);
                     }
                  }

                  long e;
                  for(e = 0L; e != r; ++e) {
                     if (this.s == Operators.cancelledSubscription()) {
                        queue.clear();
                        return;
                     }

                     T v;
                     try {
                        v = (T)queue.poll();
                     } catch (Throwable var14) {
                        Throwable ex = var14;
                        this.error = Operators.onOperatorError(this.s, var14, this.context);
                        queue.clear();
                        a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                        n = a.length;

                        for(int i = 0; i < n; ++i) {
                           a[i].actual.onError(ex);
                        }

                        return;
                     }

                     if (v == null) {
                        a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                        n = a.length;

                        for(int i = 0; i < n; ++i) {
                           a[i].actual.onComplete();
                        }

                        return;
                     }

                     for(int i = 0; i < n; ++i) {
                        a[i].actual.onNext(v);
                     }
                  }

                  if (this.s == Operators.cancelledSubscription()) {
                     queue.clear();
                     return;
                  }

                  if (queue.isEmpty()) {
                     a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                     n = a.length;

                     for(int i = 0; i < n; ++i) {
                        a[i].actual.onComplete();
                     }

                     return;
                  }

                  if (e != 0L) {
                     for(int i = 0; i < n; ++i) {
                        a[i].produced(e);
                     }
                  }
               }
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      void drainAsync() {
         int missed = 1;
         int p = this.produced;

         do {
            if (this.connected) {
               if (this.s == Operators.cancelledSubscription()) {
                  this.queue.clear();
                  return;
               }

               Queue<T> queue = this.queue;
               FluxPublishMulticast.PublishMulticastInner<T>[] a = this.subscribers;
               int n = a.length;
               if (n != 0) {
                  long r = Long.MAX_VALUE;

                  for(int i = 0; i < n; ++i) {
                     long u = a[i].requested;
                     if (u != Long.MIN_VALUE) {
                        r = Math.min(r, u);
                     }
                  }

                  long e = 0L;

                  while(e != r) {
                     if (this.s == Operators.cancelledSubscription()) {
                        queue.clear();
                        return;
                     }

                     boolean d = this.done;

                     T v;
                     try {
                        v = (T)queue.poll();
                     } catch (Throwable var17) {
                        Throwable ex = var17;
                        queue.clear();
                        this.error = Operators.onOperatorError(this.s, var17, this.context);
                        a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                        n = a.length;

                        for(int i = 0; i < n; ++i) {
                           a[i].actual.onError(ex);
                        }

                        return;
                     }

                     boolean empty = v == null;
                     if (d) {
                        Throwable ex = this.error;
                        if (ex != null) {
                           queue.clear();
                           a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                           n = a.length;

                           for(int i = 0; i < n; ++i) {
                              a[i].actual.onError(ex);
                           }

                           return;
                        }

                        if (empty) {
                           a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                           n = a.length;

                           for(int i = 0; i < n; ++i) {
                              a[i].actual.onComplete();
                           }

                           return;
                        }
                     }

                     if (empty) {
                        break;
                     }

                     for(int i = 0; i < n; ++i) {
                        a[i].actual.onNext(v);
                     }

                     ++e;
                     if (++p == this.limit) {
                        this.s.request((long)p);
                        p = 0;
                     }
                  }

                  if (e == r) {
                     if (this.s == Operators.cancelledSubscription()) {
                        queue.clear();
                        return;
                     }

                     boolean d = this.done;
                     if (d) {
                        Throwable ex = this.error;
                        if (ex != null) {
                           queue.clear();
                           a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                           n = a.length;

                           for(int i = 0; i < n; ++i) {
                              a[i].actual.onError(ex);
                           }

                           return;
                        }

                        if (queue.isEmpty()) {
                           a = (FluxPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
                           n = a.length;

                           for(int i = 0; i < n; ++i) {
                              a[i].actual.onComplete();
                           }

                           return;
                        }
                     }
                  }

                  if (e != 0L) {
                     for(int i = 0; i < n; ++i) {
                        a[i].produced(e);
                     }
                  }
               }
            }

            this.produced = p;
            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      boolean add(FluxPublishMulticast.PublishMulticastInner<T> s) {
         FluxPublishMulticast.PublishMulticastInner<T>[] a;
         FluxPublishMulticast.PublishMulticastInner<T>[] b;
         do {
            a = this.subscribers;
            if (a == TERMINATED) {
               return false;
            }

            int n = a.length;
            b = new FluxPublishMulticast.PublishMulticastInner[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = s;
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

         return true;
      }

      void remove(FluxPublishMulticast.PublishMulticastInner<T> s) {
         while(true) {
            FluxPublishMulticast.PublishMulticastInner<T>[] a = this.subscribers;
            if (a != TERMINATED && a != EMPTY) {
               int n = a.length;
               int j = -1;

               for(int i = 0; i < n; ++i) {
                  if (a[i] == s) {
                     j = i;
                     break;
                  }
               }

               if (j < 0) {
                  return;
               }

               FluxPublishMulticast.PublishMulticastInner<T>[] b;
               if (n == 1) {
                  b = EMPTY;
               } else {
                  b = new FluxPublishMulticast.PublishMulticastInner[n - 1];
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

      @Override
      public void terminate() {
         Operators.terminate(S, this);
         if (WIP.getAndIncrement(this) == 0 && this.connected) {
            this.queue.clear();
         }

      }
   }

   static final class PublishMulticastInner<T> implements InnerProducer<T> {
      final FluxPublishMulticast.FluxPublishMulticaster<T> parent;
      final CoreSubscriber<? super T> actual;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxPublishMulticast.PublishMulticastInner> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxPublishMulticast.PublishMulticastInner.class, "requested"
      );

      PublishMulticastInner(FluxPublishMulticast.FluxPublishMulticaster<T> parent, CoreSubscriber<? super T> actual) {
         this.parent = parent;
         this.actual = actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return Math.max(0L, this.requested);
         } else if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return Long.MIN_VALUE == this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCapCancellable(REQUESTED, this, n);
            this.parent.drain();
         }

      }

      @Override
      public void cancel() {
         if (REQUESTED.getAndSet(this, Long.MIN_VALUE) != Long.MIN_VALUE) {
            this.parent.remove(this);
            this.parent.drain();
         }

      }

      void produced(long n) {
         Operators.producedCancellable(REQUESTED, this, n);
      }
   }

   interface PublishMulticasterParent {
      void terminate();
   }
}
