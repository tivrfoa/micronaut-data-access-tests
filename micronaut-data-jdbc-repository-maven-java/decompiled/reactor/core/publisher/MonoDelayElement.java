package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class MonoDelayElement<T> extends InternalMonoOperator<T, T> {
   final Scheduler timedScheduler;
   final long delay;
   final TimeUnit unit;

   MonoDelayElement(Mono<? extends T> source, long delay, TimeUnit unit, Scheduler timedScheduler) {
      super(source);
      this.delay = delay;
      this.unit = (TimeUnit)Objects.requireNonNull(unit, "unit");
      this.timedScheduler = (Scheduler)Objects.requireNonNull(timedScheduler, "timedScheduler");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoDelayElement.DelayElementSubscriber<>(actual, this.timedScheduler, this.delay, this.unit);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.timedScheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class DelayElementSubscriber<T> extends Operators.MonoSubscriber<T, T> {
      final long delay;
      final Scheduler scheduler;
      final TimeUnit unit;
      Subscription s;
      volatile Disposable task;
      boolean done;

      DelayElementSubscriber(CoreSubscriber<? super T> actual, Scheduler scheduler, long delay, TimeUnit unit) {
         super(actual);
         this.scheduler = scheduler;
         this.delay = delay;
         this.unit = unit;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.scheduler;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void cancel() {
         super.cancel();
         if (this.task != null) {
            this.task.dispose();
         }

         if (this.s != Operators.cancelledSubscription()) {
            this.s.cancel();
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
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            this.done = true;

            try {
               this.task = this.scheduler.schedule(() -> this.complete(t), this.delay, this.unit);
            } catch (RejectedExecutionException var3) {
               this.actual.onError(Operators.onRejectedExecution(var3, this, null, t, this.actual.currentContext()));
            }
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }
   }
}
