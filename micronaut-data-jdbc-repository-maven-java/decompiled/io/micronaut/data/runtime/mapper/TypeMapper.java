package io.micronaut.data.runtime.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.data.exceptions.DataAccessException;

public interface TypeMapper<D, R> {
   @NonNull
   R map(@NonNull D object, @NonNull Class<R> type) throws DataAccessException;

   @Nullable
   Object read(@NonNull D object, @NonNull String name);

   @Nullable
   default Object read(@NonNull D object, @NonNull Argument<?> argument) {
      return this.read(object, argument.getName());
   }

   @NonNull
   default ConversionService<?> getConversionService() {
      return ConversionService.SHARED;
   }
}
