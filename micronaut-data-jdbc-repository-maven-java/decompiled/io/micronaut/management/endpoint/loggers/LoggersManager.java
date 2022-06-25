package io.micronaut.management.endpoint.loggers;

import io.micronaut.logging.LogLevel;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.reactivestreams.Publisher;

public interface LoggersManager<T> {
   Publisher<T> getLoggers(ManagedLoggingSystem loggingSystem);

   Publisher<T> getLogger(ManagedLoggingSystem loggingSystem, @NotBlank String name);

   void setLogLevel(ManagedLoggingSystem loggingSystem, @NotBlank String name, @NotNull LogLevel level);
}
