package io.micronaut.management.health.indicator;

import io.micronaut.core.async.publisher.AsyncSingleResultPublisher;
import io.micronaut.health.HealthStatus;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.concurrent.ExecutorService;
import org.reactivestreams.Publisher;

public abstract class AbstractHealthIndicator<T> implements HealthIndicator {
   protected ExecutorService executorService;
   protected HealthStatus healthStatus;

   @Inject
   public void setExecutorService(@Named("io") ExecutorService executorService) {
      this.executorService = executorService;
   }

   @Override
   public Publisher<HealthResult> getResult() {
      if (this.executorService == null) {
         throw new IllegalStateException("I/O ExecutorService is null");
      } else {
         return new AsyncSingleResultPublisher<>(this.executorService, this::getHealthResult);
      }
   }

   protected abstract T getHealthInformation();

   protected HealthResult getHealthResult() {
      HealthResult.Builder builder = HealthResult.builder(this.getName());

      try {
         builder.details(this.getHealthInformation());
         builder.status(this.healthStatus);
      } catch (Exception var3) {
         builder.status(HealthStatus.DOWN);
         builder.exception(var3);
      }

      return builder.build();
   }

   protected abstract String getName();
}
