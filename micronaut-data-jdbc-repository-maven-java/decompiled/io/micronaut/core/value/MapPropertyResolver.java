package io.micronaut.core.value;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapPropertyResolver implements PropertyResolver {
   private final Map<String, Object> map;
   private final ConversionService<?> conversionService;

   public MapPropertyResolver(Map<String, Object> map) {
      this.map = map;
      this.conversionService = ConversionService.SHARED;
   }

   public MapPropertyResolver(Map<String, Object> map, ConversionService conversionService) {
      this.map = map;
      this.conversionService = conversionService;
   }

   @Override
   public boolean containsProperty(String name) {
      return this.map.containsKey(name);
   }

   @Override
   public boolean containsProperties(String name) {
      return this.map.keySet().stream().anyMatch(k -> k.startsWith(name));
   }

   @Override
   public <T> Optional<T> getProperty(String name, ArgumentConversionContext<T> conversionContext) {
      Object value = this.map.get(name);
      return this.conversionService.convert(value, conversionContext);
   }

   @NonNull
   @Override
   public Collection<String> getPropertyEntries(@NonNull String name) {
      if (StringUtils.isNotEmpty(name)) {
         String prefix = name + ".";
         return (Collection<String>)this.map.keySet().stream().filter(k -> k.startsWith(prefix)).map(k -> {
            String withoutPrefix = k.substring(prefix.length());
            int i = withoutPrefix.indexOf(46);
            return i > -1 ? withoutPrefix.substring(0, i) : withoutPrefix;
         }).collect(Collectors.toList());
      } else {
         return Collections.emptySet();
      }
   }
}
