package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;

@Internal
public interface EnvironmentConfigurable {
   default void configure(Environment environment) {
   }

   default boolean hasPropertyExpressions() {
      return true;
   }
}
