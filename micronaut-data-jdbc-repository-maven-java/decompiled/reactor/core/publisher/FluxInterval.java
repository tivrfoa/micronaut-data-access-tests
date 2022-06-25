package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxInterval extends Flux<Long> implements SourceProducer<Long> {
   final Scheduler timedScheduler;
   final long initialDelay;
   final long period;
   final TimeUnit unit;

   FluxInterval(long initialDelay, long period, TimeUnit unit, Scheduler timedScheduler) {
      if (period < 0L) {
         throw new IllegalArgumentException("period >= 0 required but it was " + period);
      } else {
         this.initialDelay = initialDelay;
         this.period = period;
         this.unit = (TimeUnit)Objects.requireNonNull(unit, "unit");
         this.timedScheduler = (Scheduler)Objects.requireNonNull(timedScheduler, "timedScheduler");
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super Long> actual) {
      Scheduler.Worker w = this.timedScheduler.createWorker();
      FluxInterval.IntervalRunnable r = new FluxInterval.IntervalRunnable(actual, w);
      actual.onSubscribe(r);

      try {
         w.schedulePeriodically(r, this.initialDelay, this.period, this.unit);
      } catch (RejectedExecutionException var5) {
         if (!r.cancelled) {
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

   static final class IntervalRunnable implements Runnable, Subscription, InnerProducer<Long> {
      final CoreSubscriber<? super Long> actual;
      final Scheduler.Worker worker;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxInterval.IntervalRunnable> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxInterval.IntervalRunnable.class, "requested"
      );
      long count;
      volatile boolean cancelled;

      IntervalRunnable(CoreSubscriber<? super Long> actual, Scheduler.Worker worker) {
         this.actual = actual;
         this.worker = worker;
      }

      @Override
      public CoreSubscriber<? super Long> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.worker;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      public void run() {
         if (!this.cancelled) {
            if (this.requested != 0L) {
               this.actual.onNext(Long.valueOf((long)(this.count++)));
               if (this.requested != Long.MAX_VALUE) {
                  REQUESTED.decrementAndGet(this);
               }
            } else {
               this.cancel();
               this.actual
                  .onError(
                     Exceptions.failWithOverflow(
                        "Could not emit tick "
                           + this.count
                           + " due to lack of requests (interval doesn't support small downstream requests that replenish slower than the ticks)"
                     )
                  );
            }
         }

      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.worker.dispose();
         }

      }
   }
}
