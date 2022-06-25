package io.micronaut.context.event;

import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;

public class BeanDestroyedEvent<T> extends BeanEvent<T> {
   public BeanDestroyedEvent(BeanContext beanContext, BeanDefinition<T> beanDefinition, T bean) {
      super(beanContext, beanDefinition, bean);
   }
}
