package org.flywaydb.core.internal.logging.javautil;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.flywaydb.core.api.logging.Log;

public class JavaUtilLog implements Log {
   private final Logger logger;

   @Override
   public boolean isDebugEnabled() {
      return this.logger.isLoggable(Level.FINE);
   }

   @Override
   public void debug(String message) {
      this.log(Level.FINE, message, null);
   }

   @Override
   public void info(String message) {
      this.log(Level.INFO, message, null);
   }

   @Override
   public void warn(String message) {
      this.log(Level.WARNING, message, null);
   }

   @Override
   public void error(String message) {
      this.log(Level.SEVERE, message, null);
   }

   @Override
   public void error(String message, Exception e) {
      this.log(Level.SEVERE, message, e);
   }

   @Override
   public void notice(String message) {
   }

   private void log(Level level, String message, Exception e) {
      LogRecord record = new LogRecord(level, message);
      record.setLoggerName(this.logger.getName());
      record.setThrown(e);
      record.setSourceClassName(this.logger.getName());
      record.setSourceMethodName(this.getMethodName());
      this.logger.log(record);
   }

   private String getMethodName() {
      StackTraceElement[] steArray = new Throwable().getStackTrace();

      for(StackTraceElement stackTraceElement : steArray) {
         if (this.logger.getName().equals(stackTraceElement.getClassName())) {
            return stackTraceElement.getMethodName();
         }
      }

      return null;
   }

   public JavaUtilLog(Logger logger) {
      this.logger = logger;
   }
}
