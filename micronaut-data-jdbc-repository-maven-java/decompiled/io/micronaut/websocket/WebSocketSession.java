package io.micronaut.websocket;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.http.MediaType;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import java.net.URI;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.reactivestreams.Publisher;

public interface WebSocketSession extends MutableConvertibleValues<Object>, AutoCloseable {
   String getId();

   MutableConvertibleValues<Object> getAttributes();

   boolean isOpen();

   boolean isWritable();

   boolean isSecure();

   Set<? extends WebSocketSession> getOpenSessions();

   URI getRequestURI();

   String getProtocolVersion();

   <T> Publisher<T> send(T message, MediaType mediaType);

   <T> CompletableFuture<T> sendAsync(T message, MediaType mediaType);

   default void sendSync(Object message, MediaType mediaType) {
      try {
         this.sendAsync(message, mediaType).get();
      } catch (InterruptedException var4) {
         throw new WebSocketSessionException("Send Interrupted");
      } catch (ExecutionException var5) {
         throw new WebSocketSessionException("Send Failure: " + var5.getMessage(), var5);
      }
   }

   default <T> Publisher<T> send(T message) {
      return this.send(message, MediaType.APPLICATION_JSON_TYPE);
   }

   default <T> CompletableFuture<T> sendAsync(T message) {
      return this.sendAsync(message, MediaType.APPLICATION_JSON_TYPE);
   }

   default void sendSync(Object message) {
      this.sendSync(message, MediaType.APPLICATION_JSON_TYPE);
   }

   @NonNull
   default CompletableFuture<?> sendPingAsync(@NonNull byte[] content) {
      throw new UnsupportedOperationException("Ping not supported by this implementation");
   }

   default Optional<String> getSubprotocol() {
      return Optional.empty();
   }

   default ConvertibleMultiValues<String> getRequestParameters() {
      return ConvertibleMultiValues.empty();
   }

   default ConvertibleValues<Object> getUriVariables() {
      return ConvertibleValues.empty();
   }

   default Optional<Principal> getUserPrincipal() {
      return Optional.empty();
   }

   void close();

   void close(CloseReason closeReason);
}
