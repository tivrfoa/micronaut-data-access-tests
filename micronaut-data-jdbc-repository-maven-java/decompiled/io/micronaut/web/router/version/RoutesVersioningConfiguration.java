package io.micronaut.web.router.version;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;
import java.util.Optional;

@ConfigurationProperties("micronaut.router.versioning")
@Requires(
   property = "micronaut.router.versioning.enabled",
   value = "true"
)
public class RoutesVersioningConfiguration implements Toggleable {
   public static final String PREFIX = "micronaut.router.versioning";
   private static final boolean DEFAULT_ENABLED = false;
   private boolean enabled = false;
   private String defaultVersion;

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public Optional<String> getDefaultVersion() {
      return Optional.ofNullable(this.defaultVersion);
   }

   public void setDefaultVersion(@Nullable String defaultVersion) {
      this.defaultVersion = defaultVersion;
   }
}
