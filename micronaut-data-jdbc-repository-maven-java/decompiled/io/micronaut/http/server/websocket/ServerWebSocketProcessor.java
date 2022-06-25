package io.micronaut.http.server.websocket;

import io.micronaut.context.ExecutionHandleLocator;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.web.router.DefaultRouteBuilder;
import io.micronaut.web.router.RouteBuilder;
import io.micronaut.web.router.UriRoute;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
@Internal
public class ServerWebSocketProcessor extends DefaultRouteBuilder implements ExecutableMethodProcessor<ServerWebSocket> {
   private Set<Class> mappedWebSockets = new HashSet(4);

   ServerWebSocketProcessor(
      ExecutionHandleLocator executionHandleLocator, RouteBuilder.UriNamingStrategy uriNamingStrategy, ConversionService<?> conversionService
   ) {
      super(executionHandleLocator, uriNamingStrategy, conversionService);
   }

   @Override
   public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      Class<?> beanType = beanDefinition.getBeanType();
      if (!this.mappedWebSockets.contains(beanType)) {
         if (method.isAnnotationPresent(OnMessage.class) || method.isAnnotationPresent(OnOpen.class)) {
            this.mappedWebSockets.add(beanType);
            String uri = (String)beanDefinition.stringValue(ServerWebSocket.class).orElse("/ws");
            UriRoute route = this.GET(uri, method);
            if (LOG.isDebugEnabled()) {
               LOG.debug("Created WebSocket: {}", route);
            }
         }

      }
   }
}
