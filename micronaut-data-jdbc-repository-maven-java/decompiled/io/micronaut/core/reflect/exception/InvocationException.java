package io.micronaut.core.reflect.exception;

public class InvocationException extends RuntimeException {
   public InvocationException(String message) {
      super(message);
   }

   public InvocationException(String message, Throwable cause) {
      super(message, cause);
   }
}
