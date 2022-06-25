package io.micronaut.web.router.version;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import jakarta.inject.Singleton;

@Requirements({@Requires(
   beans = {RoutesVersioningConfiguration.class}
), @Requires(
   property = "micronaut.router.versioning.default-version"
)})
@Singleton
public class ConfigurationDefaultVersionProvider implements DefaultVersionProvider {
   private final String defaultVersion;

   public ConfigurationDefaultVersionProvider(RoutesVersioningConfiguration routesVersioningConfiguration) {
      if (!routesVersioningConfiguration.getDefaultVersion().isPresent()) {
         throw new ConfigurationException("this bean should not be loaded if micronaut.router.versioning.default-versionis null");
      } else {
         this.defaultVersion = (String)routesVersioningConfiguration.getDefaultVersion().get();
      }
   }

   @Override
   public String resolveDefaultVersion() {
      return this.defaultVersion;
   }
}
