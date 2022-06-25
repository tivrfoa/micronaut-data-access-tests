package io.micronaut.core.naming;

import java.util.Optional;

public interface NameResolver {
   Optional<String> resolveName();
}
