package io.micronaut.context.processor;

import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import java.lang.annotation.Annotation;

public interface ExecutableMethodProcessor<A extends Annotation> extends AnnotationProcessor<A, ExecutableMethod<?, ?>> {
   void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method);
}
