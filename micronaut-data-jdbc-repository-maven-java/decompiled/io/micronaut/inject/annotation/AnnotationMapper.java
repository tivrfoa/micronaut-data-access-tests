package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.List;

public interface AnnotationMapper<T extends Annotation> {
   List<AnnotationValue<?>> map(AnnotationValue<T> annotation, VisitorContext visitorContext);
}
