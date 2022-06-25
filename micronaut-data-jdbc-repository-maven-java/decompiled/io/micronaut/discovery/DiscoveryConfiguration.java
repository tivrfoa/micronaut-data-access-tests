package io.micronaut.discovery;

import io.micronaut.core.util.Toggleable;

public abstract class DiscoveryConfiguration implements Toggleable {
   public static final String PREFIX = "discovery";
   public static final boolean DEFAULT_ENABLED = true;
   private boolean enabled = true;

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
