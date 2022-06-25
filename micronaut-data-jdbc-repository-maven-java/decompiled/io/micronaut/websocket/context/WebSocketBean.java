package io.micronaut.websocket.context;

import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.MethodExecutionHandle;
import java.util.Optional;

public interface WebSocketBean<T> {
   BeanDefinition<T> getBeanDefinition();

   T getTarget();

   Optional<MethodExecutionHandle<T, ?>> messageMethod();

   default Optional<MethodExecutionHandle<T, ?>> pongMethod() {
      return Optional.empty();
   }

   Optional<MethodExecutionHandle<T, ?>> closeMethod();

   Optional<MethodExecutionHandle<T, ?>> openMethod();

   Optional<MethodExecutionHandle<T, ?>> errorMethod();
}
