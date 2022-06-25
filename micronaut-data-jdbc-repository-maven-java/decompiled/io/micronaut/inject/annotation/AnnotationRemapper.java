package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.List;

public interface AnnotationRemapper {
   @NonNull
   String getPackageName();

   @NonNull
   List<AnnotationValue<?>> remap(AnnotationValue<?> annotation, VisitorContext visitorContext);
}
