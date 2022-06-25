package io.micronaut.inject.beans.visitor;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.annotation.NamedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class JsonCreatorAnnotationMapper implements NamedAnnotationMapper {
   @NonNull
   @Override
   public String getName() {
      return "com.fasterxml.jackson.annotation.JsonCreator";
   }

   @Override
   public List<AnnotationValue<?>> map(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      return Collections.singletonList(AnnotationValue.builder(Creator.class).build());
   }
}
