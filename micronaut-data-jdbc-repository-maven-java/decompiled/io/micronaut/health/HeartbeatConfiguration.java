package io.micronaut.health;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.Toggleable;
import java.time.Duration;

@ConfigurationProperties("micronaut.heartbeat")
public class HeartbeatConfiguration implements Toggleable {
   public static final boolean DEFAULT_ENABLED = true;
   public static final int DEFAULT_INTERVAL_SECONDS = 15;
   public static final String PREFIX = "micronaut.heartbeat";
   public static final String ENABLED = "micronaut.heartbeat.enabled";
   private Duration interval = Duration.ofSeconds(15L);
   private boolean enabled = true;

   public Duration getInterval() {
      return this.interval;
   }

   public void setInterval(Duration interval) {
      this.interval = interval;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
