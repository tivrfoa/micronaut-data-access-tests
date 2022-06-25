package io.micronaut.scheduling.executor;

import io.micronaut.core.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import javax.validation.constraints.Min;

public interface ExecutorConfiguration {
   String PREFIX = "micronaut.executors";
   String PREFIX_IO = "micronaut.executors.io";
   String PREFIX_SCHEDULED = "micronaut.executors.scheduled";
   String PREFIX_CONSUMER = "micronaut.executors.consumer";

   @Nullable
   default String getName() {
      return null;
   }

   ExecutorType getType();

   @Min(1L)
   Integer getParallelism();

   @Min(1L)
   Integer getNumberOfThreads();

   @Min(1L)
   Integer getCorePoolSize();

   Optional<Class<? extends ThreadFactory>> getThreadFactoryClass();
}
