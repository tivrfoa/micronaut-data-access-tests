package io.micronaut.websocket;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.client.HttpClientConfiguration;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public interface WebSocketClientFactory {
   @NonNull
   @Deprecated
   default WebSocketClient createWebSocketClient(@Nullable URL url) {
      try {
         return this.createWebSocketClient(url != null ? url.toURI() : null);
      } catch (URISyntaxException var3) {
         throw new IllegalArgumentException(var3);
      }
   }

   @NonNull
   @Deprecated
   default WebSocketClient createWebSocketClient(@Nullable URL url, @NonNull HttpClientConfiguration configuration) {
      try {
         return this.createWebSocketClient(url != null ? url.toURI() : null, configuration);
      } catch (URISyntaxException var4) {
         throw new IllegalArgumentException(var4);
      }
   }

   @NonNull
   default WebSocketClient createWebSocketClient(@Nullable URI uri) {
      try {
         return this.createWebSocketClient(uri != null ? uri.toURL() : null);
      } catch (MalformedURLException var3) {
         throw new UnsupportedOperationException(var3);
      }
   }

   @NonNull
   default WebSocketClient createWebSocketClient(@Nullable URI uri, @NonNull HttpClientConfiguration configuration) {
      try {
         return this.createWebSocketClient(uri != null ? uri.toURL() : null, configuration);
      } catch (MalformedURLException var4) {
         throw new UnsupportedOperationException(var4);
      }
   }
}
