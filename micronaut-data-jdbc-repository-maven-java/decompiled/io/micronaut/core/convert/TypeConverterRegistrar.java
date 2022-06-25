package io.micronaut.core.convert;

import io.micronaut.core.annotation.Indexed;

@Indexed(TypeConverterRegistrar.class)
public interface TypeConverterRegistrar {
   void register(ConversionService<?> conversionService);
}
