package io.micronaut.retry.exception;

public class FallbackException extends RetryException {
   public FallbackException(String message) {
      super(message);
   }

   public FallbackException(String message, Throwable cause) {
      super(message, cause);
   }
}
