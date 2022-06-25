package io.micronaut.json.tree;

import io.micronaut.core.annotation.NonNull;

final class JsonNull extends JsonScalar {
   static final JsonNull INSTANCE = new JsonNull();

   private JsonNull() {
   }

   @NonNull
   @Override
   public String coerceStringValue() {
      return "null";
   }

   @Override
   public boolean isNull() {
      return true;
   }
}
