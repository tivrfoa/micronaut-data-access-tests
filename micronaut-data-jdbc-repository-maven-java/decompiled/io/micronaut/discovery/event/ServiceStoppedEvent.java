package io.micronaut.discovery.event;

import io.micronaut.discovery.ServiceInstance;

public class ServiceStoppedEvent extends AbstractServiceInstanceEvent {
   public ServiceStoppedEvent(ServiceInstance source) {
      super(source);
   }
}
