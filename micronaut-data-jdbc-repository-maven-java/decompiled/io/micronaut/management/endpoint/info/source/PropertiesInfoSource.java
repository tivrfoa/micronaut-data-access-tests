package io.micronaut.management.endpoint.info.source;

import io.micronaut.context.env.PropertiesPropertySourceLoader;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.management.endpoint.info.InfoSource;
import java.util.Optional;

public interface PropertiesInfoSource extends InfoSource {
   default Optional<PropertySource> retrievePropertiesPropertySource(String path, String prefix, String extension, ResourceResolver resourceResolver) {
      StringBuilder pathBuilder = new StringBuilder();
      if (prefix != null && !path.startsWith(prefix)) {
         pathBuilder.append(prefix);
      }

      if (extension != null && path.endsWith(extension)) {
         int index = path.indexOf(extension);
         pathBuilder.append(path, 0, index);
      } else {
         pathBuilder.append(path);
      }

      String propertiesPath = pathBuilder.toString();
      Optional<ResourceLoader> resourceLoader = resourceResolver.getSupportingLoader(propertiesPath);
      if (resourceLoader.isPresent()) {
         PropertiesPropertySourceLoader propertySourceLoader = new PropertiesPropertySourceLoader();
         return propertySourceLoader.load(propertiesPath, (ResourceLoader)resourceLoader.get());
      } else {
         return Optional.empty();
      }
   }
}
