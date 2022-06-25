package io.micronaut.core.naming.conventions;

import io.micronaut.core.naming.NameUtils;
import java.util.Locale;

public enum TypeConvention {
   CONTROLLER,
   SERVICE,
   REPOSITORY,
   JOB,
   FACTORY;

   private final String suffix = NameUtils.capitalize(this.name().toLowerCase(Locale.ENGLISH));

   public String asPropertyName(Class type) {
      return NameUtils.decapitalizeWithoutSuffix(type.getSimpleName(), this.suffix);
   }

   public String asHyphenatedName(Class type) {
      String shortName = NameUtils.trimSuffix(type.getSimpleName(), this.suffix);
      return NameUtils.hyphenate(shortName);
   }
}
