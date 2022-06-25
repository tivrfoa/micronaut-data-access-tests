package io.micronaut.http.simple;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.ConvertibleMultiValuesMap;
import io.micronaut.http.MutableHttpParameters;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleHttpParameters implements MutableHttpParameters {
   private final Map<CharSequence, List<String>> valuesMap;
   private final ConvertibleMultiValues<String> values;

   public SimpleHttpParameters(Map<CharSequence, List<String>> values, ConversionService conversionService) {
      this.valuesMap = values;
      this.values = new ConvertibleMultiValuesMap(this.valuesMap, conversionService);
   }

   public SimpleHttpParameters(ConversionService conversionService) {
      this(new LinkedHashMap(), conversionService);
   }

   @Override
   public Set<String> names() {
      return this.values.names();
   }

   @Override
   public Collection<List<String>> values() {
      return this.values.values();
   }

   @Override
   public List<String> getAll(CharSequence name) {
      return this.values.getAll(name);
   }

   public String get(CharSequence name) {
      return (String)this.values.get(name);
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      return this.values.get(name, conversionContext);
   }

   @Override
   public MutableHttpParameters add(CharSequence name, List<CharSequence> values) {
      this.valuesMap.put(name, values.stream().map(v -> v == null ? null : v.toString()).collect(Collectors.toList()));
      return this;
   }
}
