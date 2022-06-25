package io.micronaut.inject.annotation;

import java.lang.annotation.Annotation;

public interface TypedAnnotationMapper<T extends Annotation> extends AnnotationMapper<T> {
   Class<T> annotationType();
}
