package io.micronaut.core.value;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.type.Argument;
import java.util.Optional;

public interface ValueResolver<K extends CharSequence> {
   <T> Optional<T> get(K name, ArgumentConversionContext<T> conversionContext);

   default <T> Optional<T> get(K name, Class<T> requiredType) {
      return this.get(name, ConversionContext.of(Argument.of(requiredType)));
   }

   default <T> Optional<T> get(K name, Argument<T> requiredType) {
      return this.get(name, ConversionContext.of(requiredType));
   }

   default <T> T get(K name, Class<T> requiredType, T defaultValue) {
      return (T)this.get(name, requiredType).orElse(defaultValue);
   }
}
