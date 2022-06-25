package org.flywaydb.core.internal.logging.slf4j;

import org.flywaydb.core.api.logging.Log;
import org.slf4j.Logger;

public class Slf4jLog implements Log {
   private final Logger logger;

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

   public Slf4jLog(Logger logger) {
      this.logger = logger;
   }
}
