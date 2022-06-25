package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.List;

public interface AnnotationTransformer<T extends Annotation> {
   List<AnnotationValue<?>> transform(AnnotationValue<T> annotation, VisitorContext visitorContext);
}
