package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanResolutionContext;
import io.micronaut.context.exceptions.BeanInstantiationException;

public interface BeanFactory<T> {
   default T build(BeanContext context, BeanDefinition<T> definition) throws BeanInstantiationException {
      return this.build(new DefaultBeanResolutionContext(context, definition), context, definition);
   }

   T build(BeanResolutionContext resolutionContext, BeanContext context, BeanDefinition<T> definition) throws BeanInstantiationException;
}
