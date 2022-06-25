package io.micronaut.management.endpoint.loggers.impl;

import io.micronaut.context.annotation.Requires;
import io.micronaut.logging.LogLevel;
import io.micronaut.management.endpoint.loggers.LoggerConfiguration;
import io.micronaut.management.endpoint.loggers.LoggersEndpoint;
import io.micronaut.management.endpoint.loggers.LoggersManager;
import io.micronaut.management.endpoint.loggers.ManagedLoggingSystem;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Requires(
   beans = {LoggersEndpoint.class}
)
public class DefaultLoggersManager implements LoggersManager<Map<String, Object>> {
   private static final String LEVELS = "levels";
   private static final String LOGGERS = "loggers";

   @Override
   public Publisher<Map<String, Object>> getLoggers(ManagedLoggingSystem loggingSystem) {
      Map<String, Object> data = new LinkedHashMap(2);
      data.put("levels", getLogLevels());
      data.put("loggers", getLoggerData(loggingSystem.getLoggers()));
      return Flux.just(data);
   }

   @Override
   public Publisher<Map<String, Object>> getLogger(ManagedLoggingSystem loggingSystem, String name) {
      return Flux.just(getLoggerData(loggingSystem.getLogger(name)));
   }

   @Override
   public void setLogLevel(ManagedLoggingSystem loggingSystem, @NotBlank String name, @NotNull LogLevel level) {
      loggingSystem.setLogLevel(name, level);
   }

   private static Map<String, Object> getLoggerData(Collection<LoggerConfiguration> configurations) {
      return (Map<String, Object>)configurations.stream()
         .collect(Collectors.toMap(LoggerConfiguration::getName, LoggerConfiguration::getData, (l1, l2) -> l1, LinkedHashMap::new));
   }

   private static Map<String, Object> getLoggerData(LoggerConfiguration configuration) {
      return configuration.getData();
   }

   private static List<LogLevel> getLogLevels() {
      return Arrays.asList(LogLevel.values());
   }
}
