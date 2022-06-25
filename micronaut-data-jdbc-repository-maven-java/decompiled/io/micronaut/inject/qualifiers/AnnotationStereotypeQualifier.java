package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanType;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.stream.Stream;

@Internal
final class AnnotationStereotypeQualifier<T> implements Qualifier<T> {
   final Class<? extends Annotation> stereotype;

   AnnotationStereotypeQualifier(Class<? extends Annotation> stereotype) {
      this.stereotype = stereotype;
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> candidate.getAnnotationMetadata().hasStereotype(this.stereotype));
   }

   public String toString() {
      return "@" + this.stereotype.getSimpleName();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o == null ? false : QualifierUtils.annotationQualifiersEquals(this, o);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.stereotype.getName()});
   }
}
