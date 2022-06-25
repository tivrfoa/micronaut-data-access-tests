package io.micronaut.inject.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.annotation.NamedAnnotationTransformer;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

@Internal
public final class JakartaPostConstructTransformer implements NamedAnnotationTransformer {
   @NonNull
   @Override
   public String getName() {
      return "jakarta.annotation.PostConstruct";
   }

   @Override
   public List<AnnotationValue<?>> transform(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      return Collections.singletonList(AnnotationValue.builder("javax.annotation.PostConstruct").build());
   }
}
