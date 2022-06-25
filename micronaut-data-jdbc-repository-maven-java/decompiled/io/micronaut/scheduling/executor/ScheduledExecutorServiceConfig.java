package io.micronaut.scheduling.executor;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Requires(
   missingProperty = "micronaut.executors.scheduled"
)
@Factory
public class ScheduledExecutorServiceConfig {
   @Singleton
   @Named("scheduled")
   ExecutorConfiguration configuration() {
      return UserExecutorConfiguration.of("scheduled", ExecutorType.SCHEDULED);
   }
}
