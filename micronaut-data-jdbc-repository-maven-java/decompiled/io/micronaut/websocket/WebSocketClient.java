package io.micronaut.websocket;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClientConfiguration;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import org.reactivestreams.Publisher;

public interface WebSocketClient extends AutoCloseable {
   String SCHEME_WS = "ws";
   String SCHEME_WSS = "wss";

   <T extends AutoCloseable> Publisher<T> connect(Class<T> clientEndpointType, MutableHttpRequest<?> request);

   <T extends AutoCloseable> Publisher<T> connect(Class<T> clientEndpointType, Map<String, Object> parameters);

   void close();

   default <T extends AutoCloseable> Publisher<T> connect(Class<T> clientEndpointType, String uri) {
      return this.connect(clientEndpointType, URI.create(uri));
   }

   default <T extends AutoCloseable> Publisher<T> connect(Class<T> clientEndpointType, URI uri) {
      return this.connect(clientEndpointType, HttpRequest.GET(uri));
   }

   @Deprecated
   @NonNull
   static WebSocketClient create(@Nullable URL url) {
      return WebSocketClientFactoryResolver.getFactory().createWebSocketClient(url);
   }

   @Deprecated
   @NonNull
   static WebSocketClient create(@Nullable URL url, HttpClientConfiguration configuration) {
      return WebSocketClientFactoryResolver.getFactory().createWebSocketClient(url, configuration);
   }

   @NonNull
   static WebSocketClient create(@Nullable URI uri) {
      return WebSocketClientFactoryResolver.getFactory().createWebSocketClient(uri);
   }

   @NonNull
   static WebSocketClient create(@Nullable URI uri, HttpClientConfiguration configuration) {
      return WebSocketClientFactoryResolver.getFactory().createWebSocketClient(uri, configuration);
   }
}
