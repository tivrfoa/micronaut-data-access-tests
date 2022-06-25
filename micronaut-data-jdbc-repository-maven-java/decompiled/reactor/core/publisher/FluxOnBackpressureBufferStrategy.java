package reactor.core.publisher;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxOnBackpressureBufferStrategy<O> extends InternalFluxOperator<O, O> {
   final Consumer<? super O> onBufferOverflow;
   final int bufferSize;
   final boolean delayError;
   final BufferOverflowStrategy bufferOverflowStrategy;

   FluxOnBackpressureBufferStrategy(
      Flux<? extends O> source, int bufferSize, @Nullable Consumer<? super O> onBufferOverflow, BufferOverflowStrategy bufferOverflowStrategy
   ) {
      super(source);
      if (bufferSize < 1) {
         throw new IllegalArgumentException("Buffer Size must be strictly positive");
      } else {
         this.bufferSize = bufferSize;
         this.onBufferOverflow = onBufferOverflow;
         this.bufferOverflowStrategy = bufferOverflowStrategy;
         this.delayError = onBufferOverflow != null || bufferOverflowStrategy == BufferOverflowStrategy.ERROR;
      }
   }

   @Override
   public CoreSubscriber<? super O> subscribeOrReturn(CoreSubscriber<? super O> actual) {
      return new FluxOnBackpressureBufferStrategy.BackpressureBufferDropOldestSubscriber<>(
         actual, this.bufferSize, this.delayError, this.onBufferOverflow, this.bufferOverflowStrategy
      );
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class BackpressureBufferDropOldestSubscriber<T> extends ArrayDeque<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final int bufferSize;
      final Consumer<? super T> onOverflow;
      final boolean delayError;
      final BufferOverflowStrategy overflowStrategy;
      Subscription s;
      volatile boolean cancelled;
      volatile boolean done;
      Throwable error;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxOnBackpressureBufferStrategy.BackpressureBufferDropOldestSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxOnBackpressureBufferStrategy.BackpressureBufferDropOldestSubscriber.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxOnBackpressureBufferStrategy.BackpressureBufferDropOldestSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxOnBackpressureBufferStrategy.BackpressureBufferDropOldestSubscriber.class, "requested"
      );

      BackpressureBufferDropOldestSubscriber(
         CoreSubscriber<? super T> actual,
         int bufferSize,
         boolean delayError,
         @Nullable Consumer<? super T> onOverflow,
         BufferOverflowStrategy overflowStrategy
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.delayError = delayError;
         this.onOverflow = onOverflow;
         this.overflowStrategy = overflowStrategy;
         this.bufferSize = bufferSize;
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
               return this.size();
            } else if (key == Scannable.Attr.ERROR) {
               return this.error;
            } else if (key == Scannable.Attr.PREFETCH) {
               return Integer.MAX_VALUE;
            } else if (key == Scannable.Attr.DELAY_ERROR) {
               return this.delayError;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return this.done && this.isEmpty();
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
            boolean callOnOverflow = false;
            boolean callOnError = false;
            T overflowElement = t;
            synchronized(this) {
               if (this.size() == this.bufferSize) {
                  callOnOverflow = true;
                  switch(this.overflowStrategy) {
                     case DROP_OLDEST:
                        overflowElement = (T)this.pollFirst();
                        this.offer(t);
                     case DROP_LATEST:
                        break;
                     case ERROR:
                     default:
                        callOnError = true;
                  }
               } else {
                  this.offer(t);
               }
            }

            if (callOnOverflow) {
               label113:
               if (this.onOverflow != null) {
                  try {
                     this.onOverflow.accept(overflowElement);
                     break label113;
                  } catch (Throwable var12) {
                     Throwable ex = Operators.onOperatorError(this.s, var12, overflowElement, this.ctx);
                     this.onError(ex);
                  } finally {
                     Operators.onDiscard(overflowElement, this.ctx);
                  }

                  return;
               } else {
                  Operators.onDiscard(overflowElement, this.ctx);
               }
            }

            if (callOnError) {
               Throwable ex = Operators.onOperatorError(this.s, Exceptions.failWithOverflow(), overflowElement, this.ctx);
               this.onError(ex);
            }

            if (!callOnError && !callOnOverflow) {
               this.drain();
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
            this.drain();
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.drain();
         }
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;

            do {
               Subscriber<? super T> a = this.actual;
               if (a != null) {
                  this.innerDrain(a);
                  return;
               }

               missed = WIP.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }

      void innerDrain(Subscriber<? super T> a) {
         int missed = 1;

         do {
            long r = this.requested;

            long e;
            for(e = 0L; r != e; ++e) {
               boolean d = this.done;
               T t;
               synchronized(this) {
                  t = (T)this.poll();
               }

               boolean empty = t == null;
               if (this.checkTerminated(d, empty, a)) {
                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(t);
            }

            if (r == e) {
               boolean empty;
               synchronized(this) {
                  empty = this.isEmpty();
               }

               if (this.checkTerminated(this.done, empty, a)) {
                  return;
               }
            }

            if (e != 0L && r != Long.MAX_VALUE) {
               Operators.produced(REQUESTED, this, e);
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain();
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            if (WIP.getAndIncrement(this) == 0) {
               synchronized(this) {
                  this.clear();
               }
            }
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a) {
         if (this.cancelled) {
            this.s.cancel();
            synchronized(this) {
               this.clear();
               return true;
            }
         } else {
            if (d) {
               if (this.delayError) {
                  if (empty) {
                     Throwable e = this.error;
                     if (e != null) {
                        a.onError(e);
                     } else {
                        a.onComplete();
                     }

                     return true;
                  }
               } else {
                  Throwable e = this.error;
                  if (e != null) {
                     synchronized(this) {
                        this.clear();
                     }

                     a.onError(e);
                     return true;
                  }

                  if (empty) {
                     a.onComplete();
                     return true;
                  }
               }
            }

            return false;
         }
      }

      public void clear() {
         Operators.onDiscardMultiple(this, this.ctx);
         super.clear();
      }
   }
}
