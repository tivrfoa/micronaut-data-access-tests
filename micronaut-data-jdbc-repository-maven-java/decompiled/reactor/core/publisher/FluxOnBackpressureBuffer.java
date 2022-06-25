package reactor.core.publisher;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class FluxOnBackpressureBuffer<O> extends InternalFluxOperator<O, O> implements Fuseable {
   final Consumer<? super O> onOverflow;
   final int bufferSize;
   final boolean unbounded;

   FluxOnBackpressureBuffer(Flux<? extends O> source, int bufferSize, boolean unbounded, @Nullable Consumer<? super O> onOverflow) {
      super(source);
      if (bufferSize < 1) {
         throw new IllegalArgumentException("Buffer Size must be strictly positive");
      } else {
         this.bufferSize = bufferSize;
         this.unbounded = unbounded;
         this.onOverflow = onOverflow;
      }
   }

   @Override
   public CoreSubscriber<? super O> subscribeOrReturn(CoreSubscriber<? super O> actual) {
      return new FluxOnBackpressureBuffer.BackpressureBufferSubscriber<>(actual, this.bufferSize, this.unbounded, this.onOverflow);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   static final class BackpressureBufferSubscriber<T> implements Fuseable.QueueSubscription<T>, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final Queue<T> queue;
      final int capacityOrSkip;
      final Consumer<? super T> onOverflow;
      Subscription s;
      volatile boolean cancelled;
      volatile boolean enabledFusion;
      volatile boolean done;
      Throwable error;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxOnBackpressureBuffer.BackpressureBufferSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxOnBackpressureBuffer.BackpressureBufferSubscriber.class, "wip"
      );
      volatile int discardGuard;
      static final AtomicIntegerFieldUpdater<FluxOnBackpressureBuffer.BackpressureBufferSubscriber> DISCARD_GUARD = AtomicIntegerFieldUpdater.newUpdater(
         FluxOnBackpressureBuffer.BackpressureBufferSubscriber.class, "discardGuard"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxOnBackpressureBuffer.BackpressureBufferSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxOnBackpressureBuffer.BackpressureBufferSubscriber.class, "requested"
      );

      BackpressureBufferSubscriber(CoreSubscriber<? super T> actual, int bufferSize, boolean unbounded, @Nullable Consumer<? super T> onOverflow) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.onOverflow = onOverflow;
         Queue<T> q;
         if (unbounded) {
            q = (Queue)Queues.unbounded(bufferSize).get();
         } else {
            q = (Queue)Queues.get(bufferSize).get();
         }

         if (!unbounded && Queues.capacity(q) > bufferSize) {
            this.capacityOrSkip = bufferSize;
         } else {
            this.capacityOrSkip = Integer.MAX_VALUE;
         }

         this.queue = q;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.CANCELLED) {
               return this.cancelled;
            } else if (key == Scannable.Attr.BUFFERED) {
               return this.queue.size();
            } else if (key == Scannable.Attr.ERROR) {
               return this.error;
            } else if (key == Scannable.Attr.PREFETCH) {
               return Integer.MAX_VALUE;
            } else if (key == Scannable.Attr.DELAY_ERROR) {
               return true;
            } else if (key == Scannable.Attr.CAPACITY) {
               return this.capacityOrSkip == Integer.MAX_VALUE ? Queues.capacity(this.queue) : this.capacityOrSkip;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return this.done && this.queue.isEmpty();
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.ctx);
         } else {
            if (this.cancelled) {
               Operators.onDiscard(t, this.ctx);
            }

            if ((this.capacityOrSkip == Integer.MAX_VALUE || this.queue.size() < this.capacityOrSkip) && this.queue.offer(t)) {
               this.drain(t);
            } else {
               Throwable ex = Operators.onOperatorError(this.s, Exceptions.failWithOverflow(), t, this.ctx);
               if (this.onOverflow != null) {
                  try {
                     this.onOverflow.accept(t);
                  } catch (Throwable var4) {
                     Exceptions.throwIfFatal(var4);
                     ex.initCause(var4);
                  }
               }

               Operators.onDiscard(t, this.ctx);
               this.onError(ex);
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.error = t;
            this.done = true;
            this.drain((T)null);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.drain((T)null);
         }
      }

      void drain(@Nullable T dataSignal) {
         if (WIP.getAndIncrement(this) != 0) {
            if (dataSignal != null && this.cancelled) {
               Operators.onDiscard(dataSignal, this.actual.currentContext());
            }

         } else {
            int missed = 1;

            do {
               Subscriber<? super T> a = this.actual;
               if (a != null) {
                  if (this.enabledFusion) {
                     this.drainFused(a);
                  } else {
                     this.drainRegular(a);
                  }

                  return;
               }

               missed = WIP.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }

      void drainRegular(Subscriber<? super T> a) {
         int missed = 1;
         Queue<T> q = this.queue;

         do {
            long r = this.requested;

            long e;
            for(e = 0L; r != e; ++e) {
               boolean d = this.done;
               T t = (T)q.poll();
               boolean empty = t == null;
               if (this.checkTerminated(d, empty, a, t)) {
                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(t);
            }

            if (r == e && this.checkTerminated(this.done, q.isEmpty(), a, (T)null)) {
               return;
            }

            if (e != 0L && r != Long.MAX_VALUE) {
               REQUESTED.addAndGet(this, -e);
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      void drainFused(Subscriber<? super T> a) {
         int missed = 1;
         Queue<T> q = this.queue;

         while(!this.cancelled) {
            boolean d = this.done;
            a.onNext((T)null);
            if (d) {
               Throwable ex = this.error;
               if (ex != null) {
                  a.onError(ex);
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         this.clear();
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain((T)null);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            if (WIP.getAndIncrement(this) == 0 && !this.enabledFusion) {
               Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
            }
         }

      }

      @Nullable
      public T poll() {
         return (T)this.queue.poll();
      }

      public int size() {
         return this.queue.size();
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      public void clear() {
         if (DISCARD_GUARD.getAndIncrement(this) == 0) {
            int missed = 1;

            while(true) {
               Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
               int dg = this.discardGuard;
               if (missed == dg) {
                  missed = DISCARD_GUARD.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               } else {
                  missed = dg;
               }
            }
         }
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0) {
            this.enabledFusion = true;
            return 2;
         } else {
            return 0;
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a, @Nullable T v) {
         if (this.cancelled) {
            this.s.cancel();
            Operators.onDiscard(v, this.ctx);
            Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
            return true;
         } else if (d && empty) {
            Throwable e = this.error;
            if (e != null) {
               a.onError(e);
            } else {
               a.onComplete();
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
