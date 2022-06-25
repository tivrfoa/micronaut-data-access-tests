package io.micronaut.retry.exception;

public class RetryException extends RuntimeException {
   public RetryException(String message) {
      super(message);
   }

   public RetryException(String message, Throwable cause) {
      super(message, cause);
   }
}
