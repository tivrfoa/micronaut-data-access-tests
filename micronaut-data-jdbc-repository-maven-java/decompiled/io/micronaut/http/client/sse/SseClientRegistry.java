package io.micronaut.http.client.sse;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.inject.InjectionPoint;

public interface SseClientRegistry<E extends SseClient> {
   @NonNull
   E resolveSseClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   );

   @NonNull
   E getSseClient(@NonNull AnnotationMetadata annotationMetadata);

   void disposeClient(AnnotationMetadata annotationMetadata);

   default E getDefaultSseClient() {
      return this.getSseClient(AnnotationMetadata.EMPTY_METADATA);
   }
}
