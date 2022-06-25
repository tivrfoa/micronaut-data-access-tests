package io.micronaut.logging;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.util.StringUtils;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BootstrapContextCompatible
@Singleton
@Context
@Requirements({@Requires(
   beans = {LoggingSystem.class}
), @Requires(
   beans = {Environment.class}
), @Requires(
   property = "logger.levels"
)})
@Internal
final class PropertiesLoggingLevelsConfigurer implements ApplicationEventListener<RefreshEvent> {
   static final String LOGGER_LEVELS_PROPERTY_PREFIX = "logger.levels";
   private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesLoggingLevelsConfigurer.class);
   private final Environment environment;
   private final List<LoggingSystem> loggingSystems;

   public PropertiesLoggingLevelsConfigurer(Environment environment, List<LoggingSystem> loggingSystems) {
      this.environment = environment;
      this.loggingSystems = loggingSystems;
      this.configureLogLevels();
   }

   public void onApplicationEvent(RefreshEvent event) {
      if (event.getSource().keySet().stream().anyMatch(key -> key.startsWith("logger.levels"))) {
         this.configureLogLevels();
      }

   }

   private void configureLogLevels() {
      Map<String, Object> rawProperties = this.environment.getProperties("logger.levels", StringConvention.RAW);
      Map<String, Object> generatedProperties = this.environment.getProperties("logger.levels");
      Map<String, Object> properties = new HashMap(generatedProperties.size() + rawProperties.size(), 1.0F);
      properties.putAll(rawProperties);
      properties.putAll(generatedProperties);
      properties.forEach(this::configureLogLevelForPrefix);
   }

   private void configureLogLevelForPrefix(final String loggerPrefix, final Object levelValue) {
      LogLevel newLevel;
      if (levelValue instanceof Boolean && !(Boolean)levelValue) {
         newLevel = LogLevel.OFF;
      } else {
         newLevel = toLogLevel(levelValue.toString());
      }

      if (newLevel == null) {
         throw new ConfigurationException("Invalid log level: '" + levelValue + "' for logger: '" + loggerPrefix + "'");
      } else {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Setting log level '{}' for logger: '{}'", newLevel, loggerPrefix);
         }

         LOGGER.info("Setting log level '{}' for logger: '{}'", newLevel, loggerPrefix);

         for(LoggingSystem loggingSystem : this.loggingSystems) {
            loggingSystem.setLogLevel(loggerPrefix, newLevel);
         }

      }
   }

   private static LogLevel toLogLevel(String logLevel) {
      if (StringUtils.isEmpty(logLevel)) {
         return LogLevel.NOT_SPECIFIED;
      } else {
         try {
            return (LogLevel)Enum.valueOf(LogLevel.class, logLevel);
         } catch (IllegalArgumentException var2) {
            return null;
         }
      }
   }
}
