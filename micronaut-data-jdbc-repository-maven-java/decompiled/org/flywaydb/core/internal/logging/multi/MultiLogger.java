package org.flywaydb.core.internal.logging.multi;

import java.util.List;
import org.flywaydb.core.api.logging.Log;

public class MultiLogger implements Log {
   private final List<Log> logs;

   @Override
   public boolean isDebugEnabled() {
      for(Log log : this.logs) {
         if (!log.isDebugEnabled()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void debug(String message) {
      for(Log log : this.logs) {
         log.debug(message);
      }

   }

   @Override
   public void info(String message) {
      for(Log log : this.logs) {
         log.info(message);
      }

   }

   @Override
   public void warn(String message) {
      for(Log log : this.logs) {
         log.warn(message);
      }

   }

   @Override
   public void error(String message) {
      for(Log log : this.logs) {
         log.error(message);
      }

   }

   @Override
   public void error(String message, Exception e) {
      for(Log log : this.logs) {
         log.error(message, e);
      }

   }

   @Override
   public void notice(String message) {
      for(Log log : this.logs) {
         log.notice(message);
      }

   }

   public MultiLogger(List<Log> logs) {
      this.logs = logs;
   }
}
