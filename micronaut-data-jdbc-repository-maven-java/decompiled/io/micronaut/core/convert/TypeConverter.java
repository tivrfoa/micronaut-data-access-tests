package io.micronaut.core.convert;

import io.micronaut.core.annotation.Indexed;
import java.util.Optional;
import java.util.function.Function;

@Indexed(TypeConverter.class)
public interface TypeConverter<S, T> {
   default Optional<T> convert(S object, Class<T> targetType) {
      return this.convert(object, targetType, ConversionContext.DEFAULT);
   }

   Optional<T> convert(S object, Class<T> targetType, ConversionContext context);

   static <ST, TT> TypeConverter<ST, TT> of(Class<ST> sourceType, Class<TT> targetType, Function<ST, TT> converter) {
      return (object, targetType1, context) -> Optional.ofNullable(converter.apply(object));
   }
}
