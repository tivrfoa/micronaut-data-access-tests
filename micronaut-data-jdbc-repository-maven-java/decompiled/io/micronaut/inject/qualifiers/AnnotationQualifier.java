package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.BeanType;
import java.lang.annotation.Annotation;
import java.util.stream.Stream;

@Internal
class AnnotationQualifier<T> implements Qualifier<T> {
   final Annotation annotation;

   AnnotationQualifier(Annotation annotation) {
      this.annotation = annotation;
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      String qualifiedName = this.annotation.annotationType().getName();
      String annotationSimpleName = this.annotation.annotationType().getSimpleName();
      return candidates.filter(
         candidate -> {
            if (!QualifierUtils.matchType(beanType, candidate)) {
               return false;
            } else if (QualifierUtils.matchAny(beanType, candidate)) {
               return true;
            } else {
               return candidate.getAnnotationMetadata().hasDeclaredAnnotation(qualifiedName)
                  ? true
                  : QualifierUtils.matchByCandidateName(candidate, beanType, annotationSimpleName);
            }
         }
      );
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() ? QualifierUtils.annotationQualifiersEquals(this, o) : false;
      }
   }

   public int hashCode() {
      return this.annotation.hashCode();
   }

   public String toString() {
      return '@' + this.annotation.annotationType().getSimpleName();
   }
}
