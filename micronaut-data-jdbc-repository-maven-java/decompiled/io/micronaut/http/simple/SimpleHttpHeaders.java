package io.micronaut.http.simple;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleMultiValuesMap;
import io.micronaut.http.MutableHttpHeaders;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SimpleHttpHeaders implements MutableHttpHeaders {
   private final MutableConvertibleMultiValuesMap<String> headers = new MutableConvertibleMultiValuesMap();
   private final ConversionService conversionService;

   public SimpleHttpHeaders(Map<String, String> headers, ConversionService conversionService) {
      headers.forEach(this.headers::add);
      this.conversionService = conversionService;
   }

   public SimpleHttpHeaders(ConversionService conversionService) {
      this(new LinkedHashMap(), conversionService);
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      Optional<String> value = this.headers.getFirst(name.toString());
      return value.flatMap(it -> this.conversionService.convert(it, conversionContext));
   }

   @Override
   public List<String> getAll(CharSequence name) {
      return this.headers.getAll(name.toString());
   }

   @Override
   public Set<String> names() {
      return this.headers.names();
   }

   @Override
   public Collection<List<String>> values() {
      return this.headers.values();
   }

   public String get(CharSequence name) {
      return (String)this.headers.get(name.toString());
   }

   @Override
   public MutableHttpHeaders add(CharSequence header, CharSequence value) {
      if (value != null) {
         this.headers.add(header.toString(), value.toString());
      }

      return this;
   }

   @Override
   public MutableHttpHeaders remove(CharSequence header) {
      this.headers.remove(header.toString());
      return this;
   }
}
