package org.flywaydb.core.api;

public class ErrorDetails {
   public final ErrorCode errorCode;
   public final String errorMessage;

   public ErrorDetails(ErrorCode errorCode, String errorMessage) {
      this.errorCode = errorCode;
      this.errorMessage = errorMessage;
   }
}
