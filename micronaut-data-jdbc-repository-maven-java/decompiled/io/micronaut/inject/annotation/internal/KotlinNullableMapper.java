package io.micronaut.inject.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.annotation.NamedAnnotationTransformer;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class KotlinNullableMapper implements NamedAnnotationTransformer {
   @NonNull
   @Override
   public String getName() {
      return "org.jetbrains.annotations.Nullable";
   }

   @Override
   public List<AnnotationValue<?>> transform(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      return Collections.singletonList(AnnotationValue.builder("javax.annotation.Nullable").build());
   }
}
