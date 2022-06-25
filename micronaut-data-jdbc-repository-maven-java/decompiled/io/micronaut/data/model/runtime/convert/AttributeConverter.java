package io.micronaut.data.model.runtime.convert;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionContext;

@Indexed(AttributeConverter.class)
public interface AttributeConverter<X, Y> {
   @Nullable
   Y convertToPersistedValue(@Nullable X entityValue, @NonNull ConversionContext context);

   @Nullable
   X convertToEntityValue(@Nullable Y persistedValue, @NonNull ConversionContext context);
}
