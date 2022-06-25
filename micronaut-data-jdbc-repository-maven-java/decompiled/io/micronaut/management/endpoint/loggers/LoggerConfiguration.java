package io.micronaut.management.endpoint.loggers;

import io.micronaut.logging.LogLevel;
import java.util.LinkedHashMap;
import java.util.Map;

public class LoggerConfiguration {
   private static final String CONFIGURED_LEVEL = "configuredLevel";
   private static final String EFFECTIVE_LEVEL = "effectiveLevel";
   private final String name;
   private final LogLevel configuredLevel;
   private final LogLevel effectiveLevel;

   public LoggerConfiguration(String name, LogLevel configuredLevel, LogLevel effectiveLevel) {
      this.name = name;
      this.configuredLevel = configuredLevel;
      this.effectiveLevel = effectiveLevel;
   }

   public String getName() {
      return this.name;
   }

   public LogLevel configuredLevel() {
      return this.configuredLevel;
   }

   public LogLevel effectiveLevel() {
      return this.effectiveLevel;
   }

   public Map<String, Object> getData() {
      Map<String, Object> data = new LinkedHashMap(2);
      data.put("configuredLevel", this.configuredLevel());
      data.put("effectiveLevel", this.effectiveLevel());
      return data;
   }
}
