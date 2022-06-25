package io.micronaut.core.convert;

import java.util.Optional;

public interface ConversionError {
   Exception getCause();

   default Optional<Object> getOriginalValue() {
      return Optional.empty();
   }
}
