package io.micronaut.data.model.naming;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.NameUtils;
import java.util.Locale;

public class NamingStrategies {
   public static class KebabCase implements NamingStrategy {
      @NonNull
      @Override
      public String mappedName(@NonNull String name) {
         return NameUtils.hyphenate(name);
      }
   }

   public static class LowerCase implements NamingStrategy {
      @NonNull
      @Override
      public String mappedName(@NonNull String name) {
         return name.toLowerCase(Locale.ENGLISH);
      }
   }

   public static class Raw implements NamingStrategy {
      @NonNull
      @Override
      public String mappedName(@NonNull String name) {
         return name;
      }
   }

   public static class UnderScoreSeparatedLowerCase implements NamingStrategy {
      @NonNull
      @Override
      public String mappedName(@NonNull String name) {
         return NameUtils.underscoreSeparate(name).toLowerCase(Locale.ENGLISH);
      }
   }

   public static class UnderScoreSeparatedUpperCase implements NamingStrategy {
      @NonNull
      @Override
      public String mappedName(@NonNull String name) {
         return NameUtils.environmentName(name);
      }
   }

   public static class UpperCase implements NamingStrategy {
      @NonNull
      @Override
      public String mappedName(@NonNull String name) {
         return name.toUpperCase(Locale.ENGLISH);
      }
   }
}
