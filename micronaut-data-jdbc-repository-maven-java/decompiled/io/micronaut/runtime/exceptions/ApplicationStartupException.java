package io.micronaut.runtime.exceptions;

public class ApplicationStartupException extends RuntimeException {
   public ApplicationStartupException(String message, Throwable cause) {
      super(message, cause);
   }

   public ApplicationStartupException(String message) {
      super(message);
   }
}
