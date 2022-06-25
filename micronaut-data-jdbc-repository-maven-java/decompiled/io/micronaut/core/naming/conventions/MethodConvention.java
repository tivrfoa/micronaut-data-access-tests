package io.micronaut.core.naming.conventions;

import java.util.Locale;
import java.util.Optional;

public enum MethodConvention {
   INDEX("", "GET"),
   SHOW("{/id}", "GET"),
   SAVE("", "POST"),
   UPDATE("{/id}", "PUT"),
   DELETE("{/id}"),
   OPTIONS(""),
   HEAD(""),
   TRACE("");

   public static final String ID_PATH = "{/id}";
   private final String lowerCase;
   private final String httpMethod;
   private final String uri;

   private MethodConvention(String uri, String httpMethod) {
      this.uri = uri;
      this.httpMethod = httpMethod;
      this.lowerCase = this.name().toLowerCase(Locale.ENGLISH);
   }

   private MethodConvention(String uri) {
      this.uri = uri;
      this.httpMethod = this.name();
      this.lowerCase = this.name().toLowerCase(Locale.ENGLISH);
   }

   public String uri() {
      return this.uri;
   }

   public String httpMethod() {
      return this.httpMethod;
   }

   public String methodName() {
      return this.lowerCase;
   }

   public static Optional<MethodConvention> forMethod(String name) {
      try {
         return Optional.of(valueOf(name.toUpperCase(Locale.ENGLISH)));
      } catch (IllegalArgumentException var2) {
         return Optional.empty();
      }
   }
}
