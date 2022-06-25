package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.Email;

@Singleton
public class EmailValidator extends AbstractPatternValidator<Email> {
   private static final int MAX_LOCAL_PART_LENGTH = 64;
   private static final String LOCAL_PART_ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~\u0080-\uffff-]";
   private static final String LOCAL_PART_INSIDE_QUOTES_ATOM = "([a-z0-9!#$%&'*.(),<>\\[\\]:;  @+/=?^_`{|}~\u0080-\uffff-]|\\\\\\\\|\\\\\\\")";
   private static final Pattern LOCAL_PART_PATTERN = Pattern.compile(
      "([a-z0-9!#$%&'*+/=?^_`{|}~\u0080-\uffff-]+|\"([a-z0-9!#$%&'*.(),<>\\[\\]:;  @+/=?^_`{|}~\u0080-\uffff-]|\\\\\\\\|\\\\\\\")+\")(\\.([a-z0-9!#$%&'*+/=?^_`{|}~\u0080-\uffff-]+|\"([a-z0-9!#$%&'*.(),<>\\[\\]:;  @+/=?^_`{|}~\u0080-\uffff-]|\\\\\\\\|\\\\\\\")+\"))*",
      2
   );

   public boolean isValid(@Nullable CharSequence value, @NonNull AnnotationValue<Email> annotationMetadata, @NonNull ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      } else {
         String stringValue = value.toString();
         int i = stringValue.lastIndexOf(64);
         if (i < 0) {
            return false;
         } else {
            String localPart = stringValue.substring(0, i);
            String domainPart = stringValue.substring(i + 1);
            boolean isValid;
            if (!this.isValidEmailLocalPart(localPart)) {
               isValid = false;
            } else {
               isValid = DomainNameUtil.isValidEmailDomainAddress(domainPart);
            }

            Pattern pattern = this.getPattern(annotationMetadata, true);
            if (pattern != null && isValid) {
               Matcher m = pattern.matcher(value);
               return m.matches();
            } else {
               return isValid;
            }
         }
      }
   }

   private boolean isValidEmailLocalPart(String localPart) {
      if (localPart.length() > 64) {
         return false;
      } else {
         Matcher matcher = LOCAL_PART_PATTERN.matcher(localPart);
         return matcher.matches();
      }
   }
}
