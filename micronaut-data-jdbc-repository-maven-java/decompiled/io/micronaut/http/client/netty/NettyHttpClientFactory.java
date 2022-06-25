package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.HttpClientFactory;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.client.ProxyHttpClientFactory;
import io.micronaut.http.client.StreamingHttpClient;
import io.micronaut.http.client.StreamingHttpClientFactory;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.client.sse.SseClientFactory;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.WebSocketClientFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Internal
public class NettyHttpClientFactory implements HttpClientFactory, SseClientFactory, ProxyHttpClientFactory, StreamingHttpClientFactory, WebSocketClientFactory {
   @NonNull
   @Override
   public HttpClient createClient(URL url) {
      return this.createNettyClient(url);
   }

   @NonNull
   @Override
   public HttpClient createClient(URL url, @NonNull HttpClientConfiguration configuration) {
      return this.createNettyClient(url, configuration);
   }

   @NonNull
   @Override
   public ProxyHttpClient createProxyClient(URL url) {
      return this.createNettyClient(url);
   }

   @NonNull
   @Override
   public ProxyHttpClient createProxyClient(URL url, @NonNull HttpClientConfiguration configuration) {
      return this.createNettyClient(url, configuration);
   }

   @NonNull
   @Override
   public SseClient createSseClient(@Nullable URL url) {
      return this.createNettyClient(url);
   }

   @NonNull
   @Override
   public SseClient createSseClient(@Nullable URL url, @NonNull HttpClientConfiguration configuration) {
      return this.createNettyClient(url, configuration);
   }

   @NonNull
   @Override
   public StreamingHttpClient createStreamingClient(URL url) {
      return this.createNettyClient(url);
   }

   @NonNull
   @Override
   public StreamingHttpClient createStreamingClient(URL url, @NonNull HttpClientConfiguration configuration) {
      return this.createNettyClient(url, configuration);
   }

   @NonNull
   @Override
   public WebSocketClient createWebSocketClient(URI uri) {
      return this.createNettyClient(uri);
   }

   @NonNull
   @Override
   public WebSocketClient createWebSocketClient(URI uri, @NonNull HttpClientConfiguration configuration) {
      return this.createNettyClient(uri, configuration);
   }

   private DefaultHttpClient createNettyClient(URL url) {
      try {
         return this.createNettyClient(url != null ? url.toURI() : null);
      } catch (URISyntaxException var3) {
         throw new IllegalArgumentException(var3);
      }
   }

   private DefaultHttpClient createNettyClient(URL url, HttpClientConfiguration configuration) {
      try {
         return this.createNettyClient(url != null ? url.toURI() : null, configuration);
      } catch (URISyntaxException var4) {
         throw new IllegalArgumentException(var4);
      }
   }

   private DefaultHttpClient createNettyClient(URI uri) {
      return new DefaultHttpClient(uri);
   }

   private DefaultHttpClient createNettyClient(URI uri, HttpClientConfiguration configuration) {
      return new DefaultHttpClient(uri, configuration);
   }
}
