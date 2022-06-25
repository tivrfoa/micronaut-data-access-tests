package org.flywaydb.core.internal.util;

import java.sql.SQLException;

public class ExceptionUtils {
   public static Throwable getRootCause(Throwable throwable) {
      if (throwable == null) {
         return null;
      } else {
         Throwable cause = throwable;

         Throwable rootCause;
         while((rootCause = cause.getCause()) != null) {
            cause = rootCause;
         }

         return cause;
      }
   }

   public static String getThrowLocation(Throwable e) {
      StackTraceElement element = e.getStackTrace()[0];
      int lineNumber = element.getLineNumber();
      return element.getClassName() + "." + element.getMethodName() + (lineNumber < 0 ? "" : ":" + lineNumber) + (element.isNativeMethod() ? " [native]" : "");
   }

   public static String toMessage(SQLException e) {
      SQLException cause = e;

      while(cause.getNextException() != null) {
         cause = cause.getNextException();
      }

      String message = "SQL State  : " + cause.getSQLState() + "\nError Code : " + cause.getErrorCode() + "\n";
      if (cause.getMessage() != null) {
         message = message + "Message    : " + cause.getMessage().trim() + "\n";
      }

      return message;
   }

   private ExceptionUtils() {
   }
}
