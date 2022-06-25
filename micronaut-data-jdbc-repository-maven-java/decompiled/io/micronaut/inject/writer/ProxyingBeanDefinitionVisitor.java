package io.micronaut.inject.writer;

public interface ProxyingBeanDefinitionVisitor extends BeanDefinitionVisitor {
   String getProxiedTypeName();

   String getProxiedBeanDefinitionName();
}
