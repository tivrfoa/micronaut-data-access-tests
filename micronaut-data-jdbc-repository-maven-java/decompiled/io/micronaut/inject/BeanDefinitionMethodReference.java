package io.micronaut.inject;

public interface BeanDefinitionMethodReference<T, R> extends ExecutableMethod<T, R> {
   BeanDefinition<T> getBeanDefinition();

   static <T1, R1> BeanDefinitionMethodReference<T1, R1> of(BeanDefinition<T1> definition, ExecutableMethod<T1, R1> method) {
      return new DefaultBeanDefinitionMethodReference<>(definition, method);
   }
}
