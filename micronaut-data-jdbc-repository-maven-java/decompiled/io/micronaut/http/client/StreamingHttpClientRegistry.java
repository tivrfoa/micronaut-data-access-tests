package io.micronaut.http.client;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.InjectionPoint;

public interface StreamingHttpClientRegistry<S extends StreamingHttpClient> {
   @NonNull
   S resolveStreamingHttpClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   );

   @NonNull
   S getStreamingHttpClient(@NonNull AnnotationMetadata annotationMetadata);

   default S getDefaultStreamingHttpClient() {
      return this.getStreamingHttpClient(AnnotationMetadata.EMPTY_METADATA);
   }

   void disposeClient(AnnotationMetadata annotationMetadata);
}
