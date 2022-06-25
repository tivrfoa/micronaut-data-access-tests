package io.micronaut.inject;

public interface ProxyBeanDefinition<T> extends BeanDefinition<T> {
   Class<BeanDefinition<T>> getTargetDefinitionType();

   Class<T> getTargetType();
}
