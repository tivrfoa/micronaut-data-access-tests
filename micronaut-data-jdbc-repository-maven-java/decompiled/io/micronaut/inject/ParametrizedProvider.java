package io.micronaut.inject;

import jakarta.inject.Provider;
import java.util.Map;

public interface ParametrizedProvider<T> extends Provider<T> {
   T get(Map<String, Object> argumentValues);

   T get(Object... argumentValues);

   @Override
   default T get() {
      return this.get((Map<String, Object>)null);
   }
}
