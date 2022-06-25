package io.micronaut.inject.validation;

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.InjectionPoint;

public interface BeanDefinitionValidator {
   BeanDefinitionValidator DEFAULT = new BeanDefinitionValidator() {
   };

   default <T> void validateBeanArgument(
      @NonNull BeanResolutionContext resolutionContext, @NonNull InjectionPoint injectionPoint, @NonNull Argument<T> argument, int index, @Nullable T value
   ) throws BeanInstantiationException {
   }

   default <T> void validateBean(@NonNull BeanResolutionContext resolutionContext, @NonNull BeanDefinition<T> definition, @NonNull T bean) throws BeanInstantiationException {
   }
}
