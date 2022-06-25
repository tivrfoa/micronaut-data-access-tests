package io.micronaut.context.event;

import io.micronaut.context.BeanContext;

public class ShutdownEvent extends BeanContextEvent {
   public ShutdownEvent(BeanContext beanContext) {
      super(beanContext);
   }
}
