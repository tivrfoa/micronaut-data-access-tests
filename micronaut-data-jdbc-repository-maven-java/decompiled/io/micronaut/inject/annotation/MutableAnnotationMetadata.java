package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MutableAnnotationMetadata extends DefaultAnnotationMetadata {
   private boolean hasPropertyExpressions = false;

   public MutableAnnotationMetadata() {
   }

   private MutableAnnotationMetadata(
      @Nullable Map<String, Map<CharSequence, Object>> declaredAnnotations,
      @Nullable Map<String, Map<CharSequence, Object>> declaredStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allAnnotations,
      @Nullable Map<String, List<String>> annotationsByStereotype,
      boolean hasPropertyExpressions
   ) {
      super(declaredAnnotations, declaredStereotypes, allStereotypes, allAnnotations, annotationsByStereotype, hasPropertyExpressions);
      this.hasPropertyExpressions = hasPropertyExpressions;
   }

   @Override
   public boolean hasPropertyExpressions() {
      return this.hasPropertyExpressions;
   }

   public MutableAnnotationMetadata clone() {
      MutableAnnotationMetadata cloned = new MutableAnnotationMetadata(
         this.declaredAnnotations != null ? this.cloneMapOfMapValue(this.declaredAnnotations) : null,
         this.declaredStereotypes != null ? this.cloneMapOfMapValue(this.declaredStereotypes) : null,
         this.allStereotypes != null ? this.cloneMapOfMapValue(this.allStereotypes) : null,
         this.allAnnotations != null ? this.cloneMapOfMapValue(this.allAnnotations) : null,
         this.annotationsByStereotype != null ? this.cloneMapOfListValue(this.annotationsByStereotype) : null,
         this.hasPropertyExpressions
      );
      if (this.annotationDefaultValues != null) {
         cloned.annotationDefaultValues = new LinkedHashMap(this.annotationDefaultValues);
      }

      if (this.repeated != null) {
         cloned.repeated = new HashMap(this.repeated);
      }

      return cloned;
   }

   @NonNull
   @Override
   public Map<String, Object> getDefaultValues(@NonNull String annotation) {
      Map<String, Object> values = super.getDefaultValues(annotation);
      if (values.isEmpty() && this.annotationDefaultValues != null) {
         Map<CharSequence, Object> compileTimeDefaults = (Map)this.annotationDefaultValues.get(annotation);
         if (compileTimeDefaults != null && !compileTimeDefaults.isEmpty()) {
            return (Map<String, Object>)compileTimeDefaults.entrySet()
               .stream()
               .collect(Collectors.toMap(e -> ((CharSequence)e.getKey()).toString(), Entry::getValue));
         }
      }

      return values;
   }

   @Override
   public <A extends Annotation> void removeAnnotationIf(@NonNull Predicate<AnnotationValue<A>> predicate) {
      super.removeAnnotationIf(predicate);
   }

   @Override
   public void removeAnnotation(String annotationType) {
      super.removeAnnotation(annotationType);
   }

   @Override
   public void removeStereotype(String annotationType) {
      super.removeStereotype(annotationType);
   }

   @Override
   public void addAnnotation(String annotation, Map<CharSequence, Object> values) {
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(values, RetentionPolicy.RUNTIME);
      super.addAnnotation(annotation, values);
   }

   @Override
   public void addAnnotation(String annotation, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(values, retentionPolicy);
      super.addAnnotation(annotation, values, retentionPolicy);
   }

   @Override
   public void addRepeatableStereotype(List<String> parents, String stereotype, AnnotationValue annotationValue) {
      Objects.requireNonNull(annotationValue, "Annotation Value cannot be null");
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(annotationValue.getValues(), RetentionPolicy.RUNTIME);
      super.addRepeatableStereotype(parents, stereotype, annotationValue);
   }

   @Override
   public void addDeclaredRepeatableStereotype(List<String> parents, String stereotype, AnnotationValue annotationValue) {
      Objects.requireNonNull(annotationValue, "Annotation Value cannot be null");
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(annotationValue.getValues(), RetentionPolicy.RUNTIME);
      super.addDeclaredRepeatableStereotype(parents, stereotype, annotationValue);
   }

   @Override
   public void addDeclaredAnnotation(String annotation, Map<CharSequence, Object> values) {
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(values, RetentionPolicy.RUNTIME);
      super.addDeclaredAnnotation(annotation, values);
   }

   @Override
   public void addDeclaredAnnotation(String annotation, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(values, retentionPolicy);
      super.addDeclaredAnnotation(annotation, values, retentionPolicy);
   }

   @Override
   public void addRepeatable(String annotationName, AnnotationValue annotationValue) {
      Objects.requireNonNull(annotationValue, "Annotation Value cannot be null");
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(annotationValue.getValues(), RetentionPolicy.RUNTIME);
      super.addRepeatable(annotationName, annotationValue);
   }

   @Override
   public void addRepeatable(String annotationName, AnnotationValue annotationValue, RetentionPolicy retentionPolicy) {
      Objects.requireNonNull(annotationValue, "Annotation Value cannot be null");
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(annotationValue.getValues(), retentionPolicy);
      super.addRepeatable(annotationName, annotationValue, retentionPolicy);
   }

   @Override
   public void addDeclaredRepeatable(String annotationName, AnnotationValue annotationValue) {
      Objects.requireNonNull(annotationValue, "Annotation Value cannot be null");
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(annotationValue.getValues(), RetentionPolicy.RUNTIME);
      super.addDeclaredRepeatable(annotationName, annotationValue);
   }

   @Override
   public void addDeclaredRepeatable(String annotationName, AnnotationValue annotationValue, RetentionPolicy retentionPolicy) {
      Objects.requireNonNull(annotationValue, "Annotation Value cannot be null");
      this.hasPropertyExpressions = this.computeHasPropertyExpressions(annotationValue.getValues(), retentionPolicy);
      super.addDeclaredRepeatable(annotationName, annotationValue, retentionPolicy);
   }

   @Override
   public void addDeclaredStereotype(List<String> parentAnnotations, String stereotype, Map<CharSequence, Object> values) {
      super.addDeclaredStereotype(parentAnnotations, stereotype, values);
   }

   @Override
   public void addDeclaredStereotype(List<String> parentAnnotations, String stereotype, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      super.addDeclaredStereotype(parentAnnotations, stereotype, values, retentionPolicy);
   }

   private boolean computeHasPropertyExpressions(Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      return this.hasPropertyExpressions || values != null && retentionPolicy == RetentionPolicy.RUNTIME && this.hasPropertyExpressions(values);
   }

   private boolean hasPropertyExpressions(Map<CharSequence, Object> values) {
      return CollectionUtils.isEmpty(values) ? false : values.values().stream().anyMatch(v -> {
         if (v instanceof CharSequence) {
            return v.toString().contains("${");
         } else if (v instanceof String[]) {
            return Arrays.stream((String[])v).anyMatch(s -> s.contains("${"));
         } else if (v instanceof AnnotationValue) {
            return this.hasPropertyExpressions(((AnnotationValue)v).getValues());
         } else if (v instanceof AnnotationValue[]) {
            AnnotationValue[] a = (AnnotationValue[])v;
            return a.length > 0 ? Arrays.stream(a).anyMatch(av -> this.hasPropertyExpressions(av.getValues())) : false;
         } else {
            return false;
         }
      });
   }
}
