package io.micronaut.management.endpoint.beans;

import io.micronaut.inject.BeanDefinition;
import java.util.Collection;
import org.reactivestreams.Publisher;

public interface BeanDefinitionDataCollector<T> {
   Publisher<T> getData(Collection<BeanDefinition<?>> beanDefinitions);
}
