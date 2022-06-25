package io.micronaut.context.event;

import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanIdentifier;

public class BeanCreatedEvent<T> extends BeanEvent<T> {
   private final BeanIdentifier beanIdentifier;

   public BeanCreatedEvent(BeanContext beanContext, BeanDefinition<T> beanDefinition, BeanIdentifier beanIdentifier, T bean) {
      super(beanContext, beanDefinition, bean);
      this.beanIdentifier = beanIdentifier;
   }

   public BeanIdentifier getBeanIdentifier() {
      return this.beanIdentifier;
   }
}
