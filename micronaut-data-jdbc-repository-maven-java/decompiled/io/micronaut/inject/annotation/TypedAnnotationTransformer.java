package io.micronaut.inject.annotation;

import java.lang.annotation.Annotation;

public interface TypedAnnotationTransformer<T extends Annotation> extends AnnotationTransformer<T> {
   Class<T> annotationType();
}
