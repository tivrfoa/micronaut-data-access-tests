package io.micronaut.management.endpoint;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import java.util.Optional;

@EachProperty("endpoints")
public class EndpointConfiguration {
   public static final String PREFIX = "endpoints";
   private Boolean enabled;
   private Boolean sensitive;
   private final String id;
   private EndpointDefaultConfiguration defaultConfiguration;

   public EndpointConfiguration(@Parameter String id, EndpointDefaultConfiguration defaultConfiguration) {
      this.id = id;
      this.defaultConfiguration = defaultConfiguration;
   }

   public String getId() {
      return this.id;
   }

   public Optional<Boolean> isEnabled() {
      return this.enabled != null ? Optional.of(this.enabled) : this.defaultConfiguration.isEnabled();
   }

   public Optional<Boolean> isSensitive() {
      return this.sensitive != null ? Optional.of(this.sensitive) : this.defaultConfiguration.isSensitive();
   }

   public void setEnabled(Boolean enabled) {
      this.enabled = enabled;
   }

   public void setSensitive(Boolean sensitive) {
      this.sensitive = sensitive;
   }
}
