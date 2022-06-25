package io.micronaut.discovery.cloud.digitalocean;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.Toggleable;

@ConfigurationProperties("micronaut.application.digitalocean.metadata")
@Requires(
   env = {"digitalocean"}
)
public class DigitalOceanMetadataConfiguration implements Toggleable {
   public static final String PREFIX = "micronaut.application.digitalocean.metadata";
   public static final boolean DEFAULT_ENABLED = true;
   public static final String DEFAULT_URL = "http://169.254.169.254/metadata/v1.json";
   private String url = "http://169.254.169.254/metadata/v1.json";
   private boolean enabled = true;

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }
}
