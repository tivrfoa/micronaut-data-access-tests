package io.micronaut.retry;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.retry.annotation.RetryPredicate;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalDouble;

public interface RetryState {
   boolean canRetry(Throwable exception);

   int getMaxAttempts();

   int currentAttempt();

   OptionalDouble getMultiplier();

   Duration getDelay();

   Duration getOverallDelay();

   Optional<Duration> getMaxDelay();

   default RetryPredicate getRetryPredicate() {
      throw new UnsupportedOperationException("Retry predicate not supported on this type");
   }

   Class<? extends Throwable> getCapturedException();

   default void open() {
   }

   default void close(@Nullable Throwable exception) {
   }
}
