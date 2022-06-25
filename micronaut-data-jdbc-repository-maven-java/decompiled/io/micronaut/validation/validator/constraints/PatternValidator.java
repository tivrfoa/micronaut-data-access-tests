package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import javax.validation.constraints.Pattern;

@Singleton
public class PatternValidator extends AbstractPatternValidator<Pattern> {
   public boolean isValid(@Nullable CharSequence value, @NonNull AnnotationValue<Pattern> annotationMetadata, @NonNull ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      } else {
         java.util.regex.Pattern regex = this.getPattern(annotationMetadata, false);
         return regex.matcher(value).matches();
      }
   }
}
