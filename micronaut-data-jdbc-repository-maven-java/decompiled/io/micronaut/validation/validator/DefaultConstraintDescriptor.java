package io.micronaut.validation.validator;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ValidateUnwrappedValue;

@Internal
class DefaultConstraintDescriptor<T extends Annotation> implements ConstraintDescriptor<T> {
   private final AnnotationValue<T> annotationValue;
   private final AnnotationMetadata annotationMetadata;
   private final Class<T> type;

   DefaultConstraintDescriptor(AnnotationMetadata annotationMetadata, Class<T> type, AnnotationValue<T> annotationValue) {
      this.annotationValue = annotationValue;
      this.annotationMetadata = annotationMetadata;
      this.type = type;
   }

   @Override
   public T getAnnotation() {
      return this.annotationMetadata.synthesize(this.type);
   }

   @Override
   public String getMessageTemplate() {
      return (String)this.annotationValue.get("groups", String.class).orElse(null);
   }

   @Override
   public Set<Class<?>> getGroups() {
      return (Set<Class<?>>)this.annotationValue.get("groups", Argument.setOf(Class.class)).orElse(Collections.emptySet());
   }

   @Override
   public Set<Class<? extends Payload>> getPayload() {
      return (Set<Class<? extends Payload>>)this.annotationValue.get("payload", Argument.setOf(Class.class)).orElse(Collections.emptySet());
   }

   @Override
   public ConstraintTarget getValidationAppliesTo() {
      return ConstraintTarget.IMPLICIT;
   }

   @Override
   public List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses() {
      return Collections.emptyList();
   }

   @Override
   public Map<String, Object> getAttributes() {
      return (Map<String, Object>)this.annotationValue
         .getValues()
         .entrySet()
         .stream()
         .collect(Collectors.toMap(entry -> ((CharSequence)entry.getKey()).toString(), Entry::getValue));
   }

   @Override
   public Set<ConstraintDescriptor<?>> getComposingConstraints() {
      return Collections.emptySet();
   }

   @Override
   public boolean isReportAsSingleViolation() {
      return false;
   }

   @Override
   public ValidateUnwrappedValue getValueUnwrapping() {
      return ValidateUnwrappedValue.DEFAULT;
   }

   @Override
   public Object unwrap(Class type) {
      throw new UnsupportedOperationException("Unwrapping unsupported");
   }
}
