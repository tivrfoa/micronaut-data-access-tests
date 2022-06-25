package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import java.math.BigDecimal;
import javax.validation.ValidationException;
import javax.validation.constraints.DecimalMax;

public interface DecimalMaxValidator<T> extends ConstraintValidator<DecimalMax, T> {
   @Override
   default boolean isValid(@Nullable T value, @NonNull AnnotationValue<DecimalMax> annotationMetadata, @NonNull ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      } else {
         BigDecimal bigDecimal = (BigDecimal)annotationMetadata.getValue(String.class)
            .map(
               s -> (BigDecimal)ConversionService.SHARED
                     .convert(s, BigDecimal.class)
                     .orElseThrow(() -> new ValidationException(s + " does not represent a valid BigDecimal format."))
            )
            .orElseThrow(() -> new ValidationException("null does not represent a valid BigDecimal format."));

         int result;
         try {
            result = this.doComparison(value, bigDecimal);
         } catch (NumberFormatException var7) {
            return false;
         }

         boolean inclusive = annotationMetadata.get("inclusive", Boolean.TYPE).orElse(true);
         return inclusive ? result <= 0 : result < 0;
      }
   }

   int doComparison(@NonNull T value, @NonNull BigDecimal bigDecimal);
}
