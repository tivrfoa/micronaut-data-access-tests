package io.micronaut.retry.exception;

public class CircuitOpenException extends RetryException {
   public CircuitOpenException(String message) {
      super(message);
   }

   public CircuitOpenException(String message, Throwable cause) {
      super(message, cause);
   }
}
