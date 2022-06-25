package io.micronaut.context.env;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.value.PropertyResolver;
import java.util.Optional;

@FunctionalInterface
public interface PropertyExpressionResolver {
   @NonNull
   <T> Optional<T> resolve(
      @NonNull PropertyResolver propertyResolver, @NonNull ConversionService<?> conversionService, @NonNull String expression, @NonNull Class<T> requiredType
   );
}
