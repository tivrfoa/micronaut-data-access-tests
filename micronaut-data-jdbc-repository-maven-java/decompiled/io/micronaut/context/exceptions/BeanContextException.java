package io.micronaut.context.exceptions;

public class BeanContextException extends RuntimeException {
   public BeanContextException(String message, Throwable cause) {
      super(message, cause);
   }

   public BeanContextException(String message) {
      super(message);
   }
}
