package io.micronaut.http.uri;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UriMatchVariable {
   private static final List<Character> OPTIONAL_OPERATORS = Arrays.asList('/', '#', '?', '&');
   private final String name;
   private final char modifier;
   private final char operator;

   UriMatchVariable(String name, char modifier, char operator) {
      this.name = name;
      this.modifier = modifier;
      this.operator = operator;
   }

   public String getName() {
      return this.name;
   }

   public boolean isExploded() {
      return this.modifier == '*';
   }

   public boolean isQuery() {
      return this.operator == '?' || this.operator == '#' || this.operator == '&';
   }

   public boolean isOptional() {
      return OPTIONAL_OPERATORS.contains(this.operator);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof UriMatchVariable)) {
         return false;
      } else {
         UriMatchVariable that = (UriMatchVariable)o;
         return Objects.equals(this.name, that.name);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.name});
   }
}
