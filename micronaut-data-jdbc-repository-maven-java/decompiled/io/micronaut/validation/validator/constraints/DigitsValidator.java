package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.math.BigDecimal;
import javax.validation.constraints.Digits;

@FunctionalInterface
public interface DigitsValidator<T> extends ConstraintValidator<Digits, T> {
   @Override
   default boolean isValid(@Nullable T value, @NonNull AnnotationValue<Digits> annotationMetadata, @NonNull ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      } else {
         int intMax = annotationMetadata.get("integer", Integer.TYPE).orElse(0);
         if (intMax < 0) {
            throw new IllegalArgumentException("The length of the integer part cannot be negative.");
         } else {
            int fracMax = annotationMetadata.get("fraction", Integer.TYPE).orElse(0);
            if (fracMax < 0) {
               throw new IllegalArgumentException("The length of the fraction part cannot be negative.");
            } else {
               BigDecimal bigDecimal;
               try {
                  bigDecimal = this.getBigDecimal(value);
               } catch (NumberFormatException var9) {
                  return false;
               }

               int intLen = bigDecimal.precision() - bigDecimal.scale();
               int fracLen = bigDecimal.scale() < 0 ? 0 : bigDecimal.scale();
               return intMax >= intLen && fracMax >= fracLen;
            }
         }
      }
   }

   BigDecimal getBigDecimal(@NonNull T value);
}
