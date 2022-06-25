package io.micronaut.websocket;

import io.micronaut.http.MediaType;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public interface WebSocketBroadcaster {
   <T> Publisher<T> broadcast(T message, MediaType mediaType, Predicate<WebSocketSession> filter);

   default <T> Publisher<T> broadcast(T message, MediaType mediaType) {
      return this.broadcast(message, mediaType, s -> true);
   }

   default <T> Publisher<T> broadcast(T message) {
      return this.broadcast(message, MediaType.APPLICATION_JSON_TYPE, s -> true);
   }

   default <T> Publisher<T> broadcast(T message, Predicate<WebSocketSession> filter) {
      Objects.requireNonNull(filter, "The filter cannot be null");
      return this.broadcast(message, MediaType.APPLICATION_JSON_TYPE, filter);
   }

   default <T> CompletableFuture<T> broadcastAsync(T message, MediaType mediaType, Predicate<WebSocketSession> filter) {
      CompletableFuture<T> future = new CompletableFuture();
      Flux.from(this.broadcast(message, mediaType, filter)).subscribe(o -> {
      }, future::completeExceptionally, () -> future.complete(message));
      return future;
   }

   default <T> CompletableFuture<T> broadcastAsync(T message) {
      return this.broadcastAsync(message, MediaType.APPLICATION_JSON_TYPE, o -> true);
   }

   default <T> CompletableFuture<T> broadcastAsync(T message, Predicate<WebSocketSession> filter) {
      return this.broadcastAsync(message, MediaType.APPLICATION_JSON_TYPE, filter);
   }

   default <T> CompletableFuture<T> broadcastAsync(T message, MediaType mediaType) {
      return this.broadcastAsync(message, mediaType, o -> true);
   }

   default <T> void broadcastSync(T message, MediaType mediaType, Predicate<WebSocketSession> filter) {
      try {
         this.broadcastAsync(message, mediaType, filter).get();
      } catch (InterruptedException var5) {
         throw new WebSocketSessionException("Broadcast Interrupted");
      } catch (ExecutionException var6) {
         throw new WebSocketSessionException("Broadcast Failure: " + var6.getMessage(), var6);
      }
   }

   default <T> void broadcastSync(T message) {
      this.broadcastSync(message, MediaType.APPLICATION_JSON_TYPE, o -> true);
   }

   default <T> void broadcastSync(T message, Predicate<WebSocketSession> filter) {
      this.broadcastSync(message, MediaType.APPLICATION_JSON_TYPE, filter);
   }

   default <T> void broadcastSync(T message, MediaType mediaType) {
      this.broadcastSync(message, mediaType, o -> true);
   }
}
