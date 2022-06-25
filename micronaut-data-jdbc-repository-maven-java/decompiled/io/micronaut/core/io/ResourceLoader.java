package io.micronaut.core.io;

import io.micronaut.core.annotation.Indexed;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

@Indexed(ResourceLoader.class)
public interface ResourceLoader {
   Optional<InputStream> getResourceAsStream(String path);

   Optional<URL> getResource(String path);

   Stream<URL> getResources(String name);

   boolean supportsPrefix(String path);

   ResourceLoader forBase(String basePath);
}
