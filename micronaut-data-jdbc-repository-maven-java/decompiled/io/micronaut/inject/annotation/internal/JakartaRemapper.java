package io.micronaut.inject.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.annotation.AnnotationRemapper;
import io.micronaut.inject.visitor.VisitorContext;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Internal
public final class JakartaRemapper implements AnnotationRemapper {
   private static final Pattern JAKARTA = Pattern.compile("^jakarta");

   @NonNull
   @Override
   public String getPackageName() {
      return "jakarta.inject";
   }

   @NonNull
   @Override
   public List<AnnotationValue<?>> remap(AnnotationValue<?> annotation, VisitorContext visitorContext) {
      String name = annotation.getAnnotationName();
      Matcher matcher = JAKARTA.matcher(name);
      AnnotationValue<?> stereotype = null;
      if (name.equals(Named.class.getName())) {
         stereotype = AnnotationValue.builder("javax.inject.Qualifier").build();
      } else if (name.equals(Singleton.class.getName())) {
         stereotype = AnnotationValue.builder("javax.inject.Scope").build();
      }

      return Collections.singletonList(AnnotationValue.builder(matcher.replaceFirst("javax")).members(annotation.getValues()).stereotype(stereotype).build());
   }
}
