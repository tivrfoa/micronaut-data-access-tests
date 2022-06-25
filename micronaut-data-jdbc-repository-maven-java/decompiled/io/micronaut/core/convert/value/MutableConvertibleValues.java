package io.micronaut.core.convert.value;

import io.micronaut.core.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;

public interface MutableConvertibleValues<V> extends ConvertibleValues<V> {
   MutableConvertibleValues<V> put(CharSequence key, @Nullable V value);

   MutableConvertibleValues<V> remove(CharSequence key);

   MutableConvertibleValues<V> clear();

   default MutableConvertibleValues<V> putAll(Map<CharSequence, V> values) {
      if (values != null) {
         for(Entry<CharSequence, V> entry : values.entrySet()) {
            this.put((CharSequence)entry.getKey(), (V)entry.getValue());
         }
      }

      return this;
   }

   default MutableConvertibleValues<V> putAll(ConvertibleValues<V> values) {
      if (values != null) {
         for(Entry<String, V> entry : values) {
            this.put((CharSequence)entry.getKey(), (V)entry.getValue());
         }
      }

      return this;
   }

   static <T> MutableConvertibleValues<T> of(Map<? extends CharSequence, T> values) {
      return values == null ? new MutableConvertibleValuesMap<>() : new MutableConvertibleValuesMap<>(values);
   }
}
