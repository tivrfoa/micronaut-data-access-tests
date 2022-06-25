package io.micronaut.context.env;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import java.util.Optional;

public interface PropertyPlaceholderResolver {
   Optional<String> resolvePlaceholders(String str);

   @NonNull
   default String getPrefix() {
      return "${";
   }

   @NonNull
   default String resolveRequiredPlaceholders(String str) throws ConfigurationException {
      return (String)this.resolvePlaceholders(str).orElseThrow(() -> new ConfigurationException("Unable to resolve placeholders for property: " + str));
   }

   @NonNull
   default <T> T resolveRequiredPlaceholder(String str, Class<T> type) throws ConfigurationException {
      throw new ConfigurationException("Unsupported operation");
   }
}
