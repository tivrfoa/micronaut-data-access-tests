package io.micronaut.logging;

import io.micronaut.core.annotation.Indexed;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Indexed(LoggingSystem.class)
public interface LoggingSystem {
   void setLogLevel(@NotBlank String name, @NotNull LogLevel level);
}
