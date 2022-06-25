package io.micronaut.context.env;

import java.util.Optional;

public interface PropertySourceLocator {
   Optional<PropertySource> load(Environment environment);
}
