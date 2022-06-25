package io.micronaut.core.convert;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public interface ErrorsContext extends Iterable<ConversionError> {
   default void reject(Exception exception) {
   }

   default void reject(Object value, Exception exception) {
   }

   default Iterator<ConversionError> iterator() {
      return Collections.emptyIterator();
   }

   default Optional<ConversionError> getLastError() {
      return Optional.empty();
   }

   default boolean hasErrors() {
      return this.getLastError().isPresent();
   }
}
