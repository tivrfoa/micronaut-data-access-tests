package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.type.Argument;
import java.util.Map;

public interface ParametrizedBeanFactory<T> extends BeanFactory<T> {
   Argument<?>[] getRequiredArguments();

   T build(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition, Map<String, Object> requiredArgumentValues) throws BeanInstantiationException;

   @Override
   default T build(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition) throws BeanInstantiationException {
      throw new BeanInstantiationException(definition, "Cannot instantiate parametrized bean with no arguments");
   }
}
