package io.micronaut.websocket;

import io.micronaut.core.annotation.Internal;
import java.util.Iterator;
import java.util.ServiceLoader;

@Internal
final class WebSocketClientFactoryResolver {
   private static volatile WebSocketClientFactory factory;

   static WebSocketClientFactory getFactory() {
      if (factory == null) {
         synchronized(WebSocketClientFactoryResolver.class) {
            if (factory == null) {
               factory = resolveClientFactory();
            }
         }
      }

      return factory;
   }

   private static WebSocketClientFactory resolveClientFactory() {
      Iterator<WebSocketClientFactory> i = ServiceLoader.load(WebSocketClientFactory.class).iterator();
      if (i.hasNext()) {
         return (WebSocketClientFactory)i.next();
      } else {
         throw new IllegalStateException("No HttpClientFactory present on classpath, cannot create client");
      }
   }
}
