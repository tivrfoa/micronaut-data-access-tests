package io.micronaut.inject.ast;

import java.util.Locale;

public enum ElementModifier {
   PUBLIC,
   PROTECTED,
   PRIVATE,
   ABSTRACT,
   DEFAULT,
   STATIC,
   FINAL,
   TRANSIENT,
   VOLATILE,
   SYNCHRONIZED,
   NATIVE,
   STRICTFP;

   public String toString() {
      return this.name().toLowerCase(Locale.ENGLISH);
   }
}
