package io.micronaut.core.convert;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import java.util.Optional;
import java.util.function.Function;

public interface ConversionService<Impl extends ConversionService> {
   ConversionService<?> SHARED = new DefaultConversionService();

   <S, T> Impl addConverter(Class<S> sourceType, Class<T> targetType, Function<S, T> typeConverter);

   <S, T> Impl addConverter(Class<S> sourceType, Class<T> targetType, TypeConverter<S, T> typeConverter);

   <T> Optional<T> convert(Object object, Class<T> targetType, ConversionContext context);

   <S, T> boolean canConvert(Class<S> sourceType, Class<T> targetType);

   default <T> Optional<T> convert(Object object, Class<T> targetType) {
      return this.convert(object, targetType, ConversionContext.DEFAULT);
   }

   default <T> Optional<T> convert(Object object, Argument<T> targetType) {
      return this.convert(object, targetType.getType(), ConversionContext.of(targetType));
   }

   default <T> Optional<T> convert(Object object, ArgumentConversionContext<T> context) {
      return this.convert(object, context.getArgument().getType(), context);
   }

   @Nullable
   default <T> T convertRequired(@Nullable Object value, Class<T> type) {
      if (value == null) {
         return null;
      } else {
         Argument<T> arg = Argument.of(type);
         return this.convertRequired(value, arg);
      }
   }

   @Nullable
   default <T> T convertRequired(@Nullable Object value, Argument<T> argument) {
      ArgumentConversionContext<T> context = ConversionContext.of(argument);
      return (T)this.convert(value, argument.getType(), context)
         .orElseThrow(
            () -> {
               Optional<ConversionError> lastError = context.getLastError();
               return lastError.isPresent()
                  ? new ConversionErrorException(context.getArgument(), (ConversionError)lastError.get())
                  : new ConversionErrorException(
                     context.getArgument(),
                     new IllegalArgumentException(
                        "Cannot convert type ["
                           + value.getClass()
                           + "] to target type: "
                           + argument.getType()
                           + ". Considering defining a TypeConverter bean to handle this case."
                     )
                  );
            }
         );
   }
}
