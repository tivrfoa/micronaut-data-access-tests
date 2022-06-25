package io.micronaut.web.router.version.resolution;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.Toggleable;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties("micronaut.router.versioning.header")
@Requires(
   property = "micronaut.router.versioning.header.enabled",
   value = "true"
)
public class HeaderVersionResolverConfiguration implements Toggleable {
   public static final String PREFIX = "micronaut.router.versioning.header";
   public static final String DEFAULT_HEADER_NAME = "X-API-VERSION";
   private boolean enabled;
   private List<String> names = Collections.singletonList("X-API-VERSION");

   public List<String> getNames() {
      return this.names;
   }

   public void setNames(List<String> names) {
      this.names = names;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
