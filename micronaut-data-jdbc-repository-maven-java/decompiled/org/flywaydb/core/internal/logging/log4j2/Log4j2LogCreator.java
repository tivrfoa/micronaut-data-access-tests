package org.flywaydb.core.internal.logging.log4j2;

import org.apache.logging.log4j.LogManager;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

public class Log4j2LogCreator implements LogCreator {
   @Override
   public Log createLogger(Class<?> clazz) {
      return new Log4j2Log(LogManager.getLogger(clazz.getName()));
   }
}
