package io.micronaut.data.model.runtime;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.runtime.convert.AttributeConverter;

public interface AttributeConverterRegistry {
   @NonNull
   AttributeConverter<Object, Object> getConverter(@NonNull Class<?> converterClass);
}
