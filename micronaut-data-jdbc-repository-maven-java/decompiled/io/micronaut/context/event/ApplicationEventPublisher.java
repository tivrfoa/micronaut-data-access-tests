package io.micronaut.context.event;

import io.micronaut.core.annotation.NonNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface ApplicationEventPublisher<T> {
   ApplicationEventPublisher<?> NO_OP = new NoOpApplicationEventPublisher();

   static <K> ApplicationEventPublisher<K> noOp() {
      return NO_OP;
   }

   void publishEvent(@NonNull T event);

   @NonNull
   default Future<Void> publishEventAsync(@NonNull T event) {
      CompletableFuture<Void> future = new CompletableFuture();
      future.completeExceptionally(new UnsupportedOperationException("Asynchronous event publishing is not supported by this implementation"));
      return future;
   }
}
