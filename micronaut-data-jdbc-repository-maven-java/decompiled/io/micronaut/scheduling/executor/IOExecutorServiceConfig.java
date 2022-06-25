package io.micronaut.scheduling.executor;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Requires(
   missingProperty = "micronaut.executors.io"
)
@Factory
public class IOExecutorServiceConfig {
   @Singleton
   @Named("io")
   ExecutorConfiguration configuration() {
      return UserExecutorConfiguration.of("io", ExecutorType.CACHED);
   }
}
