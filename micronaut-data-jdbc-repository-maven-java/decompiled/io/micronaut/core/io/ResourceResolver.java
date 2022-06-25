package io.micronaut.core.io;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.util.ArgumentUtils;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ResourceResolver {
   private final List<ResourceLoader> resourceLoaders;

   public ResourceResolver(@NonNull ResourceLoader[] resourceLoaders) {
      this(Arrays.asList(resourceLoaders));
   }

   public ResourceResolver(@NonNull List<ResourceLoader> resourceLoaders) {
      ArgumentUtils.requireNonNull("resourceLoaders", resourceLoaders);
      this.resourceLoaders = resourceLoaders;
   }

   public ResourceResolver() {
      this(new ResourceLoader[]{ClassPathResourceLoader.defaultLoader(ResourceResolver.class.getClassLoader()), FileSystemResourceLoader.defaultLoader()});
   }

   @NonNull
   public <T extends ResourceLoader> Optional<T> getLoader(@NonNull Class<T> resolverType) {
      ArgumentUtils.requireNonNull("resolverType", (T)resolverType);
      return this.resourceLoaders.stream().filter(rl -> resolverType.isAssignableFrom(rl.getClass())).map(rl -> rl).findFirst();
   }

   @NonNull
   public Optional<ResourceLoader> getSupportingLoader(@NonNull String prefix) {
      ArgumentUtils.requireNonNull("prefix", prefix);
      return this.resourceLoaders.stream().filter(rl -> rl.supportsPrefix(prefix)).findFirst();
   }

   @NonNull
   public Optional<ResourceLoader> getLoaderForBasePath(@NonNull String basePath) {
      ArgumentUtils.requireNonNull("basePath", basePath);
      Optional<ResourceLoader> resourceLoader = this.getSupportingLoader(basePath);
      return resourceLoader.map(rl -> rl.forBase(basePath));
   }

   @NonNull
   public Optional<InputStream> getResourceAsStream(@NonNull String path) {
      ArgumentUtils.requireNonNull("path", path);
      Optional<ResourceLoader> resourceLoader = this.getSupportingLoader(path);
      return resourceLoader.isPresent() ? ((ResourceLoader)resourceLoader.get()).getResourceAsStream(path) : Optional.empty();
   }

   @NonNull
   public Optional<URL> getResource(@NonNull String path) {
      ArgumentUtils.requireNonNull("path", path);
      Optional<ResourceLoader> resourceLoader = this.getSupportingLoader(path);
      return resourceLoader.isPresent() ? ((ResourceLoader)resourceLoader.get()).getResource(path) : Optional.empty();
   }

   @NonNull
   public Stream<URL> getResources(@NonNull String path) {
      ArgumentUtils.requireNonNull("path", path);
      Optional<ResourceLoader> resourceLoader = this.getSupportingLoader(path);
      return resourceLoader.isPresent() ? ((ResourceLoader)resourceLoader.get()).getResources(path) : Stream.empty();
   }
}
