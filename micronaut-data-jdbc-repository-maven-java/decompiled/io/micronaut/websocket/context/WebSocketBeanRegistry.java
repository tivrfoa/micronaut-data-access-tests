package io.micronaut.websocket.context;

import io.micronaut.context.BeanContext;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.ServerWebSocket;

public interface WebSocketBeanRegistry {
   WebSocketBeanRegistry EMPTY = new WebSocketBeanRegistry() {
      @Override
      public <T> WebSocketBean<T> getWebSocket(Class<T> type) {
         throw new NoSuchBeanException(type);
      }
   };

   <T> WebSocketBean<T> getWebSocket(Class<T> type);

   static WebSocketBeanRegistry forServer(BeanContext beanContext) {
      return new DefaultWebSocketBeanRegistry(beanContext, ServerWebSocket.class);
   }

   static WebSocketBeanRegistry forClient(BeanContext beanContext) {
      return new DefaultWebSocketBeanRegistry(beanContext, ClientWebSocket.class);
   }
}
