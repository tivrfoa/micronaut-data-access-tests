package io.micronaut.http.client;

import io.micronaut.core.annotation.Internal;
import java.util.Iterator;
import java.util.ServiceLoader;

@Internal
final class HttpClientFactoryResolver {
   private static volatile HttpClientFactory factory;

   static HttpClientFactory getFactory() {
      if (factory == null) {
         synchronized(HttpClientFactoryResolver.class) {
            if (factory == null) {
               factory = resolveClientFactory();
            }
         }
      }

      return factory;
   }

   private static HttpClientFactory resolveClientFactory() {
      Iterator<HttpClientFactory> i = ServiceLoader.load(HttpClientFactory.class).iterator();
      if (i.hasNext()) {
         return (HttpClientFactory)i.next();
      } else {
         throw new IllegalStateException("No HttpClientFactory present on classpath, cannot create client");
      }
   }
}
