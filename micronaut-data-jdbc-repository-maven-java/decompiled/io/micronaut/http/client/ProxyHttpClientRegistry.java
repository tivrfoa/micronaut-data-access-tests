package io.micronaut.http.client;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.InjectionPoint;

public interface ProxyHttpClientRegistry<P extends ProxyHttpClient> {
   @NonNull
   P resolveProxyHttpClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   );

   void disposeClient(AnnotationMetadata annotationMetadata);

   @NonNull
   P getProxyHttpClient(@NonNull AnnotationMetadata annotationMetadata);

   default P getDefaultProxyHttpClient() {
      return this.getProxyHttpClient(AnnotationMetadata.EMPTY_METADATA);
   }
}
