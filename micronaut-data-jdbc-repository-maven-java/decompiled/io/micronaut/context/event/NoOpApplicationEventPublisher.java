package io.micronaut.context.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

final class NoOpApplicationEventPublisher implements ApplicationEventPublisher<Object> {
   @Override
   public void publishEvent(Object event) {
   }

   @Override
   public Future<Void> publishEventAsync(Object event) {
      return CompletableFuture.completedFuture(null);
   }
}
