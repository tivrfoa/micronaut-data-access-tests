package io.micronaut.context;

import io.micronaut.context.annotation.ConfigurationReader;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

public interface BeanContextConfiguration {
   default boolean isAllowEmptyProviders() {
      return false;
   }

   @NonNull
   default ClassLoader getClassLoader() {
      return ApplicationContextConfiguration.class.getClassLoader();
   }

   default boolean isEagerInitSingletons() {
      for(Class<? extends Annotation> ann : this.getEagerInitAnnotated()) {
         if (ann == Singleton.class || ann.getName().equals("javax.inject.Singleton")) {
            return true;
         }
      }

      return false;
   }

   default boolean isEagerInitConfiguration() {
      return this.getEagerInitAnnotated().contains(ConfigurationReader.class);
   }

   default Set<Class<? extends Annotation>> getEagerInitAnnotated() {
      return Collections.emptySet();
   }
}
