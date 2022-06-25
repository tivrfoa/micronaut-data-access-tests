package io.micronaut.management.health.indicator.discovery;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.Toggleable;

@ConfigurationProperties("endpoints.health.discovery-client")
@Requires(
   property = "endpoints.health.discovery-client.enabled",
   notEquals = "false"
)
public class DiscoveryClientHealthIndicatorConfiguration implements Toggleable {
   static final String PREFIX = "endpoints.health.discovery-client";
   private boolean enabled = true;

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
