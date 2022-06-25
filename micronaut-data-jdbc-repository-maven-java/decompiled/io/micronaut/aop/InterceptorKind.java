package io.micronaut.aop;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.annotation.Annotation;

public enum InterceptorKind {
   AROUND(Around.class),
   AROUND_CONSTRUCT(AroundConstruct.class),
   INTRODUCTION(Introduction.class),
   POST_CONSTRUCT(PostConstruct.class),
   PRE_DESTROY(PreDestroy.class);

   private final Class<? extends Annotation> annotationType;

   private InterceptorKind(Class<? extends Annotation> annotationType) {
      this.annotationType = annotationType;
   }

   public Class<? extends Annotation> getAnnotationType() {
      return this.annotationType;
   }

   public String getAnnotationName() {
      return this.annotationType.getName();
   }
}
