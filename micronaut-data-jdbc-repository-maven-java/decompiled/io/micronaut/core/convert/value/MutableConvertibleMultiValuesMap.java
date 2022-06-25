package io.micronaut.core.convert.value;

import io.micronaut.core.convert.ConversionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MutableConvertibleMultiValuesMap<V> extends ConvertibleMultiValuesMap<V> implements MutableConvertibleMultiValues<V> {
   public MutableConvertibleMultiValuesMap() {
   }

   public MutableConvertibleMultiValuesMap(Map<CharSequence, List<V>> values) {
      super(values, ConversionService.SHARED);
   }

   public MutableConvertibleMultiValuesMap(Map<CharSequence, List<V>> values, ConversionService<?> conversionService) {
      super(values, conversionService);
   }

   @Override
   public MutableConvertibleMultiValues<V> add(CharSequence key, V value) {
      ((List)this.values.computeIfAbsent(key, k -> new ArrayList())).add(value);
      return this;
   }

   public MutableConvertibleValues<List<V>> put(CharSequence key, List<V> value) {
      if (value != null) {
         this.values.put(key, value);
      }

      return this;
   }

   @Override
   public MutableConvertibleValues<List<V>> remove(CharSequence key) {
      this.values.remove(key);
      return this;
   }

   @Override
   public MutableConvertibleMultiValues<V> remove(CharSequence key, V value) {
      ((List)this.values.computeIfAbsent(key, k -> new ArrayList())).remove(value);
      return this;
   }

   @Override
   public MutableConvertibleMultiValues<V> clear() {
      this.values.clear();
      return this;
   }

   @Override
   protected Map<CharSequence, List<V>> wrapValues(Map<CharSequence, List<V>> values) {
      return values;
   }
}
