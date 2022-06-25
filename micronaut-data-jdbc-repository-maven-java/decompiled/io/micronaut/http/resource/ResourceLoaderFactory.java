package io.micronaut.http.resource;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.file.DefaultFileSystemResourceLoader;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.io.scan.DefaultClassPathResourceLoader;
import jakarta.inject.Singleton;
import java.util.List;

@Factory
@BootstrapContextCompatible
public class ResourceLoaderFactory {
   private final ClassLoader classLoader;

   public ResourceLoaderFactory(Environment environment) {
      this.classLoader = environment.getClassLoader();
   }

   @Singleton
   @BootstrapContextCompatible
   @NonNull
   protected ClassPathResourceLoader getClassPathResourceLoader() {
      return new DefaultClassPathResourceLoader(this.classLoader);
   }

   @Singleton
   @BootstrapContextCompatible
   @NonNull
   protected FileSystemResourceLoader fileSystemResourceLoader() {
      return new DefaultFileSystemResourceLoader();
   }

   @Singleton
   @BootstrapContextCompatible
   @Indexed(ResourceResolver.class)
   @NonNull
   protected ResourceResolver resourceResolver(@NonNull List<ResourceLoader> resourceLoaders) {
      return new ResourceResolver(resourceLoaders);
   }
}
