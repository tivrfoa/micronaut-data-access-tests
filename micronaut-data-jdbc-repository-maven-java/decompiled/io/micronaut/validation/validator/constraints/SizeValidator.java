package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import javax.validation.constraints.Size;

@FunctionalInterface
public interface SizeValidator<T> extends ConstraintValidator<Size, T> {
   @Override
   default boolean isValid(@Nullable T value, @NonNull AnnotationValue<Size> annotationMetadata, @NonNull ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      } else {
         int len = this.getSize(value);
         int max = annotationMetadata.get("max", Integer.class).orElse(Integer.MAX_VALUE);
         int min = annotationMetadata.get("min", Integer.class).orElse(0);
         return len <= max && len >= min;
      }
   }

   int getSize(@NonNull T value);
}
