package io.micronaut.core.naming.conventions;

import io.micronaut.core.naming.NameUtils;
import java.util.Locale;

public enum StringConvention {
   CAMEL_CASE_CAPITALIZED,
   CAMEL_CASE,
   HYPHENATED,
   RAW,
   UNDER_SCORE_SEPARATED,
   UNDER_SCORE_SEPARATED_LOWER_CASE;

   public String format(String str) {
      return format(this, str);
   }

   public static String format(StringConvention convention, String str) {
      switch(convention) {
         case CAMEL_CASE:
            return NameUtils.camelCase(str);
         case HYPHENATED:
            return NameUtils.hyphenate(str);
         case UNDER_SCORE_SEPARATED_LOWER_CASE:
            return NameUtils.underscoreSeparate(str.toLowerCase(Locale.ENGLISH));
         case UNDER_SCORE_SEPARATED:
            return NameUtils.environmentName(str);
         case CAMEL_CASE_CAPITALIZED:
            return NameUtils.camelCase(str, false);
         case RAW:
         default:
            return str;
      }
   }
}
