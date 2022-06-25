package io.micronaut.management.endpoint;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import java.util.Optional;

@ConfigurationProperties("endpoints.all")
public class EndpointDefaultConfiguration {
   public static final String PREFIX = "endpoints.all";
   public static final String PATH = "endpoints.all.path";
   public static final String PORT = "endpoints.all.port";
   public static final String DEFAULT_ENDPOINT_BASE_PATH = "/";
   private Boolean enabled;
   private Boolean sensitive;
   private Integer port;
   private String path = "/";

   public String getPath() {
      return this.path;
   }

   public Optional<Boolean> isEnabled() {
      return Optional.ofNullable(this.enabled);
   }

   public Optional<Boolean> isSensitive() {
      return Optional.ofNullable(this.sensitive);
   }

   public void setEnabled(Boolean enabled) {
      this.enabled = enabled;
   }

   public void setSensitive(Boolean sensitive) {
      this.sensitive = sensitive;
   }

   public void setPath(String path) {
      if (StringUtils.isNotEmpty(path)) {
         this.path = path;
      }

   }

   public Optional<Integer> getPort() {
      return Optional.ofNullable(this.port);
   }

   public void setPort(@Nullable Integer port) {
      this.port = port;
   }
}
