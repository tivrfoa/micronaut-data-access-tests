package io.micronaut.inject.writer;

public class ClassGenerationException extends RuntimeException {
   public ClassGenerationException(String message, Throwable cause) {
      super(message, cause);
   }

   public ClassGenerationException(String message) {
      super(message);
   }
}
