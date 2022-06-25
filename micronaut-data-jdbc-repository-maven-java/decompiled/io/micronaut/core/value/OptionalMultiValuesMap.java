package io.micronaut.core.value;

import java.util.List;
import java.util.Map;

class OptionalMultiValuesMap<V> extends OptionalValuesMap<List<V>> implements OptionalMultiValues<V> {
   public OptionalMultiValuesMap(Class<?> type, Map<CharSequence, ?> values) {
      super(type, values);
   }
}
