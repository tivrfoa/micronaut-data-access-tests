package io.micronaut.web.router.version.resolution;

import java.util.Optional;

public interface VersionResolver<T, R> {
   Optional<R> resolve(T object);
}
