package org.flywaydb.core.api.output;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.command.DbMigrate;

public class ErrorOutput implements OperationResult {
   public ErrorOutput.ErrorOutputItem error;

   public ErrorOutput(ErrorCode errorCode, String message, String stackTrace) {
      this.error = new ErrorOutput.ErrorOutputItem(errorCode, message, stackTrace);
   }

   public static ErrorOutput fromException(Exception exception) {
      String message = exception.getMessage();
      if (exception instanceof FlywayException) {
         FlywayException flywayException = (FlywayException)exception;
         return new ErrorOutput(flywayException.getErrorCode(), message == null ? "Error occurred" : message, null);
      } else {
         return new ErrorOutput(ErrorCode.FAULT, message == null ? "Fault occurred" : message, getStackTrace(exception));
      }
   }

   public static MigrateErrorResult fromMigrateException(DbMigrate.FlywayMigrateException exception) {
      return exception.getErrorResult();
   }

   private static String getStackTrace(Exception exception) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();

      PrintStream printStream;
      try {
         printStream = new PrintStream(output, true, "UTF-8");
      } catch (UnsupportedEncodingException var4) {
         return "";
      }

      exception.printStackTrace(printStream);
      return new String(output.toByteArray(), StandardCharsets.UTF_8);
   }

   public static class ErrorOutputItem {
      public ErrorCode errorCode;
      public String message;
      public String stackTrace;

      ErrorOutputItem(ErrorCode errorCode, String message, String stackTrace) {
         this.errorCode = errorCode;
         this.message = message;
         this.stackTrace = stackTrace;
      }
   }
}
