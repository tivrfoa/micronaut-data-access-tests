package io.micronaut.http.client.sse;

import io.micronaut.core.annotation.Internal;
import java.util.Iterator;
import java.util.ServiceLoader;

@Internal
final class SseClientFactoryResolver {
   private static volatile SseClientFactory factory;

   static SseClientFactory getFactory() {
      if (factory == null) {
         synchronized(SseClientFactoryResolver.class) {
            if (factory == null) {
               factory = resolveClientFactory();
            }
         }
      }

      return factory;
   }

   private static SseClientFactory resolveClientFactory() {
      Iterator<SseClientFactory> i = ServiceLoader.load(SseClientFactory.class).iterator();
      if (i.hasNext()) {
         return (SseClientFactory)i.next();
      } else {
         throw new IllegalStateException("No SseClientFactory present on classpath, cannot create sse client");
      }
   }
}
