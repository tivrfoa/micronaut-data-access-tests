package io.micronaut.retry.intercept;

import io.micronaut.core.annotation.Internal;
import io.micronaut.retry.RetryState;
import io.micronaut.retry.annotation.DefaultRetryPredicate;
import io.micronaut.retry.annotation.RetryPredicate;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class SimpleRetry implements RetryState, MutableRetryState {
   private final int maxAttempts;
   private final double multiplier;
   private final Duration delay;
   private final Duration maxDelay;
   private final AtomicInteger attemptNumber = new AtomicInteger(0);
   private final AtomicLong overallDelay = new AtomicLong(0L);
   private final RetryPredicate predicate;
   private final Class<? extends Throwable> capturedException;

   SimpleRetry(int maxAttempts, double multiplier, Duration delay, Duration maxDelay, RetryPredicate predicate, Class<? extends Throwable> capturedException) {
      this.maxAttempts = maxAttempts;
      this.multiplier = multiplier;
      this.delay = delay;
      this.maxDelay = maxDelay;
      this.predicate = predicate;
      this.capturedException = capturedException;
   }

   SimpleRetry(int maxAttempts, double multiplier, Duration delay, Duration maxDelay, Class<? extends Throwable> capturedException) {
      this(maxAttempts, multiplier, delay, maxDelay, new DefaultRetryPredicate(), capturedException);
   }

   SimpleRetry(int maxAttempts, double multiplier, Duration delay) {
      this(maxAttempts, multiplier, delay, null, null);
   }

   @Override
   public boolean canRetry(Throwable exception) {
      if (exception == null) {
         return false;
      } else if (!this.predicate.test(exception)) {
         return false;
      } else {
         return this.attemptNumber.incrementAndGet() < this.maxAttempts + 1 && (this.maxDelay == null || this.overallDelay.get() < this.maxDelay.toMillis());
      }
   }

   @Override
   public int getMaxAttempts() {
      return this.maxAttempts;
   }

   @Override
   public int currentAttempt() {
      return this.attemptNumber.get();
   }

   @Override
   public OptionalDouble getMultiplier() {
      return this.multiplier > 0.0 ? OptionalDouble.of(this.multiplier) : OptionalDouble.empty();
   }

   @Override
   public Duration getDelay() {
      return this.delay;
   }

   @Override
   public Duration getOverallDelay() {
      return Duration.ofMillis(this.overallDelay.get());
   }

   @Override
   public Optional<Duration> getMaxDelay() {
      return Optional.ofNullable(this.maxDelay);
   }

   @Override
   public RetryPredicate getRetryPredicate() {
      return this.predicate;
   }

   @Override
   public Class<? extends Throwable> getCapturedException() {
      return this.capturedException;
   }

   @Internal
   @Override
   public long nextDelay() {
      double multiplier = this.getMultiplier().orElse(1.0);
      int current = this.attemptNumber.get() + 1;
      long delay = (long)((double)this.getDelay().toMillis() * multiplier) * (long)current;
      this.overallDelay.addAndGet(delay);
      return delay;
   }
}
