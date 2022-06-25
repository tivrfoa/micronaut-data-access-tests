package io.micronaut.http.client;

import io.micronaut.core.annotation.Internal;
import java.util.Iterator;
import java.util.ServiceLoader;

@Internal
final class ProxyHttpClientFactoryResolver {
   private static volatile ProxyHttpClientFactory factory;

   static ProxyHttpClientFactory getFactory() {
      if (factory == null) {
         synchronized(ProxyHttpClientFactoryResolver.class) {
            if (factory == null) {
               factory = resolveClientFactory();
            }
         }
      }

      return factory;
   }

   private static ProxyHttpClientFactory resolveClientFactory() {
      Iterator<ProxyHttpClientFactory> i = ServiceLoader.load(ProxyHttpClientFactory.class).iterator();
      if (i.hasNext()) {
         return (ProxyHttpClientFactory)i.next();
      } else {
         throw new IllegalStateException("No ProxyHttpClientFactory present on classpath, cannot create client");
      }
   }
}
