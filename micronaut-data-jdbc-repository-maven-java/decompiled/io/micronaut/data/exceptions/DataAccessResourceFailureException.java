package io.micronaut.data.exceptions;

public class DataAccessResourceFailureException extends DataAccessException {
   public DataAccessResourceFailureException(String message) {
      super(message);
   }

   public DataAccessResourceFailureException(String message, Throwable cause) {
      super(message, cause);
   }
}
