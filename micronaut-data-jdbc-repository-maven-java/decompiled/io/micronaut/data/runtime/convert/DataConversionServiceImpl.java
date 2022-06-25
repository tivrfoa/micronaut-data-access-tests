package io.micronaut.data.runtime.convert;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.DefaultConversionService;
import io.micronaut.core.convert.TypeConverter;
import java.util.Optional;
import java.util.function.Function;

@Internal
final class DataConversionServiceImpl implements DataConversionService<DataConversionServiceImpl> {
   private final DefaultConversionService internalConversionService = new DefaultConversionService();
   private final ConversionService sharedConversionService = ConversionService.SHARED;

   public <S, T> DataConversionServiceImpl addConverter(Class<S> sourceType, Class<T> targetType, Function<S, T> typeConverter) {
      this.internalConversionService.addConverter(sourceType, targetType, typeConverter);
      return this;
   }

   public <S, T> DataConversionServiceImpl addConverter(Class<S> sourceType, Class<T> targetType, TypeConverter<S, T> typeConverter) {
      this.internalConversionService.addConverter(sourceType, targetType, typeConverter);
      return this;
   }

   @Override
   public <T> Optional<T> convert(Object object, Class<T> targetType, ConversionContext context) {
      Optional<T> result = this.internalConversionService.convert(object, targetType, context);
      return result.isPresent() ? result : this.sharedConversionService.convert(object, targetType, context);
   }

   @Override
   public <S, T> boolean canConvert(Class<S> sourceType, Class<T> targetType) {
      return this.internalConversionService.canConvert(sourceType, targetType) || this.sharedConversionService.canConvert(sourceType, targetType);
   }
}
