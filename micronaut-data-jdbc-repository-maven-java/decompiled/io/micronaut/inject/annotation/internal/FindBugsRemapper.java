package io.micronaut.inject.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.inject.annotation.AnnotationRemapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.Collections;
import java.util.List;

@Internal
public final class FindBugsRemapper implements AnnotationRemapper {
   @NonNull
   @Override
   public String getPackageName() {
      return "edu.umd.cs.findbugs.annotations";
   }

   @NonNull
   @Override
   public List<AnnotationValue<?>> remap(AnnotationValue<?> annotation, VisitorContext visitorContext) {
      String simpleName = NameUtils.getSimpleName(annotation.getAnnotationName());
      if ("nullable".equalsIgnoreCase(simpleName)) {
         return Collections.singletonList(AnnotationValue.builder("javax.annotation.Nullable").build());
      } else {
         return "nonnull".equalsIgnoreCase(simpleName)
            ? Collections.singletonList(AnnotationValue.builder("javax.annotation.Nonnull").build())
            : Collections.singletonList(annotation);
      }
   }
}
