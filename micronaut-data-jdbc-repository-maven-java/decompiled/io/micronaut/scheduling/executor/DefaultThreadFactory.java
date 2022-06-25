package io.micronaut.scheduling.executor;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Factory
public class DefaultThreadFactory {
   @Singleton
   @Primary
   ThreadFactory threadFactory() {
      return Executors.defaultThreadFactory();
   }
}
