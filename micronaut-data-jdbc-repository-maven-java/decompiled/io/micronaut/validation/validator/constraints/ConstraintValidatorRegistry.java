package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.NonNull;
import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.validation.ValidationException;

public interface ConstraintValidatorRegistry {
   @NonNull
   <A extends Annotation, T> Optional<ConstraintValidator<A, T>> findConstraintValidator(@NonNull Class<A> constraintType, @NonNull Class<T> targetType);

   @NonNull
   default <A extends Annotation, T> ConstraintValidator<A, T> getConstraintValidator(@NonNull Class<A> constraintType, @NonNull Class<T> targetType) {
      return (ConstraintValidator<A, T>)this.findConstraintValidator(constraintType, targetType)
         .orElseThrow(
            () -> new ValidationException("No constraint validator present able to validate constraint [" + constraintType + "] on type: " + targetType)
         );
   }
}
