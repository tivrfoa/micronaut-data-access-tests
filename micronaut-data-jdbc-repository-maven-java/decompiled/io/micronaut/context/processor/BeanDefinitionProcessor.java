package io.micronaut.context.processor;

import io.micronaut.context.BeanContext;
import java.lang.annotation.Annotation;

public interface BeanDefinitionProcessor<A extends Annotation> extends AnnotationProcessor<A, BeanContext> {
}
