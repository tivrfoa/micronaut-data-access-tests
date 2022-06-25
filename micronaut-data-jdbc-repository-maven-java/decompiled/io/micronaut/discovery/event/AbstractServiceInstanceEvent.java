package io.micronaut.discovery.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.discovery.ServiceInstance;

public abstract class AbstractServiceInstanceEvent extends ApplicationEvent {
   public AbstractServiceInstanceEvent(ServiceInstance source) {
      super(source);
   }

   public ServiceInstance getSource() {
      return (ServiceInstance)super.getSource();
   }
}
