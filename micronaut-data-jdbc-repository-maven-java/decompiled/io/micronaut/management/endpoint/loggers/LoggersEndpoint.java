package io.micronaut.management.endpoint.loggers;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.type.Argument;
import io.micronaut.logging.LogLevel;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.management.endpoint.annotation.Selector;
import io.micronaut.management.endpoint.annotation.Sensitive;
import io.micronaut.management.endpoint.annotation.Write;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Endpoint(
   id = "loggers",
   defaultSensitive = false,
   defaultEnabled = false
)
public class LoggersEndpoint {
   public static final String NAME = "loggers";
   public static final String PREFIX = "endpoints.loggers";
   public static final boolean DEFAULT_ENABLED = false;
   public static final boolean DEFAULT_SENSITIVE = false;
   private final ManagedLoggingSystem loggingSystem;
   private final LoggersManager<Map<String, Object>> loggersManager;
   private boolean writeSensitive = true;

   public LoggersEndpoint(ManagedLoggingSystem loggingSystem, LoggersManager<Map<String, Object>> loggersManager) {
      this.loggingSystem = loggingSystem;
      this.loggersManager = loggersManager;
   }

   @Read
   @SingleResult
   public Publisher<Map<String, Object>> loggers() {
      return Mono.from(this.loggersManager.getLoggers(this.loggingSystem));
   }

   @Read
   @SingleResult
   public Publisher<Map<String, Object>> logger(@NotBlank @Selector String name) {
      return Mono.from(this.loggersManager.getLogger(this.loggingSystem, name));
   }

   @Write
   @Sensitive(
      property = "write-sensitive"
   )
   public void setLogLevel(@NotBlank @Selector String name, @Nullable LogLevel configuredLevel) {
      try {
         this.loggersManager.setLogLevel(this.loggingSystem, name, configuredLevel != null ? configuredLevel : LogLevel.NOT_SPECIFIED);
      } catch (IllegalArgumentException var4) {
         throw new UnsatisfiedArgumentException(Argument.of(LogLevel.class, "configuredLevel"), "Invalid log level specified: " + configuredLevel);
      }
   }

   public boolean isWriteSensitive() {
      return this.writeSensitive;
   }

   public void setWriteSensitive(boolean writeSensitive) {
      this.writeSensitive = writeSensitive;
   }
}
