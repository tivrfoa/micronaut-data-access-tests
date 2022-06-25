package io.micronaut.context.processor;

import io.micronaut.inject.BeanDefinition;
import java.lang.annotation.Annotation;

public interface AnnotationProcessor<A extends Annotation, T> {
   void process(BeanDefinition<?> beanDefinition, T object);
}
