package io.micronaut.management.endpoint.loggers;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.logging.LoggingSystem;
import java.util.Collection;
import javax.validation.constraints.NotBlank;

public interface ManagedLoggingSystem extends LoggingSystem {
   @NonNull
   Collection<LoggerConfiguration> getLoggers();

   @NonNull
   LoggerConfiguration getLogger(@NotBlank String name);
}
