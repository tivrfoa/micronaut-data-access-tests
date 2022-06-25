package io.micronaut.discovery.registration;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public abstract class RegistrationConfiguration implements Toggleable {
   public static final String PREFIX = "registration";
   public static final boolean DEFAULT_ENABLED = true;
   public static final int DEFAULT_RETRY_COUNT = -1;
   public static final int DEFAULT_RETRYDELAY_SECONDS = 1;
   public static final boolean DEFAULT_DEREGISTER = true;
   public static final boolean DEFAULT_FAILFAST = true;
   private String healthPath;
   private int retryCount = -1;
   private Duration timeout;
   private Duration retryDelay = Duration.of(1L, ChronoUnit.SECONDS);
   private boolean failFast = true;
   private boolean enabled = true;
   private boolean deregister = true;
   private boolean preferIpAddress = false;
   private String ipAddr;

   public Optional<String> getIpAddr() {
      return Optional.ofNullable(this.ipAddr);
   }

   public void setIpAddr(@Nullable String ipAddr) {
      this.ipAddr = ipAddr;
   }

   public boolean isPreferIpAddress() {
      return this.preferIpAddress;
   }

   public void setPreferIpAddress(boolean preferIpAddress) {
      this.preferIpAddress = preferIpAddress;
   }

   public Optional<Duration> getTimeout() {
      return Optional.ofNullable(this.timeout);
   }

   public void setTimeout(Duration timeout) {
      this.timeout = timeout;
   }

   public boolean isFailFast() {
      return this.failFast;
   }

   public void setFailFast(boolean failFast) {
      this.failFast = failFast;
   }

   public boolean isDeregister() {
      return this.deregister;
   }

   public void setDeregister(boolean deregister) {
      this.deregister = deregister;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public int getRetryCount() {
      return this.retryCount;
   }

   public void setRetryCount(int retryCount) {
      this.retryCount = retryCount;
   }

   public Duration getRetryDelay() {
      return this.retryDelay;
   }

   public void setRetryDelay(Duration retryDelay) {
      this.retryDelay = retryDelay;
   }

   public Optional<String> getHealthPath() {
      return Optional.ofNullable(this.healthPath);
   }

   public void setHealthPath(String healthPath) {
      this.healthPath = healthPath;
   }
}
