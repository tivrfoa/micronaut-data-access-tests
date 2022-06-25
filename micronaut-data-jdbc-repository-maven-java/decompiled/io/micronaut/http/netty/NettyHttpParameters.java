package io.micronaut.http.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.ConvertibleMultiValuesMap;
import io.micronaut.http.MutableHttpParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Internal
public class NettyHttpParameters implements MutableHttpParameters {
   private final LinkedHashMap<CharSequence, List<String>> valuesMap;
   private final ConvertibleMultiValues<String> values;
   private final BiConsumer<CharSequence, List<String>> onChange;

   public NettyHttpParameters(
      Map<String, List<String>> parameters, ConversionService<?> conversionService, @Nullable BiConsumer<CharSequence, List<String>> onChange
   ) {
      this.valuesMap = new LinkedHashMap(parameters.size());
      this.values = new ConvertibleMultiValuesMap(this.valuesMap, conversionService);
      this.onChange = onChange;

      for(Entry<String, List<String>> entry : parameters.entrySet()) {
         this.valuesMap.put(entry.getKey(), Collections.unmodifiableList((List)entry.getValue()));
      }

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
      List<String> valueList = (List)this.valuesMap.compute(name, (key, val) -> {
         List<String> newValues = (List)values.stream().map(v -> v == null ? null : v.toString()).collect(Collectors.toList());
         if (val == null) {
            val = new ArrayList(newValues.size());
         } else {
            val = new ArrayList(val);
         }

         val.addAll(newValues);
         return Collections.unmodifiableList(val);
      });
      if (this.onChange != null) {
         this.onChange.accept(name, valueList);
      }

      return this;
   }
}
