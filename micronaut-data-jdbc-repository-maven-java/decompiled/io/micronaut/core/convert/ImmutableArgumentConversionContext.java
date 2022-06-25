package io.micronaut.core.convert;

import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;

public interface ImmutableArgumentConversionContext<T> extends ArgumentConversionContext<T> {
   static <T> ImmutableArgumentConversionContext<T> of(Argument<T> argument) {
      ArgumentUtils.requireNonNull("argument", argument);
      return () -> argument;
   }

   static <T> ImmutableArgumentConversionContext<T> of(Class<T> type) {
      return of(Argument.of(type));
   }
}
