package io.micronaut.core.value;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import java.util.Map;
import java.util.Optional;

class MapValueResolver<K extends CharSequence> implements ValueResolver<K> {
   private final Map<K, ?> map;

   MapValueResolver(Map<K, ?> map) {
      this.map = map;
   }

   @Override
   public <T> Optional<T> get(K name, ArgumentConversionContext<T> conversionContext) {
      Object v = this.map.get(name);
      if (v == null) {
         return Optional.empty();
      } else {
         Argument<T> argument = conversionContext.getArgument();
         return argument.getType().isInstance(v) ? Optional.of(v) : ConversionService.SHARED.convert(v, conversionContext);
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MapValueResolver that = (MapValueResolver)o;
         return this.map.equals(that.map);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.map.hashCode();
   }
}
