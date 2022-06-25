package io.micronaut.inject;

import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.validation.BeanDefinitionValidator;

public interface ValidatedBeanDefinition<T> extends BeanDefinition<T> {
   default T validate(BeanResolutionContext resolutionContext, T instance) {
      BeanDefinitionValidator validator = resolutionContext.getContext().getBeanValidator();
      validator.validateBean(resolutionContext, this, instance);
      return instance;
   }

   default <V> void validateBeanArgument(
      @NonNull BeanResolutionContext resolutionContext, @NonNull InjectionPoint injectionPoint, @NonNull Argument<V> argument, int index, @Nullable V value
   ) throws BeanInstantiationException {
      BeanDefinitionValidator validator = resolutionContext.getContext().getBeanValidator();
      validator.validateBeanArgument(resolutionContext, injectionPoint, argument, index, value);
   }
}
