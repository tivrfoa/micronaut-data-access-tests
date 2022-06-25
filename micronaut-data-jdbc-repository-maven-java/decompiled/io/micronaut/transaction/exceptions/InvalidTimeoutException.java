package io.micronaut.transaction.exceptions;

import java.time.Duration;

public class InvalidTimeoutException extends TransactionUsageException {
   private final Duration timeout;

   public InvalidTimeoutException(String msg, Duration timeout) {
      super(msg);
      this.timeout = timeout;
   }

   public Duration getTimeout() {
      return this.timeout;
   }
}
