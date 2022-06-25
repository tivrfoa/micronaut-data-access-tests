package io.micronaut.context.env;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.util.Toggleable;
import java.util.Optional;

public interface PropertySourceLoader extends Toggleable, PropertySourceLocator, PropertySourceReader {
   @Override
   default Optional<PropertySource> load(Environment environment) {
      return this.load("application", environment);
   }

   Optional<PropertySource> load(String resourceName, ResourceLoader resourceLoader);

   Optional<PropertySource> loadEnv(String resourceName, ResourceLoader resourceLoader, ActiveEnvironment activeEnvironment);
}
