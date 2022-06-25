package io.micronaut.inject.annotation.internal;

import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.annotation.NamedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

@Internal
public class TimedAnnotationMapper implements NamedAnnotationMapper {
   @Override
   public String getName() {
      return "io.micrometer.core.annotation.Timed";
   }

   @Override
   public List<AnnotationValue<?>> map(AnnotationValue<Annotation> annotation, VisitorContext visitorContext) {
      return Collections.singletonList(
         AnnotationValue.builder("io.micronaut.aop.InterceptorBinding")
            .member("value", new AnnotationClassValue(this.getName()))
            .member("kind", "AROUND")
            .build()
      );
   }
}
