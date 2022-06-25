package io.micronaut.logging.impl;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

@Singleton
@Requires(
   classes = {Configurator.class}
)
@Internal
public class Log4jLoggingSystem implements LoggingSystem {
   @Override
   public void setLogLevel(String name, LogLevel level) {
      if (name.equalsIgnoreCase("root")) {
         Configurator.setRootLevel(toLevel(level));
      } else {
         Configurator.setLevel(name, toLevel(level));
      }

   }

   private static Level toLevel(LogLevel logLevel) {
      return logLevel == LogLevel.NOT_SPECIFIED ? null : Level.valueOf(logLevel.name());
   }
}
