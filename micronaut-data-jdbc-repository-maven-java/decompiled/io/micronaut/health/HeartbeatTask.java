package io.micronaut.health;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@HeartbeatEnabled
public class HeartbeatTask implements ApplicationEventListener<ServiceReadyEvent> {
   private final Set<ServiceInstance> eventsReference = ConcurrentHashMap.newKeySet();
   private final ApplicationEventPublisher eventPublisher;
   private final CurrentHealthStatus currentHealthStatus;

   public HeartbeatTask(ApplicationEventPublisher eventPublisher, HeartbeatConfiguration configuration, CurrentHealthStatus currentHealthStatus) {
      this.eventPublisher = eventPublisher;
      this.currentHealthStatus = currentHealthStatus;
   }

   @Scheduled(
      fixedDelay = "${micronaut.heartbeat.interval:15s}",
      initialDelay = "${micronaut.heartbeat.initial-delay:5s}"
   )
   public void pulsate() {
      for(ServiceInstance instance : this.eventsReference) {
         this.eventPublisher.publishEvent(new HeartbeatEvent(instance, this.currentHealthStatus.current()));
      }

   }

   public void onApplicationEvent(ServiceReadyEvent event) {
      this.eventsReference.add(event.getSource());
   }
}
