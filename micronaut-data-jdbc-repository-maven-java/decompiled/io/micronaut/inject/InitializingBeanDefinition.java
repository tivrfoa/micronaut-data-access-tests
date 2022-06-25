package io.micronaut.inject;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.DefaultBeanResolutionContext;

public interface InitializingBeanDefinition<T> extends BeanDefinition<T> {
   default T initialize(BeanContext context, T bean) {
      return this.initialize(new DefaultBeanResolutionContext(context, this), context, bean);
   }

   T initialize(BeanResolutionContext resolutionContext, BeanContext context, T bean);
}
