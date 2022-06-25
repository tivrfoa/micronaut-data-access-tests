package io.micronaut.json.tree;

import io.micronaut.core.annotation.NonNull;

final class JsonNumber extends JsonScalar {
   @NonNull
   private final Number value;

   JsonNumber(@NonNull Number value) {
      this.value = value;
   }

   @Override
   public boolean isNumber() {
      return true;
   }

   @NonNull
   @Override
   public Number getNumberValue() {
      return this.value;
   }

   @NonNull
   @Override
   public String coerceStringValue() {
      return this.value.toString();
   }

   public boolean equals(Object o) {
      return o instanceof JsonNumber && ((JsonNumber)o).value.equals(this.value);
   }

   public int hashCode() {
      return this.value.hashCode();
   }
}
