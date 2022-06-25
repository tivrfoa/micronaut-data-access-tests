package io.micronaut.context.condition;

public interface Failure {
   String getMessage();

   static Failure simple(String message) {
      return () -> message;
   }
}
