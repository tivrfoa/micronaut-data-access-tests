package io.micronaut.data.exceptions;

public class NonUniqueResultException extends DataAccessException {
   public NonUniqueResultException() {
      super("Query did not return a unique result");
   }

   public NonUniqueResultException(String message) {
      super(message);
   }
}
