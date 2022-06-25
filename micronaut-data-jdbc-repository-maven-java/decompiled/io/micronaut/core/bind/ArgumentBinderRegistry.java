package io.micronaut.core.bind;

import io.micronaut.core.type.Argument;
import java.util.Optional;

public interface ArgumentBinderRegistry<S> {
   default <T, ST> void addRequestArgumentBinder(ArgumentBinder<T, ST> binder) {
      throw new UnsupportedOperationException("Binder registry is not mutable");
   }

   <T> Optional<ArgumentBinder<T, S>> findArgumentBinder(Argument<T> argument, S source);
}
