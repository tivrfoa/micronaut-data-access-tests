package io.micronaut.core.naming.conventions;

import java.util.Locale;

public enum PropertyConvention {
   ID;

   private final String lowerCase = this.name().toLowerCase(Locale.ENGLISH);

   public String lowerCaseName() {
      return this.lowerCase;
   }
}
