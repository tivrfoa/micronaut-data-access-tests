package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.DelegatingBeanDefinition;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
class AnnotationMetadataQualifier<T> implements Qualifier<T> {
   @NonNull
   final String annotationName;
   @NonNull
   final String annotationSimpleName;
   @Nullable
   final AnnotationValue<Annotation> qualifierAnn;

   private AnnotationMetadataQualifier(@NonNull String annotationName, @NonNull String annotationSimpleName, @Nullable AnnotationValue<Annotation> value) {
      this.annotationName = annotationName;
      this.annotationSimpleName = annotationSimpleName;
      this.qualifierAnn = value;
   }

   static <T> AnnotationMetadataQualifier<T> fromType(@NonNull AnnotationMetadata annotationMetadata, @NonNull Class<? extends Annotation> annotationType) {
      return new AnnotationMetadataQualifier<>(
         annotationType.getName(), annotationType.getSimpleName(), resolveBindingAnnotationValue(annotationMetadata, annotationType.getName())
      );
   }

   static <T> AnnotationMetadataQualifier<T> fromTypeName(@NonNull AnnotationMetadata annotationMetadata, @NonNull String annotationTypeName) {
      return new AnnotationMetadataQualifier<>(
         annotationTypeName, NameUtils.getSimpleName(annotationTypeName), resolveBindingAnnotationValue(annotationMetadata, annotationTypeName)
      );
   }

   static <T extends Annotation> AnnotationMetadataQualifier<T> fromValue(
      @NonNull AnnotationMetadata annotationMetadata, @NonNull AnnotationValue<T> annotationValue
   ) {
      return new AnnotationMetadataQualifier(
         annotationValue.getAnnotationName(),
         NameUtils.getSimpleName(annotationValue.getAnnotationName()),
         resolveBindingAnnotationValue(annotationMetadata, annotationValue.getAnnotationName(), annotationValue.getValues())
      );
   }

   @Override
   public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
      return candidates.filter(candidate -> {
         if (!QualifierUtils.matchType(beanType, candidate)) {
            return false;
         } else if (QualifierUtils.matchAny(beanType, candidate)) {
            return true;
         } else {
            if (candidate instanceof BeanDefinition) {
               BeanDefinition<T> bdCandidate = (BeanDefinition)candidate;
               Qualifier<T> candidateDeclaredQualifier = bdCandidate.getDeclaredQualifier();
               if (candidateDeclaredQualifier != null && candidateDeclaredQualifier.contains(this)) {
                  return true;
               }

               if (candidate instanceof DelegatingBeanDefinition && this.matchByAnnotationMetadata((BT)candidate)) {
                  return true;
               }
            } else if (this.matchByAnnotationMetadata((BT)candidate)) {
               return true;
            }

            return QualifierUtils.matchByCandidateName(candidate, beanType, this.annotationSimpleName);
         }
      });
   }

   private <BT extends BeanType<T>> boolean matchByAnnotationMetadata(BT candidate) {
      return this.qualifierAnn == null
         ? candidate.getAnnotationMetadata().hasAnnotation(this.annotationName)
         : this.qualifierAnn.equals(this.resolveBindingAnnotationValue(candidate.getAnnotationMetadata()));
   }

   @Nullable
   private <K extends Annotation> AnnotationValue<K> resolveBindingAnnotationValue(AnnotationMetadata annotationMetadata) {
      return resolveBindingAnnotationValue(annotationMetadata, this.annotationName, annotationMetadata.getValues(this.annotationName));
   }

   @Nullable
   private static <K extends Annotation> AnnotationValue<K> resolveBindingAnnotationValue(AnnotationMetadata annotationMetadata, String annotationName) {
      return resolveBindingAnnotationValue(annotationMetadata, annotationName, annotationMetadata.getValues(annotationName));
   }

   @Nullable
   private static <K extends Annotation> AnnotationValue<K> resolveBindingAnnotationValue(
      AnnotationMetadata annotationMetadata, String annotationName, Map<CharSequence, Object> values
   ) {
      Map<CharSequence, Object> bindingValues = resolveBindingValues(annotationMetadata, values);
      return CollectionUtils.isNotEmpty(bindingValues) ? new AnnotationValue<>(annotationName, bindingValues) : null;
   }

   @Nullable
   private static Map<CharSequence, Object> resolveBindingValues(AnnotationMetadata annotationMetadata, Map<CharSequence, Object> values) {
      Set<String> nonBinding = resolveNonBindingMembers(annotationMetadata);
      if (!values.isEmpty() && !nonBinding.isEmpty()) {
         Map<CharSequence, Object> map = new HashMap();

         for(Entry<CharSequence, Object> entry : values.entrySet()) {
            if (!nonBinding.contains(((CharSequence)entry.getKey()).toString()) && map.put(entry.getKey(), entry.getValue()) != null) {
               throw new IllegalStateException("Duplicate key: " + entry.getKey());
            }
         }

         return map;
      } else {
         return values;
      }
   }

   @NonNull
   private static Set<String> resolveNonBindingMembers(AnnotationMetadata annotationMetadata) {
      String[] nonBindingArray = annotationMetadata.stringValues("javax.inject.Qualifier", "nonBinding");
      return (Set<String>)(ArrayUtils.isNotEmpty(nonBindingArray) ? new HashSet(Arrays.asList(nonBindingArray)) : Collections.emptySet());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o == null ? false : QualifierUtils.annotationQualifiersEquals(this, o);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.annotationName, this.qualifierAnn});
   }

   public String toString() {
      return this.qualifierAnn != null
         ? "@"
            + this.annotationSimpleName
            + "("
            + (String)this.qualifierAnn
               .getValues()
               .entrySet()
               .stream()
               .map(entry -> entry.getKey() + "=" + this.valueToString(entry))
               .collect(Collectors.joining(", "))
            + ")"
         : "@" + this.annotationSimpleName;
   }

   private Object valueToString(Entry<CharSequence, Object> entry) {
      Object v = entry.getValue();
      return v instanceof Object[] ? Arrays.toString(v) : v;
   }
}
