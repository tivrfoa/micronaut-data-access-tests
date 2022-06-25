package io.micronaut.discovery.config;

import io.micronaut.core.util.Toggleable;
import java.util.Optional;

public abstract class ConfigDiscoveryConfiguration implements Toggleable {
   public static final String PREFIX = "config";
   public static final String DEFAULT_PATH = "config/";
   public static final boolean DEFAULT_ENABLED = true;
   private boolean enabled = true;
   private String path;
   private ConfigDiscoveryConfiguration.Format format = ConfigDiscoveryConfiguration.Format.NATIVE;

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public Optional<String> getPath() {
      return Optional.ofNullable(this.path);
   }

   public void setPath(String path) {
      this.path = path;
   }

   public ConfigDiscoveryConfiguration.Format getFormat() {
      return this.format;
   }

   public void setFormat(ConfigDiscoveryConfiguration.Format format) {
      if (format != null) {
         this.format = format;
      }

   }

   public static enum Format {
      YAML,
      JSON,
      PROPERTIES,
      NATIVE,
      FILE;
   }
}
