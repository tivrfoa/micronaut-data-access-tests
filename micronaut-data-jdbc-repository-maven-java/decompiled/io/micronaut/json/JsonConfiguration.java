package io.micronaut.json;

import io.micronaut.core.annotation.Internal;

@Internal
public interface JsonConfiguration {
   boolean isAlwaysSerializeErrorsAsList();

   int getArraySizeThreshold();
}
