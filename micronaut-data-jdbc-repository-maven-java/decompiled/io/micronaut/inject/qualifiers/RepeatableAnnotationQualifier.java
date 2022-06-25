package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.BeanType;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

final class RepeatableAnnotationQualifier<T> implements Qualifier<T> {
   private final List<AnnotationValue<Annotation>> repeatableValues;
   private final String repeatableName;

   RepeatableAnnotationQualifier(AnnotationMetadata annotationMetadata, String repeatableName) {
      this.repeatableName = repeatableName;
      this.repeatableValues = (List)annotationMetadata.findAnnotation(repeatableName).map(av -> av.getAnnotations("value")).orElse(Collections.emptyList());
      if (this.repeatableValues.isEmpty()) {
         throw new IllegalArgumentException("Repeatable qualifier [" + repeatableName + "] declared with no values");
      }
   }

   public String toString() {
      return Arrays.toString(this.repeatableValues.toArray());
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> {
         AnnotationValue<Annotation> declared = candidate.getAnnotationMetadata().getAnnotation(this.repeatableName);
         if (declared != null) {
            List<AnnotationValue<Annotation>> repeated = declared.getAnnotations("value");
            return repeated.containsAll(this.repeatableValues);
         } else {
            return false;
         }
      });
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RepeatableAnnotationQualifier<?> that = (RepeatableAnnotationQualifier)o;
         return this.repeatableValues.equals(that.repeatableValues) && this.repeatableName.equals(that.repeatableName);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.repeatableValues, this.repeatableName});
   }
}
