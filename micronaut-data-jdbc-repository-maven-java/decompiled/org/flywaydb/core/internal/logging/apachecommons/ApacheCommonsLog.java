package org.flywaydb.core.internal.logging.apachecommons;

import org.flywaydb.core.api.logging.Log;

public class ApacheCommonsLog implements Log {
   private final org.apache.commons.logging.Log logger;

   @Override
   public boolean isDebugEnabled() {
      return this.logger.isDebugEnabled();
   }

   @Override
   public void debug(String message) {
      this.logger.debug(message);
   }

   @Override
   public void info(String message) {
      this.logger.info(message);
   }

   @Override
   public void warn(String message) {
      this.logger.warn(message);
   }

   @Override
   public void error(String message) {
      this.logger.error(message);
   }

   @Override
   public void error(String message, Exception e) {
      this.logger.error(message, e);
   }

   @Override
   public void notice(String message) {
   }

   public ApacheCommonsLog(org.apache.commons.logging.Log logger) {
      this.logger = logger;
   }
}
