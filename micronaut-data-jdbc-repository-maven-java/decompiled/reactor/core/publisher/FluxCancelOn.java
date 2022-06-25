package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxCancelOn<T> extends InternalFluxOperator<T, T> {
   final Scheduler scheduler;

   public FluxCancelOn(Flux<T> source, Scheduler scheduler) {
      super(source);
      this.scheduler = (Scheduler)Objects.requireNonNull(scheduler, "scheduler");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxCancelOn.CancelSubscriber<>(actual, this.scheduler);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class CancelSubscriber<T> implements InnerOperator<T, T>, Runnable {
      final CoreSubscriber<? super T> actual;
      final Scheduler scheduler;
      Subscription s;
      volatile int cancelled = 0;
      static final AtomicIntegerFieldUpdater<FluxCancelOn.CancelSubscriber> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxCancelOn.CancelSubscriber.class, "cancelled"
      );

      CancelSubscriber(CoreSubscriber<? super T> actual, Scheduler scheduler) {
         this.actual = actual;
         this.scheduler = scheduler;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.scheduler;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      public void run() {
         this.s.cancel();
      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            try {
               this.scheduler.schedule(this);
            } catch (RejectedExecutionException var2) {
               throw Operators.onRejectedExecution(var2, this.actual.currentContext());
            }
         }

      }
   }
}
