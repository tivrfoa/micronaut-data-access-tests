package io.micronaut.inject.beans.visitor;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.inject.annotation.NamedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class EntityReflectiveAccessAnnotationMapper implements NamedAnnotationMapper {
   @NonNull
   @Override
   public String getName() {
      return "javax.persistence.Entity";
   }

   @Override
   public List<AnnotationValue<?>> map(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      AnnotationValueBuilder<ReflectiveAccess> builder = AnnotationValue.builder(ReflectiveAccess.class);
      return Collections.singletonList(builder.build());
   }
}
