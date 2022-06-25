package io.micronaut.core.reflect.exception;

public class InstantiationException extends RuntimeException {
   public InstantiationException(String message, Throwable cause) {
      super(message, cause);
   }

   public InstantiationException(String message) {
      super(message);
   }
}
