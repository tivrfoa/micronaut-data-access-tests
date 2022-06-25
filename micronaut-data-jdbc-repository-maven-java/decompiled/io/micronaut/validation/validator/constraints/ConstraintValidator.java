package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.validation.ClockProvider;
import javax.validation.Constraint;

@Indexed(ConstraintValidator.class)
@FunctionalInterface
public interface ConstraintValidator<A extends Annotation, T> extends javax.validation.ConstraintValidator<A, T> {
   ConstraintValidator VALID = (value, annotationMetadata, context) -> true;

   boolean isValid(@Nullable T value, @NonNull AnnotationValue<A> annotationMetadata, @NonNull ConstraintValidatorContext context);

   @Override
   default boolean isValid(T value, javax.validation.ConstraintValidatorContext context) {
      return this.isValid(value, new AnnotationValue<>(Constraint.class.getName()), new ConstraintValidatorContext() {
         private String messageTemplate = context.getDefaultConstraintMessageTemplate();

         @NonNull
         @Override
         public ClockProvider getClockProvider() {
            return context.getClockProvider();
         }

         @Nullable
         @Override
         public Object getRootBean() {
            return null;
         }

         @Override
         public void messageTemplate(@Nullable final String messageTemplate) {
            this.messageTemplate = messageTemplate;
         }

         public Optional<String> getMessageTemplate() {
            return Optional.ofNullable(this.messageTemplate);
         }
      });
   }
}
