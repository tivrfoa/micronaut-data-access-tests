package io.micronaut.context.event;

import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;

public abstract class BeanEvent<T> extends BeanContextEvent {
   protected final BeanDefinition<T> beanDefinition;
   protected final T bean;

   public BeanEvent(BeanContext beanContext, BeanDefinition<T> beanDefinition, T bean) {
      super(beanContext);
      this.beanDefinition = beanDefinition;
      this.bean = bean;
   }

   public T getBean() {
      return this.bean;
   }

   public BeanDefinition<T> getBeanDefinition() {
      return this.beanDefinition;
   }
}
