package io.micronaut.core.value;

public class ValueException extends RuntimeException {
   public ValueException(String message, Throwable cause) {
      super(message, cause);
   }

   public ValueException(String message) {
      super(message);
   }
}
