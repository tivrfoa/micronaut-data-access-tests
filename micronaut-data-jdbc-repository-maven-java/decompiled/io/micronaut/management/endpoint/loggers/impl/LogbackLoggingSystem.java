package io.micronaut.management.endpoint.loggers.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import io.micronaut.management.endpoint.loggers.LoggerConfiguration;
import io.micronaut.management.endpoint.loggers.LoggersEndpoint;
import io.micronaut.management.endpoint.loggers.ManagedLoggingSystem;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

@Singleton
@Requirements({@Requires(
   beans = {LoggersEndpoint.class}
), @Requires(
   classes = {LoggerContext.class}
)})
@Replaces(io.micronaut.logging.impl.LogbackLoggingSystem.class)
public class LogbackLoggingSystem implements ManagedLoggingSystem, LoggingSystem {
   @NonNull
   @Override
   public Collection<LoggerConfiguration> getLoggers() {
      return (Collection<LoggerConfiguration>)getLoggerContext()
         .getLoggerList()
         .stream()
         .map(LogbackLoggingSystem::toLoggerConfiguration)
         .collect(Collectors.toList());
   }

   @NonNull
   @Override
   public LoggerConfiguration getLogger(String name) {
      return toLoggerConfiguration(getLoggerContext().getLogger(name));
   }

   @Override
   public void setLogLevel(String name, LogLevel level) {
      getLoggerContext().getLogger(name).setLevel(toLevel(level));
   }

   private static LoggerContext getLoggerContext() {
      return (LoggerContext)LoggerFactory.getILoggerFactory();
   }

   private static LoggerConfiguration toLoggerConfiguration(Logger logger) {
      return new LoggerConfiguration(logger.getName(), toLogLevel(logger.getLevel()), toLogLevel(logger.getEffectiveLevel()));
   }

   private static LogLevel toLogLevel(Level level) {
      return level == null ? LogLevel.NOT_SPECIFIED : LogLevel.valueOf(level.toString());
   }

   private static Level toLevel(LogLevel logLevel) {
      return logLevel == LogLevel.NOT_SPECIFIED ? null : Level.valueOf(logLevel.name());
   }
}
