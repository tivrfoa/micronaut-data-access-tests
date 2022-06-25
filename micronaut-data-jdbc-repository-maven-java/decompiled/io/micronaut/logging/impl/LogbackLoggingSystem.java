package io.micronaut.logging.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Singleton;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(
   classes = {LoggerContext.class}
)
@Internal
public final class LogbackLoggingSystem implements LoggingSystem {
   @Override
   public void setLogLevel(String name, LogLevel level) {
      getLoggerContext().getLogger(name).setLevel(toLevel(level));
   }

   private static LoggerContext getLoggerContext() {
      return (LoggerContext)LoggerFactory.getILoggerFactory();
   }

   private static Level toLevel(LogLevel logLevel) {
      return logLevel == LogLevel.NOT_SPECIFIED ? null : Level.valueOf(logLevel.name());
   }
}
