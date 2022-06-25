package io.micronaut.inject.qualifiers;

import io.micronaut.context.annotation.Any;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.inject.BeanType;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

@Internal
final class QualifierUtils {
   private QualifierUtils() {
   }

   static <T> boolean matchType(Class<T> beanType, BeanType<T> candidate) {
      return candidate.isContainerType() || beanType.isAssignableFrom(beanType);
   }

   static <T> boolean matchAny(Class<T> beanType, BeanType<T> candidate) {
      return beanType != Object.class && candidate.getAnnotationMetadata().hasDeclaredAnnotation(Any.class);
   }

   static <T> boolean matchByCandidateName(BeanType<T> candidate, Class<T> beanType, String value) {
      String definedCandidateName;
      if (candidate instanceof NameResolver) {
         Optional<String> resolvedName = ((NameResolver)candidate).resolveName();
         definedCandidateName = (String)resolvedName.orElse(candidate.getBeanType().getSimpleName());
      } else {
         definedCandidateName = candidate.getBeanType().getSimpleName();
      }

      return definedCandidateName.equalsIgnoreCase(value) || definedCandidateName.equalsIgnoreCase(value + beanType.getSimpleName());
   }

   public static boolean annotationQualifiersEquals(@NonNull Object o1, @NonNull Object o2) {
      Entry<String, Map<CharSequence, Object>> val1 = extractAnnotationAndBindingValues(o1);
      if (val1 == null) {
         return false;
      } else {
         Entry<String, Map<CharSequence, Object>> val2 = extractAnnotationAndBindingValues(o2);
         if (val2 == null) {
            return false;
         } else {
            return Objects.equals(val1.getKey(), val2.getKey()) && Objects.equals(val1.getValue(), val2.getValue());
         }
      }
   }

   @Nullable
   private static Entry<String, Map<CharSequence, Object>> extractAnnotationAndBindingValues(@NonNull Object o) {
      if (o instanceof NamedAnnotationStereotypeQualifier) {
         NamedAnnotationStereotypeQualifier<?> that = (NamedAnnotationStereotypeQualifier)o;
         return new SimpleEntry(that.stereotype, null);
      } else if (o instanceof AnnotationStereotypeQualifier) {
         AnnotationStereotypeQualifier<?> that = (AnnotationStereotypeQualifier)o;
         return new SimpleEntry(that.stereotype.getName(), null);
      } else if (o instanceof AnnotationMetadataQualifier) {
         AnnotationMetadataQualifier<?> that = (AnnotationMetadataQualifier)o;
         return that.qualifierAnn == null ? new SimpleEntry(that.annotationName, null) : new SimpleEntry(that.annotationName, that.qualifierAnn.getValues());
      } else if (o instanceof AnnotationQualifier) {
         AnnotationQualifier<?> that = (AnnotationQualifier)o;
         return new SimpleEntry(that.annotation.annotationType().getName(), null);
      } else {
         return null;
      }
   }
}
