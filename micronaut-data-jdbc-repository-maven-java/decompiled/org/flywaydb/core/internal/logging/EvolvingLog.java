package org.flywaydb.core.internal.logging;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.logging.buffered.BufferedLog;

public class EvolvingLog implements Log {
   private final Object $lock = new Object[0];
   private Log log;
   private final Class<?> clazz;

   private void updateLog() {
      synchronized(this.$lock) {
         Log newLog = ((EvolvingLog)LogFactory.getLog(this.clazz)).getLog();
         if (this.log instanceof BufferedLog && !(newLog instanceof BufferedLog)) {
            ((BufferedLog)this.log).flush(newLog);
         }

         this.log = newLog;
      }
   }

   public Log getLog() {
      return this.log;
   }

   @Override
   public boolean isDebugEnabled() {
      return this.log.isDebugEnabled();
   }

   @Override
   public void debug(String message) {
      this.updateLog();
      this.log.debug(message);
   }

   @Override
   public void info(String message) {
      this.updateLog();
      this.log.info(message);
   }

   @Override
   public void warn(String message) {
      this.updateLog();
      this.log.warn(message);
   }

   @Override
   public void error(String message) {
      this.updateLog();
      this.log.error(message);
   }

   @Override
   public void error(String message, Exception e) {
      this.updateLog();
      this.log.error(message, e);
   }

   @Override
   public void notice(String message) {
      this.updateLog();
      this.log.notice(message);
   }

   public EvolvingLog(Log log, Class<?> clazz) {
      this.log = log;
      this.clazz = clazz;
   }
}
