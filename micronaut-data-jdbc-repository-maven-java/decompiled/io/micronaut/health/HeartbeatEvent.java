package io.micronaut.health;

import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.event.AbstractServiceInstanceEvent;

public class HeartbeatEvent extends AbstractServiceInstanceEvent {
   private final HealthStatus status;

   public HeartbeatEvent(ServiceInstance source, HealthStatus status) {
      super(source);
      this.status = status;
   }

   public HealthStatus getStatus() {
      return this.status;
   }
}
