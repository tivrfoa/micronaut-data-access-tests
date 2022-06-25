package io.micronaut.core.convert.value;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ConvertibleValuesMap<V> implements ConvertibleValues<V> {
   protected final Map<? extends CharSequence, V> map;
   private final ConversionService<?> conversionService;

   public ConvertibleValuesMap() {
      this(new LinkedHashMap(), ConversionService.SHARED);
   }

   public ConvertibleValuesMap(Map<? extends CharSequence, V> map) {
      this(map, ConversionService.SHARED);
   }

   public ConvertibleValuesMap(Map<? extends CharSequence, V> map, ConversionService<?> conversionService) {
      this.map = map;
      this.conversionService = conversionService;
   }

   @Nullable
   @Override
   public V getValue(CharSequence name) {
      return (V)(name != null ? this.map.get(name) : null);
   }

   @Override
   public boolean contains(String name) {
      return name != null && this.map.containsKey(name);
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      V value = (V)this.map.get(name);
      return value != null ? this.conversionService.convert(value, conversionContext) : Optional.empty();
   }

   @Override
   public Set<String> names() {
      return (Set<String>)this.map.keySet().stream().map(CharSequence::toString).collect(Collectors.toSet());
   }

   @Override
   public Collection<V> values() {
      return Collections.unmodifiableCollection(this.map.values());
   }

   public static <V> ConvertibleValues<V> empty() {
      return EMPTY;
   }
}
