package io.micronaut.core.convert.value;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ConvertibleMultiValuesMap<V> implements ConvertibleMultiValues<V> {
   public static final ConvertibleMultiValues EMPTY = new ConvertibleMultiValuesMap(Collections.emptyMap());
   protected final Map<CharSequence, List<V>> values;
   private final ConversionService<?> conversionService;

   public ConvertibleMultiValuesMap() {
      this(new LinkedHashMap(), ConversionService.SHARED);
   }

   public ConvertibleMultiValuesMap(Map<CharSequence, List<V>> values) {
      this(values, ConversionService.SHARED);
   }

   public ConvertibleMultiValuesMap(Map<CharSequence, List<V>> values, ConversionService<?> conversionService) {
      this.values = this.wrapValues(values);
      this.conversionService = conversionService;
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      List<V> values = this.getAll(name);
      if (!values.isEmpty()) {
         boolean hasSingleEntry = values.size() == 1;
         if (hasSingleEntry) {
            V v = (V)values.iterator().next();
            return conversionContext.getArgument().getType().isInstance(v) ? Optional.of(v) : this.conversionService.convert(v, conversionContext);
         } else {
            return this.conversionService.convert(values, conversionContext);
         }
      } else {
         Argument<T> argument = conversionContext.getArgument();
         if (Map.class.isAssignableFrom(argument.getType())) {
            Argument valueType = (Argument)argument.getTypeVariable("V").orElse(Argument.OBJECT_ARGUMENT);
            Map map = this.subMap(name.toString(), valueType);
            return map.isEmpty() ? Optional.empty() : Optional.of(map);
         } else {
            return Optional.empty();
         }
      }
   }

   @Override
   public List<V> getAll(CharSequence name) {
      List<V> value = (List)this.values.get(name);
      return value != null ? Collections.unmodifiableList(value) : Collections.emptyList();
   }

   @Override
   public V get(CharSequence name) {
      List<V> all = this.getAll(name);
      return (V)(all.isEmpty() ? null : all.get(0));
   }

   @Override
   public Set<String> names() {
      return (Set<String>)this.values.keySet().stream().map(CharSequence::toString).collect(Collectors.toCollection(LinkedHashSet::new));
   }

   @Override
   public Collection<List<V>> values() {
      return Collections.unmodifiableCollection(this.values.values());
   }

   protected Map<CharSequence, List<V>> wrapValues(Map<CharSequence, List<V>> values) {
      return Collections.unmodifiableMap(values);
   }
}
