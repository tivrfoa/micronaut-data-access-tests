package reactor.core.publisher;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxTimed<T> extends InternalFluxOperator<T, Timed<T>> {
   final Scheduler clock;

   FluxTimed(Flux<? extends T> source, Scheduler clock) {
      super(source);
      this.clock = clock;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Timed<T>> actual) {
      return new FluxTimed.TimedSubscriber<>(actual, this.clock);
   }

   @Override
   public int getPrefetch() {
      return 0;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ImmutableTimed<T> implements Timed<T> {
      final long eventElapsedSinceSubscriptionNanos;
      final long eventElapsedNanos;
      final long eventTimestampEpochMillis;
      final T event;

      ImmutableTimed(long eventElapsedSinceSubscriptionNanos, long eventElapsedNanos, long eventTimestampEpochMillis, T event) {
         this.eventElapsedSinceSubscriptionNanos = eventElapsedSinceSubscriptionNanos;
         this.eventElapsedNanos = eventElapsedNanos;
         this.eventTimestampEpochMillis = eventTimestampEpochMillis;
         this.event = event;
      }

      @Override
      public T get() {
         return this.event;
      }

      @Override
      public Duration elapsed() {
         return Duration.ofNanos(this.eventElapsedNanos);
      }

      @Override
      public Duration elapsedSinceSubscription() {
         return Duration.ofNanos(this.eventElapsedSinceSubscriptionNanos);
      }

      @Override
      public Instant timestamp() {
         return Instant.ofEpochMilli(this.eventTimestampEpochMillis);
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            FluxTimed.ImmutableTimed<?> timed = (FluxTimed.ImmutableTimed)o;
            return this.eventElapsedSinceSubscriptionNanos == timed.eventElapsedSinceSubscriptionNanos
               && this.eventElapsedNanos == timed.eventElapsedNanos
               && this.eventTimestampEpochMillis == timed.eventTimestampEpochMillis
               && this.event.equals(timed.event);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.eventElapsedSinceSubscriptionNanos, this.eventElapsedNanos, this.eventTimestampEpochMillis, this.event});
      }

      public String toString() {
         return "Timed("
            + this.event
            + "){eventElapsedNanos="
            + this.eventElapsedNanos
            + ", eventElapsedSinceSubscriptionNanos="
            + this.eventElapsedSinceSubscriptionNanos
            + ",  eventTimestampEpochMillis="
            + this.eventTimestampEpochMillis
            + '}';
      }
   }

   static final class TimedSubscriber<T> implements InnerOperator<T, Timed<T>> {
      final CoreSubscriber<? super Timed<T>> actual;
      final Scheduler clock;
      long subscriptionNanos;
      long lastEventNanos;
      boolean done;
      Subscription s;

      TimedSubscriber(CoreSubscriber<? super Timed<T>> actual, Scheduler clock) {
         this.actual = actual;
         this.clock = clock;
      }

      @Override
      public CoreSubscriber<? super Timed<T>> actual() {
         return this.actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.subscriptionNanos = this.clock.now(TimeUnit.NANOSECONDS);
            this.lastEventNanos = this.subscriptionNanos;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.currentContext());
         } else {
            long nowNanos = this.clock.now(TimeUnit.NANOSECONDS);
            long timestamp = this.clock.now(TimeUnit.MILLISECONDS);
            Timed<T> timed = new FluxTimed.ImmutableTimed<>(nowNanos - this.subscriptionNanos, nowNanos - this.lastEventNanos, timestamp, t);
            this.lastEventNanos = nowNanos;
            this.actual.onNext(timed);
         }
      }

      @Override
      public void onError(Throwable throwable) {
         if (this.done) {
            Operators.onErrorDropped(throwable, this.currentContext());
         } else {
            this.done = true;
            this.actual.onError(throwable);
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
      public void request(long l) {
         if (Operators.validate(l)) {
            this.s.request(l);
         }

      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }
}
