package io.micronaut.core.convert.value;

import io.micronaut.core.convert.ConversionService;
import java.util.Map;

public class MutableConvertibleValuesMap<V> extends ConvertibleValuesMap<V> implements MutableConvertibleValues<V> {
   public MutableConvertibleValuesMap() {
   }

   public MutableConvertibleValuesMap(Map<? extends CharSequence, V> map) {
      super(map);
   }

   public MutableConvertibleValuesMap(Map<? extends CharSequence, V> map, ConversionService<?> conversionService) {
      super(map, conversionService);
   }

   public String toString() {
      return this.map.toString();
   }

   @Override
   public MutableConvertibleValues<V> put(CharSequence key, V value) {
      if (value == null) {
         this.map.remove(key);
      } else {
         this.map.put(key, value);
      }

      return this;
   }

   @Override
   public MutableConvertibleValues<V> remove(CharSequence key) {
      this.map.remove(key);
      return this;
   }

   @Override
   public MutableConvertibleValues<V> clear() {
      this.map.clear();
      return this;
   }
}
