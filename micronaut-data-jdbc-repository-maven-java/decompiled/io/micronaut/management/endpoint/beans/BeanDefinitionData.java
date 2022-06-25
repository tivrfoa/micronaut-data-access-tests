package io.micronaut.management.endpoint.beans;

import io.micronaut.inject.BeanDefinition;

public interface BeanDefinitionData<T> {
   T getData(BeanDefinition<?> beanDefinition);
}
