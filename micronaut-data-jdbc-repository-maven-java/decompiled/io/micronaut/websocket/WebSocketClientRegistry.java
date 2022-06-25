package io.micronaut.websocket;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.inject.InjectionPoint;

public interface WebSocketClientRegistry<W extends WebSocketClient> {
   @NonNull
   W resolveWebSocketClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   );

   @NonNull
   W getWebSocketClient(@NonNull AnnotationMetadata annotationMetadata);

   default W getDefaultWebSocketClient() {
      return this.getWebSocketClient(AnnotationMetadata.EMPTY_METADATA);
   }

   void disposeClient(AnnotationMetadata annotationMetadata);
}
