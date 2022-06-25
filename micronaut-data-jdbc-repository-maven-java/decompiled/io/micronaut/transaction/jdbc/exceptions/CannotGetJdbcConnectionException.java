package io.micronaut.transaction.jdbc.exceptions;

public class CannotGetJdbcConnectionException extends RuntimeException {
   public CannotGetJdbcConnectionException(String message) {
      super(message);
   }

   public CannotGetJdbcConnectionException(String message, Throwable cause) {
      super(message, cause);
   }
}
