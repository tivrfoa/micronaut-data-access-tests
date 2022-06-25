package io.micronaut.context.scope;

import io.micronaut.context.BeanRegistration;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanType;
import java.lang.annotation.Annotation;
import java.util.Optional;

public interface CustomScopeRegistry {
   default Optional<CustomScope<?>> findDeclaredScope(@NonNull Argument<?> argument) {
      return Optional.empty();
   }

   default Optional<CustomScope<?>> findDeclaredScope(@NonNull BeanType<?> beanType) {
      return Optional.empty();
   }

   Optional<CustomScope<?>> findScope(String scopeAnnotation);

   default Optional<CustomScope<?>> findScope(Class<? extends Annotation> scopeAnnotation) {
      return this.findScope(scopeAnnotation.getName());
   }

   default <T> Optional<BeanRegistration<T>> findBeanRegistration(T bean) {
      return Optional.empty();
   }
}
