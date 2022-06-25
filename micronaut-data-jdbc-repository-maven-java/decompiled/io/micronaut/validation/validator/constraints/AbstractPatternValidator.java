package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.PatternSyntaxException;
import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

abstract class AbstractPatternValidator<A extends Annotation> implements ConstraintValidator<A, CharSequence> {
   private static final Pattern.Flag[] ZERO_FLAGS = new Pattern.Flag[0];
   private static final Map<AbstractPatternValidator.PatternKey, java.util.regex.Pattern> COMPUTED_PATTERNS = new ConcurrentHashMap(10);

   java.util.regex.Pattern getPattern(@NonNull AnnotationValue<?> annotationMetadata, boolean isOptional) {
      Optional<String> regexp = annotationMetadata.get("regexp", String.class);
      String pattern;
      if (isOptional) {
         pattern = (String)regexp.orElse(".*");
      } else {
         pattern = (String)regexp.orElseThrow(() -> new ValidationException("No pattern specified"));
      }

      Pattern.Flag[] flags = (Pattern.Flag[])annotationMetadata.get("flags", Pattern.Flag[].class).orElse(ZERO_FLAGS);
      if (isOptional && pattern.equals(".*") && flags.length == 0) {
         return null;
      } else {
         int computedFlag = 0;

         for(Pattern.Flag flag : flags) {
            computedFlag |= flag.getValue();
         }

         AbstractPatternValidator.PatternKey key = new AbstractPatternValidator.PatternKey(pattern, computedFlag);
         java.util.regex.Pattern regex = (java.util.regex.Pattern)COMPUTED_PATTERNS.get(key);
         if (regex == null) {
            try {
               if (computedFlag != 0) {
                  regex = java.util.regex.Pattern.compile(pattern, computedFlag);
               } else {
                  regex = java.util.regex.Pattern.compile(pattern);
               }
            } catch (PatternSyntaxException var11) {
               throw new IllegalArgumentException("Invalid regular expression", var11);
            }

            COMPUTED_PATTERNS.put(key, regex);
         }

         return regex;
      }
   }

   private static final class PatternKey {
      final String pattern;
      final int flags;

      PatternKey(String pattern, int flags) {
         this.pattern = pattern;
         this.flags = flags;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractPatternValidator.PatternKey that = (AbstractPatternValidator.PatternKey)o;
            return this.flags == that.flags && this.pattern.equals(that.pattern);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.pattern, this.flags});
      }
   }
}
