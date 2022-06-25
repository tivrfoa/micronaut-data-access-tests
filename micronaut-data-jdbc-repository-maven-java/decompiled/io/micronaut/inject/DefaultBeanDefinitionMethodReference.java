package io.micronaut.inject;

import io.micronaut.core.annotation.Internal;

@Internal
class DefaultBeanDefinitionMethodReference<T, R> implements BeanDefinitionMethodReference<T, R>, DelegatingExecutableMethod<T, R> {
   private final BeanDefinition<T> definition;
   private final ExecutableMethod<T, R> method;

   DefaultBeanDefinitionMethodReference(BeanDefinition<T> definition, ExecutableMethod<T, R> method) {
      this.definition = definition;
      this.method = method;
   }

   @Override
   public BeanDefinition<T> getBeanDefinition() {
      return this.definition;
   }

   @Override
   public ExecutableMethod<T, R> getTarget() {
      return this.method;
   }

   public String toString() {
      return this.getTarget().toString();
   }
}
