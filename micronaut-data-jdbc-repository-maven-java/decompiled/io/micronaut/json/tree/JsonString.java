package io.micronaut.json.tree;

import io.micronaut.core.annotation.NonNull;

final class JsonString extends JsonScalar {
   @NonNull
   private final String value;

   JsonString(@NonNull String value) {
      this.value = value;
   }

   public boolean equals(Object o) {
      return o instanceof JsonString && ((JsonString)o).value.equals(this.value);
   }

   public int hashCode() {
      return this.value.hashCode();
   }

   @Override
   public boolean isString() {
      return true;
   }

   @NonNull
   @Override
   public String getStringValue() {
      return this.value;
   }

   @NonNull
   @Override
   public String coerceStringValue() {
      return this.value;
   }
}
