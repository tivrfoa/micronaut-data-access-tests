package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanType;
import java.util.Objects;
import java.util.stream.Stream;

@Internal
final class NamedAnnotationStereotypeQualifier<T> implements Qualifier<T> {
   final String stereotype;

   NamedAnnotationStereotypeQualifier(String stereotype) {
      this.stereotype = (String)Objects.requireNonNull(stereotype, "Stereotype cannot be null");
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> candidate.getAnnotationMetadata().hasStereotype(this.stereotype));
   }

   public String toString() {
      return "@" + this.stereotype;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o == null ? false : QualifierUtils.annotationQualifiersEquals(this, o);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.stereotype});
   }
}
