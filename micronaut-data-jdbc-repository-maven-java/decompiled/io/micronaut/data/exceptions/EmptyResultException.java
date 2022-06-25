package io.micronaut.data.exceptions;

public class EmptyResultException extends DataAccessException {
   public EmptyResultException() {
      super("Query produced no result");
   }
}
