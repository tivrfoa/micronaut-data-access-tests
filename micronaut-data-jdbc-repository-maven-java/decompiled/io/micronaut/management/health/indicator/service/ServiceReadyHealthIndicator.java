package io.micronaut.management.health.indicator.service;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.management.health.indicator.annotation.Readiness;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Requires(
   beans = {HealthEndpoint.class}
)
@Readiness
public class ServiceReadyHealthIndicator implements HealthIndicator {
   private static final String NAME = "service";
   private final boolean isService;
   private boolean serviceReady = false;

   @Internal
   protected ServiceReadyHealthIndicator(ApplicationConfiguration applicationConfiguration) {
      this.isService = applicationConfiguration.getName().isPresent();
   }

   @Override
   public int getOrder() {
      return Integer.MAX_VALUE;
   }

   @Override
   public Publisher<HealthResult> getResult() {
      HealthResult.Builder builder = HealthResult.builder("service");
      if (this.serviceReady) {
         builder.status(HealthStatus.UP);
      } else {
         builder.status(HealthStatus.DOWN);
      }

      return Flux.just(builder.build());
   }

   @EventListener
   void onServiceStarted(ServiceReadyEvent event) {
      this.serviceReady = true;
   }

   @EventListener
   void onServerStarted(ServerStartupEvent event) {
      if (!this.isService) {
         this.serviceReady = true;
      }

   }
}
