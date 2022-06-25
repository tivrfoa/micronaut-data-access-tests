package io.micronaut.management.endpoint.loggers.impl;

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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

@Singleton
@Requirements({@Requires(
   beans = {LoggersEndpoint.class}
), @Requires(
   classes = {LoggerContext.class}
)})
@Replaces(io.micronaut.logging.impl.Log4jLoggingSystem.class)
public class Log4jLoggingSystem implements ManagedLoggingSystem, LoggingSystem {
   @NonNull
   @Override
   public Collection<LoggerConfiguration> getLoggers() {
      return (Collection<LoggerConfiguration>)this.getLog4jLoggerContext()
         .getLoggers()
         .stream()
         .map(Log4jLoggingSystem::toLoggerConfiguration)
         .collect(Collectors.toList());
   }

   @NonNull
   @Override
   public LoggerConfiguration getLogger(String name) {
      return toLoggerConfiguration(LogManager.getLogger(name));
   }

   @Override
   public void setLogLevel(String name, LogLevel level) {
      if (name.equalsIgnoreCase("root")) {
         Configurator.setRootLevel(toLog4jLevel(level));
      } else {
         Configurator.setLevel(name, toLog4jLevel(level));
      }

   }

   private LoggerContext getLog4jLoggerContext() {
      return (LoggerContext)LogManager.getContext(false);
   }

   private static Level toLog4jLevel(LogLevel logLevel) {
      return logLevel == LogLevel.NOT_SPECIFIED ? null : Level.valueOf(logLevel.name());
   }

   private static LoggerConfiguration toLoggerConfiguration(Logger logger) {
      return new LoggerConfiguration(logger.getName(), toMicronautLogLevel(logger.getLevel()), toMicronautLogLevel(logger.getLevel()));
   }

   private static LogLevel toMicronautLogLevel(Level level) {
      return level == null ? LogLevel.NOT_SPECIFIED : LogLevel.valueOf(level.toString());
   }
}
