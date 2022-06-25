package io.micronaut.inject.beans.visitor.jakarta.persistence;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.inject.annotation.NamedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

@Internal
public class JakartaEntityIntrospectedAnnotationMapper implements NamedAnnotationMapper {
   @NonNull
   @Override
   public String getName() {
      return "jakarta.persistence.Entity";
   }

   @Override
   public List<AnnotationValue<?>> map(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      AnnotationValueBuilder<Introspected> builder = AnnotationValue.<Introspected>builder(Introspected.class)
         .member("excludedAnnotations", "jakarta.persistence.Transient");
      return Arrays.asList(builder.build(), AnnotationValue.builder(ReflectiveAccess.class).build());
   }
}
