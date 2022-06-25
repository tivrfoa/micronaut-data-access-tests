package io.micronaut.core.value;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.type.Argument;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface PropertyResolver extends ValueResolver<String> {
   boolean containsProperty(@NonNull String name);

   boolean containsProperties(@NonNull String name);

   @NonNull
   <T> Optional<T> getProperty(@NonNull String name, @NonNull ArgumentConversionContext<T> conversionContext);

   @NonNull
   default Collection<String> getPropertyEntries(@NonNull String name) {
      return Collections.emptySet();
   }

   @NonNull
   default <T> Optional<T> getProperty(@NonNull String name, @NonNull Argument<T> argument) {
      return this.getProperty(name, ConversionContext.of(argument));
   }

   @NonNull
   default Map<String, Object> getProperties(@NonNull String name) {
      return this.getProperties(name, null);
   }

   @NonNull
   default Map<String, Object> getProperties(@Nullable String name, @Nullable StringConvention keyFormat) {
      return Collections.emptyMap();
   }

   @NonNull
   default <T> Optional<T> getProperty(@NonNull String name, @NonNull Class<T> requiredType, @NonNull ConversionContext context) {
      return this.getProperty(name, context.with(Argument.of(requiredType)));
   }

   @NonNull
   default <T> Optional<T> get(@NonNull String name, @NonNull ArgumentConversionContext<T> conversionContext) {
      return this.getProperty(name, conversionContext);
   }

   @NonNull
   default <T> Optional<T> getProperty(@NonNull String name, @NonNull Class<T> requiredType) {
      return this.getProperty(name, requiredType, ConversionContext.DEFAULT);
   }

   @Nullable
   default <T> T getProperty(@NonNull String name, @NonNull Class<T> requiredType, @Nullable T defaultValue) {
      return (T)this.getProperty(name, requiredType).orElse(defaultValue);
   }

   @NonNull
   default <T> T getRequiredProperty(@NonNull String name, @NonNull Class<T> requiredType) throws PropertyNotFoundException {
      return (T)this.getProperty(name, requiredType).orElseThrow(() -> new PropertyNotFoundException(name, requiredType));
   }

   static String nameOf(String... path) {
      return String.join(".", path);
   }
}
