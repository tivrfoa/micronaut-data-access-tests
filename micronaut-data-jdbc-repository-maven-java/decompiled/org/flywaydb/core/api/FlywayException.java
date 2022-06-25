package org.flywaydb.core.api;

public class FlywayException extends RuntimeException {
   private ErrorCode errorCode = ErrorCode.ERROR;

   public FlywayException(String message, Throwable cause, ErrorCode errorCode) {
      super(message, cause);
      this.errorCode = errorCode;
   }

   public FlywayException(String message, ErrorCode errorCode) {
      super(message);
      this.errorCode = errorCode;
   }

   public FlywayException(String message, Throwable cause) {
      super(message, cause);
   }

   public FlywayException(Throwable cause) {
      super(cause);
   }

   public FlywayException(String message) {
      super(message);
   }

   public FlywayException() {
   }

   public ErrorCode getErrorCode() {
      return this.errorCode;
   }
}
