package io.micronaut.core.annotation;

import java.lang.annotation.Annotation;

public interface AnnotationValueProvider<A extends Annotation> {
   @NonNull
   AnnotationValue<A> annotationValue();
}
