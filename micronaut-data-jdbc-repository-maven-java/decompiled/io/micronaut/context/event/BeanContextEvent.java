package io.micronaut.context.event;

import io.micronaut.context.BeanContext;

public abstract class BeanContextEvent extends ApplicationEvent {
   public BeanContextEvent(BeanContext beanContext) {
      super(beanContext);
   }

   public BeanContext getSource() {
      return (BeanContext)super.getSource();
   }
}
