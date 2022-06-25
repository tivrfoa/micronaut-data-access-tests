package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

@Deprecated
public final class EmitterProcessor<T> extends FluxProcessor<T, T> implements InternalManySink<T> {
   static final FluxPublish.PubSubInner[] EMPTY = new FluxPublish.PublishInner[0];
   final int prefetch;
   final boolean autoCancel;
   volatile Subscription s;
   static final AtomicReferenceFieldUpdater<EmitterProcessor, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
      EmitterProcessor.class, Subscription.class, "s"
   );
   volatile FluxPublish.PubSubInner<T>[] subscribers;
   static final AtomicReferenceFieldUpdater<EmitterProcessor, FluxPublish.PubSubInner[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
      EmitterProcessor.class, FluxPublish.PubSubInner[].class, "subscribers"
   );
   volatile int wip;
   static final AtomicIntegerFieldUpdater<EmitterProcessor> WIP = AtomicIntegerFieldUpdater.newUpdater(EmitterProcessor.class, "wip");
   volatile Queue<T> queue;
   int sourceMode;
   volatile boolean done;
   volatile Throwable error;
   static final AtomicReferenceFieldUpdater<EmitterProcessor, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
      EmitterProcessor.class, Throwable.class, "error"
   );

   @Deprecated
   public static <E> EmitterProcessor<E> create() {
      return create(Queues.SMALL_BUFFER_SIZE, true);
   }

   @Deprecated
   public static <E> EmitterProcessor<E> create(boolean autoCancel) {
      return create(Queues.SMALL_BUFFER_SIZE, autoCancel);
   }

   @Deprecated
   public static <E> EmitterProcessor<E> create(int bufferSize) {
      return create(bufferSize, true);
   }

   @Deprecated
   public static <E> EmitterProcessor<E> create(int bufferSize, boolean autoCancel) {
      return new EmitterProcessor<>(autoCancel, bufferSize);
   }

   EmitterProcessor(boolean autoCancel, int prefetch) {
      if (prefetch < 1) {
         throw new IllegalArgumentException("bufferSize must be strictly positive, was: " + prefetch);
      } else {
         this.autoCancel = autoCancel;
         this.prefetch = prefetch;
         SUBSCRIBERS.lazySet(this, EMPTY);
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

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Objects.requireNonNull(actual, "subscribe");
      EmitterProcessor.EmitterInner<T> inner = new EmitterProcessor.EmitterInner<>(actual, this);
      actual.onSubscribe(inner);
      if (!inner.isCancelled()) {
         if (this.add(inner)) {
            if (inner.isCancelled()) {
               this.remove(inner);
            }

            this.drain();
         } else {
            Throwable e = this.error;
            if (e != null) {
               inner.actual.onError(e);
            } else {
               inner.actual.onComplete();
            }
         }

      }
   }

   @Override
   public void onComplete() {
      Sinks.EmitResult emitResult = this.tryEmitComplete();
   }

   @Override
   public Sinks.EmitResult tryEmitComplete() {
      if (this.done) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         this.done = true;
         this.drain();
         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public void onError(Throwable throwable) {
      this.emitError(throwable, Sinks.EmitFailureHandler.FAIL_FAST);
   }

   @Override
   public Sinks.EmitResult tryEmitError(Throwable t) {
      Objects.requireNonNull(t, "onError");
      if (this.done) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else if (Exceptions.addThrowable(ERROR, this, t)) {
         this.done = true;
         this.drain();
         return Sinks.EmitResult.OK;
      } else {
         return Sinks.EmitResult.FAIL_TERMINATED;
      }
   }

   @Override
   public void onNext(T t) {
      if (this.sourceMode == 2) {
         this.drain();
      } else {
         this.emitNext(t, Sinks.EmitFailureHandler.FAIL_FAST);
      }
   }

   @Override
   public Sinks.EmitResult tryEmitNext(T t) {
      if (this.done) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         Objects.requireNonNull(t, "onNext");
         Queue<T> q = this.queue;
         if (q == null) {
            if (Operators.setOnce(S, this, Operators.emptySubscription())) {
               q = (Queue)Queues.get(this.prefetch).get();
               this.queue = q;
            } else {
               do {
                  if (this.isCancelled()) {
                     return Sinks.EmitResult.FAIL_CANCELLED;
                  }

                  q = this.queue;
               } while(q == null);
            }
         }

         if (!q.offer(t)) {
            return this.subscribers == EMPTY ? Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER : Sinks.EmitResult.FAIL_OVERFLOW;
         } else {
            this.drain();
            return Sinks.EmitResult.OK;
         }
      }
   }

   @Override
   public int currentSubscriberCount() {
      return this.subscribers.length;
   }

   @Override
   public Flux<T> asFlux() {
      return this;
   }

   @Override
   protected boolean isIdentityProcessor() {
      return true;
   }

   public int getPending() {
      Queue<T> q = this.queue;
      return q != null ? q.size() : 0;
   }

   @Override
   public boolean isDisposed() {
      return this.isTerminated() || this.isCancelled();
   }

   @Override
   public void onSubscribe(Subscription s) {
      if (Operators.setOnce(S, this, s)) {
         if (s instanceof Fuseable.QueueSubscription) {
            Fuseable.QueueSubscription<T> f = (Fuseable.QueueSubscription)s;
            int m = f.requestFusion(3);
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

         this.queue = (Queue)Queues.get(this.prefetch).get();
         s.request(Operators.unboundedOrPrefetch(this.prefetch));
      }

   }

   @Nullable
   @Override
   public Throwable getError() {
      return this.error;
   }

   public boolean isCancelled() {
      return Operators.cancelledSubscription() == this.s;
   }

   @Override
   public final int getBufferSize() {
      return this.prefetch;
   }

   @Override
   public boolean isTerminated() {
      return this.done && this.getPending() == 0;
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.s;
      } else if (key == Scannable.Attr.BUFFERED) {
         return this.getPending();
      } else if (key == Scannable.Attr.CANCELLED) {
         return this.isCancelled();
      } else {
         return key == Scannable.Attr.PREFETCH ? this.getPrefetch() : super.scanUnsafe(key);
      }
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
            if (a != EMPTY && !empty) {
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
                     if (Operators.producedCancellable(FluxPublish.PublishInner.REQUESTED, inner, 1L) == Long.MIN_VALUE) {
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

   FluxPublish.PubSubInner<T>[] terminate() {
      return (FluxPublish.PubSubInner<T>[])SUBSCRIBERS.getAndSet(this, FluxPublish.PublishSubscriber.TERMINATED);
   }

   boolean checkTerminated(boolean d, boolean empty) {
      if (this.s == Operators.cancelledSubscription()) {
         if (this.autoCancel) {
            this.terminate();
            Queue<T> q = this.queue;
            if (q != null) {
               q.clear();
            }
         }

         return true;
      } else {
         if (d) {
            Throwable e = this.error;
            if (e != null && e != Exceptions.TERMINATED) {
               Queue<T> q = this.queue;
               if (q != null) {
                  q.clear();
               }

               for(FluxPublish.PubSubInner<T> inner : this.terminate()) {
                  inner.actual.onError(e);
               }

               return true;
            }

            if (empty) {
               for(FluxPublish.PubSubInner<T> inner : this.terminate()) {
                  inner.actual.onComplete();
               }

               return true;
            }
         }

         return false;
      }
   }

   final boolean add(EmitterProcessor.EmitterInner<T> inner) {
      FluxPublish.PubSubInner<T>[] a;
      FluxPublish.PubSubInner<?>[] b;
      do {
         a = this.subscribers;
         if (a == FluxPublish.PublishSubscriber.TERMINATED) {
            return false;
         }

         int n = a.length;
         b = new FluxPublish.PubSubInner[n + 1];
         System.arraycopy(a, 0, b, 0, n);
         b[n] = inner;
      } while(!SUBSCRIBERS.compareAndSet(this, a, b));

      return true;
   }

   final void remove(FluxPublish.PubSubInner<T> inner) {
      while(true) {
         FluxPublish.PubSubInner<T>[] a = this.subscribers;
         if (a != FluxPublish.PublishSubscriber.TERMINATED && a != EMPTY) {
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
               b = EMPTY;
            } else {
               b = new FluxPublish.PubSubInner[n - 1];
               System.arraycopy(a, 0, b, 0, j);
               System.arraycopy(a, j + 1, b, j, n - j - 1);
            }

            if (!SUBSCRIBERS.compareAndSet(this, a, b)) {
               continue;
            }

            if (this.autoCancel && b == EMPTY && Operators.terminate(S, this)) {
               if (WIP.getAndIncrement(this) != 0) {
                  return;
               }

               this.terminate();
               Queue<T> q = this.queue;
               if (q != null) {
                  q.clear();
               }
            }

            return;
         }

         return;
      }
   }

   @Override
   public long downstreamCount() {
      return (long)this.subscribers.length;
   }

   static final class EmitterInner<T> extends FluxPublish.PubSubInner<T> {
      final EmitterProcessor<T> parent;

      EmitterInner(CoreSubscriber<? super T> actual, EmitterProcessor<T> parent) {
         super(actual);
         this.parent = parent;
      }

      @Override
      void drainParent() {
         this.parent.drain();
      }

      @Override
      void removeAndDrainParent() {
         this.parent.remove(this);
         this.parent.drain();
      }
   }
}
