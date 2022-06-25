package io.micronaut.discovery.event;

import io.micronaut.discovery.ServiceInstance;

public class ServiceReadyEvent extends AbstractServiceInstanceEvent {
   public ServiceReadyEvent(ServiceInstance source) {
      super(source);
   }
}
