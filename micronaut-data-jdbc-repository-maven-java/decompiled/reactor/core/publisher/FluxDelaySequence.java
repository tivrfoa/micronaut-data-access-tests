package reactor.core.publisher;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;

final class FluxDelaySequence<T> extends InternalFluxOperator<T, T> {
   final Duration delay;
   final Scheduler scheduler;

   FluxDelaySequence(Flux<T> source, Duration delay, Scheduler scheduler) {
      super(source);
      this.delay = delay;
      this.scheduler = scheduler;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      Scheduler.Worker w = this.scheduler.createWorker();
      return new FluxDelaySequence.DelaySubscriber<>(actual, this.delay, w);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class DelaySubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final long delay;
      final TimeUnit timeUnit;
      final Scheduler.Worker w;
      Subscription s;
      volatile boolean done;
      volatile long delayed;
      static final AtomicLongFieldUpdater<FluxDelaySequence.DelaySubscriber> DELAYED = AtomicLongFieldUpdater.newUpdater(
         FluxDelaySequence.DelaySubscriber.class, "delayed"
      );

      DelaySubscriber(CoreSubscriber<? super T> actual, Duration delay, Scheduler.Worker w) {
         this.actual = Operators.serialize(actual);
         this.w = w;
         this.delay = delay.toNanos();
         this.timeUnit = TimeUnit.NANOSECONDS;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
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
         if (!this.done && this.delayed >= 0L) {
            DELAYED.incrementAndGet(this);
            this.w.schedule(() -> this.delayedNext(t), this.delay, this.timeUnit);
         } else {
            Operators.onNextDropped(t, this.currentContext());
         }
      }

      private void delayedNext(T t) {
         DELAYED.decrementAndGet(this);
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.currentContext());
         } else {
            this.done = true;
            if (DELAYED.compareAndSet(this, 0L, -1L)) {
               this.actual.onError(t);
            } else {
               this.w.schedule(new FluxDelaySequence.DelaySubscriber.OnError(t), this.delay, this.timeUnit);
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            if (DELAYED.compareAndSet(this, 0L, -1L)) {
               this.actual.onComplete();
            } else {
               this.w.schedule(new FluxDelaySequence.DelaySubscriber.OnComplete(), this.delay, this.timeUnit);
            }

         }
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
         this.w.dispose();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.w;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key != Scannable.Attr.CANCELLED) {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         } else {
            return this.w.isDisposed() && !this.done;
         }
      }

      final class OnComplete implements Runnable {
         public void run() {
            try {
               DelaySubscriber.this.actual.onComplete();
            } finally {
               DelaySubscriber.this.w.dispose();
            }

         }
      }

      final class OnError implements Runnable {
         private final Throwable t;

         OnError(Throwable t) {
            this.t = t;
         }

         public void run() {
            try {
               DelaySubscriber.this.actual.onError(this.t);
            } finally {
               DelaySubscriber.this.w.dispose();
            }

         }
      }
   }
}
