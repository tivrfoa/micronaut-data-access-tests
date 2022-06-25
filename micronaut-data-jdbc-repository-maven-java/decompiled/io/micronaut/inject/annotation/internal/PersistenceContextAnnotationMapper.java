package io.micronaut.inject.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.annotation.NamedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Internal
public final class PersistenceContextAnnotationMapper implements NamedAnnotationMapper {
   private static final String SOURCE_ANNOTATION = "javax.persistence.PersistenceContext";

   @Override
   public String getName() {
      return "javax.persistence.PersistenceContext";
   }

   @Override
   public List<AnnotationValue<?>> map(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      String name = (String)annotation.stringValue("name").orElse(null);
      return name != null
         ? Arrays.asList(AnnotationValue.builder("javax.inject.Inject").build(), AnnotationValue.builder("javax.inject.Named").value(name).build())
         : Collections.singletonList(AnnotationValue.builder("javax.inject.Inject").build());
   }
}
