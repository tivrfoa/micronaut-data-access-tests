package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ApplicationContextConfiguration extends BeanContextConfiguration {
   @NonNull
   List<String> getEnvironments();

   default Optional<Boolean> getDeduceEnvironments() {
      return Optional.empty();
   }

   default List<String> getDefaultEnvironments() {
      return Collections.emptyList();
   }

   default boolean isEnvironmentPropertySource() {
      return true;
   }

   @Nullable
   default List<String> getEnvironmentVariableIncludes() {
      return null;
   }

   @Nullable
   default List<String> getEnvironmentVariableExcludes() {
      return null;
   }

   @NonNull
   default ConversionService<?> getConversionService() {
      return ConversionService.SHARED;
   }

   @NonNull
   default ClassPathResourceLoader getResourceLoader() {
      return ClassPathResourceLoader.defaultLoader(this.getClassLoader());
   }

   @Nullable
   default List<String> getOverrideConfigLocations() {
      return null;
   }

   default boolean isBannerEnabled() {
      return true;
   }

   @Nullable
   default Boolean isBootstrapEnvironmentEnabled() {
      return null;
   }
}
