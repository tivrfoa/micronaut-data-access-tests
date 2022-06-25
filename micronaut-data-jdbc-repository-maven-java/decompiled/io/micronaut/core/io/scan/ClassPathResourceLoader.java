package io.micronaut.core.io.scan;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.ResourceLoader;

public interface ClassPathResourceLoader extends ResourceLoader {
   ClassLoader getClassLoader();

   @Override
   default boolean supportsPrefix(String path) {
      return path.startsWith("classpath:");
   }

   static ClassPathResourceLoader defaultLoader(@Nullable ClassLoader classLoader) {
      if (classLoader == null) {
         classLoader = Thread.currentThread().getContextClassLoader();
      }

      if (classLoader == null) {
         classLoader = ClassPathResourceLoader.class.getClassLoader();
      }

      if (classLoader == null) {
         classLoader = ClassLoader.getSystemClassLoader();
      }

      return new DefaultClassPathResourceLoader(classLoader);
   }
}
