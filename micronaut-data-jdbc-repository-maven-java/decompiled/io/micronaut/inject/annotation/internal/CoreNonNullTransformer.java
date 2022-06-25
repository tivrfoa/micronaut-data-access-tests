package io.micronaut.inject.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.annotation.NamedAnnotationTransformer;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class CoreNonNullTransformer implements NamedAnnotationTransformer {
   @Override
   public String getName() {
      return "io.micronaut.core.annotation.NonNull";
   }

   @Override
   public List<AnnotationValue<?>> transform(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      return Collections.singletonList(AnnotationValue.builder("javax.annotation.Nonnull").build());
   }
}
