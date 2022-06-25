package io.micronaut.websocket.context;

import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ExecutionHandle;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.websocket.WebSocketPongMessage;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnError;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.exceptions.WebSocketException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class DefaultWebSocketBeanRegistry implements WebSocketBeanRegistry {
   private final BeanContext beanContext;
   private final Class<? extends Annotation> stereotype;
   private final Map<Class, WebSocketBean> webSocketBeanMap = new ConcurrentHashMap(3);

   DefaultWebSocketBeanRegistry(BeanContext beanContext, Class<? extends Annotation> stereotype) {
      this.beanContext = beanContext;
      this.stereotype = stereotype;
   }

   @Override
   public <T> WebSocketBean<T> getWebSocket(Class<T> type) {
      WebSocketBean webSocketBean = (WebSocketBean)this.webSocketBeanMap.get(type);
      if (webSocketBean != null) {
         return webSocketBean;
      } else {
         Qualifier<T> qualifier = Qualifiers.byStereotype(this.stereotype);
         BeanDefinition<T> beanDefinition = this.beanContext.getBeanDefinition(type, qualifier);
         T bean = this.beanContext.getBean(type, qualifier);
         Collection<ExecutableMethod<T, ?>> executableMethods = beanDefinition.getExecutableMethods();
         MethodExecutionHandle<T, ?> onOpen = null;
         MethodExecutionHandle<T, ?> onClose = null;
         MethodExecutionHandle<T, ?> onMessage = null;
         MethodExecutionHandle<T, ?> onPong = null;
         MethodExecutionHandle<T, ?> onError = null;

         for(ExecutableMethod<T, ?> method : executableMethods) {
            if (method.isAnnotationPresent(OnOpen.class)) {
               onOpen = ExecutionHandle.of(bean, method);
            } else if (method.isAnnotationPresent(OnClose.class)) {
               onClose = ExecutionHandle.of(bean, method);
            } else if (method.isAnnotationPresent(OnError.class)) {
               onError = ExecutionHandle.of(bean, method);
            } else if (method.isAnnotationPresent(OnMessage.class)) {
               if (Arrays.asList(method.getArgumentTypes()).contains(WebSocketPongMessage.class)) {
                  onPong = ExecutionHandle.of(bean, method);
               } else {
                  onMessage = ExecutionHandle.of(bean, method);
               }
            }
         }

         if (onMessage == null) {
            throw new WebSocketException("WebSocket handler must specify an @OnMessage handler: " + bean);
         } else {
            DefaultWebSocketBeanRegistry.DefaultWebSocketBean<T> newWebSocketBean = new DefaultWebSocketBeanRegistry.DefaultWebSocketBean<>(
               bean, beanDefinition, onOpen, onClose, onMessage, onPong, onError
            );
            if (beanDefinition.isSingleton()) {
               this.webSocketBeanMap.put(type, newWebSocketBean);
            }

            return newWebSocketBean;
         }
      }
   }

   private static class DefaultWebSocketBean<T> implements WebSocketBean<T> {
      private final T bean;
      private final BeanDefinition<T> definition;
      private final MethodExecutionHandle<T, ?> onOpen;
      private final MethodExecutionHandle<T, ?> onClose;
      private final MethodExecutionHandle<T, ?> onMessage;
      private final MethodExecutionHandle<T, ?> onPong;
      private final MethodExecutionHandle<T, ?> onError;

      DefaultWebSocketBean(
         T bean,
         BeanDefinition<T> definition,
         MethodExecutionHandle<T, ?> onOpen,
         MethodExecutionHandle<T, ?> onClose,
         MethodExecutionHandle<T, ?> onMessage,
         MethodExecutionHandle<T, ?> onPong,
         MethodExecutionHandle<T, ?> onError
      ) {
         this.bean = bean;
         this.definition = definition;
         this.onOpen = onOpen;
         this.onClose = onClose;
         this.onMessage = onMessage;
         this.onPong = onPong;
         this.onError = onError;
      }

      @Override
      public BeanDefinition<T> getBeanDefinition() {
         return this.definition;
      }

      @Override
      public T getTarget() {
         return this.bean;
      }

      @Override
      public Optional<MethodExecutionHandle<T, ?>> messageMethod() {
         return Optional.of(this.onMessage);
      }

      @Override
      public Optional<MethodExecutionHandle<T, ?>> pongMethod() {
         return Optional.ofNullable(this.onPong);
      }

      @Override
      public Optional<MethodExecutionHandle<T, ?>> closeMethod() {
         return Optional.ofNullable(this.onClose);
      }

      @Override
      public Optional<MethodExecutionHandle<T, ?>> openMethod() {
         return Optional.ofNullable(this.onOpen);
      }

      @Override
      public Optional<MethodExecutionHandle<T, ?>> errorMethod() {
         return Optional.ofNullable(this.onError);
      }
   }
}
