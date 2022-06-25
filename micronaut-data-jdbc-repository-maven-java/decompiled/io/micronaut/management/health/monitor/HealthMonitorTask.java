package io.micronaut.management.health.monitor;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.health.CurrentHealthStatus;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Singleton
@Requirements({@Requires(
   beans = {EmbeddedServer.class}
), @Requires(
   property = "micronaut.application.name"
), @Requires(
   property = "micronaut.health.monitor.enabled",
   value = "true",
   defaultValue = "true"
)})
public class HealthMonitorTask {
   private static final Logger LOG = LoggerFactory.getLogger(HealthMonitorTask.class);
   private final CurrentHealthStatus currentHealthStatus;
   private final List<HealthIndicator> healthIndicators;

   @Inject
   public HealthMonitorTask(CurrentHealthStatus currentHealthStatus, List<HealthIndicator> healthIndicators) {
      this.currentHealthStatus = currentHealthStatus;
      this.healthIndicators = healthIndicators;
   }

   public HealthMonitorTask(CurrentHealthStatus currentHealthStatus, HealthIndicator... healthIndicators) {
      this(currentHealthStatus, Arrays.asList(healthIndicators));
   }

   @Scheduled(
      fixedDelay = "${micronaut.health.monitor.interval:1m}",
      initialDelay = "${micronaut.health.monitor.initial-delay:1m}"
   )
   void monitor() {
      if (LOG.isDebugEnabled()) {
         LOG.debug("Starting health monitor check");
      }

      List<Publisher<HealthResult>> healthResults = (List)this.healthIndicators.stream().map(HealthIndicator::getResult).collect(Collectors.toList());
      Flux<HealthResult> reactiveSequence = Flux.<HealthResult>merge(healthResults).filter(healthResult -> {
         HealthStatus status = healthResult.getStatus();
         return status.equals(HealthStatus.DOWN) || !status.getOperational().orElse(true);
      });
      reactiveSequence.next().subscribe(new Subscriber<HealthResult>() {
         @Override
         public void onSubscribe(Subscription s) {
         }

         public void onNext(HealthResult healthResult) {
            HealthStatus status = healthResult.getStatus();
            if (HealthMonitorTask.LOG.isDebugEnabled()) {
               HealthMonitorTask.LOG.debug("Health monitor check failed with status {}", status);
            }

            HealthMonitorTask.this.currentHealthStatus.update(status);
         }

         @Override
         public void onError(Throwable e) {
            if (HealthMonitorTask.LOG.isErrorEnabled()) {
               HealthMonitorTask.LOG.error("Health monitor check failed with exception: " + e.getMessage(), e);
            }

            HealthMonitorTask.this.currentHealthStatus.update(HealthStatus.DOWN.describe("Error occurred running health check: " + e.getMessage()));
         }

         @Override
         public void onComplete() {
            if (HealthMonitorTask.LOG.isDebugEnabled()) {
               HealthMonitorTask.LOG.debug("Health monitor check passed.");
            }

            HealthMonitorTask.this.currentHealthStatus.update(HealthStatus.UP);
         }
      });
   }
}
