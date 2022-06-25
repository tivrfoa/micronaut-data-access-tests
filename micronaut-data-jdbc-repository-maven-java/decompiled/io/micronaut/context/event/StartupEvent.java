package io.micronaut.context.event;

import io.micronaut.context.BeanContext;

public class StartupEvent extends BeanContextEvent {
   public StartupEvent(BeanContext beanContext) {
      super(beanContext);
   }
}
