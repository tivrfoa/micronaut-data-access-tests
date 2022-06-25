package io.micronaut.http.client;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpVersion;
import io.micronaut.inject.InjectionPoint;

public interface HttpClientRegistry<T extends HttpClient> {
   @NonNull
   T getClient(@NonNull AnnotationMetadata annotationMetadata);

   @NonNull
   T getClient(HttpVersion httpVersion, @NonNull String clientId, @Nullable String path);

   @NonNull
   T resolveClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   );

   void disposeClient(AnnotationMetadata annotationMetadata);

   default T getDefaultClient() {
      return this.getClient(AnnotationMetadata.EMPTY_METADATA);
   }
}
