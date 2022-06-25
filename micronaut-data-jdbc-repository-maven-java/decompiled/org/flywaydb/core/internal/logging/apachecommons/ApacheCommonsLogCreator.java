package org.flywaydb.core.internal.logging.apachecommons;

import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

public class ApacheCommonsLogCreator implements LogCreator {
   @Override
   public Log createLogger(Class<?> clazz) {
      return new ApacheCommonsLog(LogFactory.getLog(clazz));
   }
}
