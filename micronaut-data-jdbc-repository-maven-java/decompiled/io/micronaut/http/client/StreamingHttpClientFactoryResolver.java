package io.micronaut.http.client;

import io.micronaut.core.annotation.Internal;
import java.util.Iterator;
import java.util.ServiceLoader;

@Internal
final class StreamingHttpClientFactoryResolver {
   private static volatile StreamingHttpClientFactory factory;

   static StreamingHttpClientFactory getFactory() {
      if (factory == null) {
         synchronized(StreamingHttpClientFactoryResolver.class) {
            if (factory == null) {
               factory = resolveClientFactory();
            }
         }
      }

      return factory;
   }

   private static StreamingHttpClientFactory resolveClientFactory() {
      Iterator<StreamingHttpClientFactory> i = ServiceLoader.load(StreamingHttpClientFactory.class).iterator();
      if (i.hasNext()) {
         return (StreamingHttpClientFactory)i.next();
      } else {
         throw new IllegalStateException("No HttpClientFactory present on classpath, cannot create client");
      }
   }
}
