package io.micronaut.data.exceptions;

public class OptimisticLockException extends DataAccessException {
   public OptimisticLockException(String message) {
      super(message);
   }
}
