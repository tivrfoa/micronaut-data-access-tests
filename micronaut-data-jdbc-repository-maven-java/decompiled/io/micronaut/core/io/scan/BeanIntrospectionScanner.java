package io.micronaut.core.io.scan;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.util.StringUtils;
import java.util.Objects;
import java.util.stream.Stream;

@Internal
public class BeanIntrospectionScanner implements AnnotationScanner {
   @NonNull
   @Override
   public Stream<Class<?>> scan(@NonNull String annotation, @NonNull String pkg) {
      Objects.requireNonNull(annotation, "Annotation type cannot be null");
      Objects.requireNonNull(pkg, "Package to scan cannot be null");
      if (StringUtils.isNotEmpty(pkg)) {
         String prefix = pkg + ".";
         return BeanIntrospector.SHARED
            .findIntrospectedTypes(
               ref -> ref.getAnnotationMetadata().hasStereotype(annotation) && ref.isPresent() && ref.getBeanType().getName().startsWith(prefix)
            )
            .stream();
      } else {
         return Stream.empty();
      }
   }
}
