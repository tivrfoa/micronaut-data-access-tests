package io.micronaut.core.value;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OptionalMultiValues<V> extends OptionalValues<List<V>> {
   OptionalMultiValues EMPTY_VALUES = of(Collections.emptyMap());

   default Optional<V> getFirst(CharSequence name) {
      Optional<List<V>> list = this.get(name);
      return list.flatMap(v -> !v.isEmpty() ? Optional.ofNullable(v.get(0)) : Optional.empty());
   }

   static <T> OptionalMultiValues<T> empty() {
      return EMPTY_VALUES;
   }

   static <T> OptionalMultiValues<T> of(Map<CharSequence, List<T>> values) {
      return new OptionalMultiValuesMap<>(List.class, values);
   }
}
