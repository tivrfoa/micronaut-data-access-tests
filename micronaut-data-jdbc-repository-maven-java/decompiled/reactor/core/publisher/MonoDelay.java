package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class MonoDelay extends Mono<Long> implements Scannable, SourceProducer<Long> {
   final Scheduler timedScheduler;
   final long delay;
   final TimeUnit unit;
   static final String CONTEXT_OPT_OUT_NOBACKPRESSURE = "reactor.core.publisher.MonoDelay.failOnBackpressure";

   MonoDelay(long delay, TimeUnit unit, Scheduler timedScheduler) {
      this.delay = delay;
      this.unit = (TimeUnit)Objects.requireNonNull(unit, "unit");
      this.timedScheduler = (Scheduler)Objects.requireNonNull(timedScheduler, "timedScheduler");
   }

   @Override
   public void subscribe(CoreSubscriber<? super Long> actual) {
      boolean failOnBackpressure = actual.currentContext().getOrDefault("reactor.core.publisher.MonoDelay.failOnBackpressure", Boolean.valueOf(false))
         == Boolean.TRUE;
      MonoDelay.MonoDelayRunnable r = new MonoDelay.MonoDelayRunnable(actual, failOnBackpressure);
      actual.onSubscribe(r);

      try {
         r.setCancel(this.timedScheduler.schedule(r, this.delay, this.unit));
      } catch (RejectedExecutionException var5) {
         if (!MonoDelay.MonoDelayRunnable.wasCancelled(r.state)) {
            actual.onError(Operators.onRejectedExecution(var5, r, null, null, actual.currentContext()));
         }
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.timedScheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : null;
      }
   }

   static final class MonoDelayRunnable implements Runnable, InnerProducer<Long> {
      final CoreSubscriber<? super Long> actual;
      final boolean failOnBackpressure;
      Disposable cancel;
      volatile int state;
      static final AtomicIntegerFieldUpdater<MonoDelay.MonoDelayRunnable> STATE = AtomicIntegerFieldUpdater.newUpdater(
         MonoDelay.MonoDelayRunnable.class, "state"
      );
      static final byte FLAG_CANCELLED = 64;
      static final byte FLAG_REQUESTED = 32;
      static final byte FLAG_REQUESTED_EARLY = 16;
      static final byte FLAG_CANCEL_SET = 1;
      static final byte FLAG_DELAY_DONE = 2;
      static final byte FLAG_PROPAGATED = 4;

      MonoDelayRunnable(CoreSubscriber<? super Long> actual, boolean failOnBackpressure) {
         this.actual = actual;
         this.failOnBackpressure = failOnBackpressure;
      }

      static int markCancelFutureSet(MonoDelay.MonoDelayRunnable instance) {
         while(true) {
            int state = instance.state;
            if (!wasCancelled(state) && !wasCancelFutureSet(state)) {
               if (!STATE.compareAndSet(instance, state, state | 1)) {
                  continue;
               }

               return state;
            }

            return state;
         }
      }

      static boolean wasCancelFutureSet(int state) {
         return (state & 1) == 1;
      }

      static int markCancelled(MonoDelay.MonoDelayRunnable instance) {
         while(true) {
            int state = instance.state;
            if (!wasCancelled(state) && !wasPropagated(state)) {
               if (!STATE.compareAndSet(instance, state, state | 64)) {
                  continue;
               }

               return state;
            }

            return state;
         }
      }

      static boolean wasCancelled(int state) {
         return (state & 64) == 64;
      }

      static int markDelayDone(MonoDelay.MonoDelayRunnable instance) {
         while(true) {
            int state = instance.state;
            if (!wasCancelled(state) && !wasDelayDone(state)) {
               if (!STATE.compareAndSet(instance, state, state | 2)) {
                  continue;
               }

               return state;
            }

            return state;
         }
      }

      static boolean wasDelayDone(int state) {
         return (state & 2) == 2;
      }

      static int markRequested(MonoDelay.MonoDelayRunnable instance) {
         while(true) {
            int state = instance.state;
            if (!wasCancelled(state) && !wasRequested(state)) {
               int newFlag = 32;
               if (!wasDelayDone(state)) {
                  newFlag |= 16;
               }

               if (!STATE.compareAndSet(instance, state, state | newFlag)) {
                  continue;
               }

               return state;
            }

            return state;
         }
      }

      static boolean wasRequested(int state) {
         return (state & 32) == 32;
      }

      static int markPropagated(MonoDelay.MonoDelayRunnable instance) {
         int state;
         do {
            state = instance.state;
            if (wasCancelled(state)) {
               return state;
            }
         } while(!STATE.compareAndSet(instance, state, state | 4));

         return state;
      }

      static boolean wasPropagated(int state) {
         return (state & 4) == 4;
      }

      @Override
      public CoreSubscriber<? super Long> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
               return wasRequested(this.state) ? 1L : 0L;
            } else if (key == Scannable.Attr.CANCELLED) {
               return wasCancelled(this.state);
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerProducer.super.scanUnsafe(key);
            }
         } else {
            return wasDelayDone(this.state) && wasRequested(this.state);
         }
      }

      void setCancel(Disposable cancel) {
         Disposable c = this.cancel;
         this.cancel = cancel;
         int previousState = markCancelFutureSet(this);
         if (wasCancelFutureSet(previousState)) {
            if (c != null) {
               c.dispose();
            }

         } else {
            if (wasCancelled(previousState)) {
               cancel.dispose();
            }

         }
      }

      private void propagateDelay() {
         int previousState = markPropagated(this);
         if (!wasCancelled(previousState)) {
            try {
               this.actual.onNext(Long.valueOf(0L));
               this.actual.onComplete();
            } catch (Throwable var3) {
               this.actual.onError(Operators.onOperatorError(var3, this.actual.currentContext()));
            }

         }
      }

      public void run() {
         int previousState = markDelayDone(this);
         if (!wasCancelled(previousState) && !wasDelayDone(previousState)) {
            if (wasRequested(previousState)) {
               this.propagateDelay();
            } else if (this.failOnBackpressure) {
               this.actual.onError(Exceptions.failWithOverflow("Could not emit value due to lack of requests"));
            }

         }
      }

      @Override
      public void cancel() {
         int previousState = markCancelled(this);
         if (!wasCancelled(previousState) && !wasPropagated(previousState)) {
            if (wasCancelFutureSet(previousState)) {
               this.cancel.dispose();
            }

         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            int previousState = markRequested(this);
            if (wasCancelled(previousState) || wasRequested(previousState)) {
               return;
            }

            if (wasDelayDone(previousState) && !this.failOnBackpressure) {
               this.propagateDelay();
            }
         }

      }
   }
}
