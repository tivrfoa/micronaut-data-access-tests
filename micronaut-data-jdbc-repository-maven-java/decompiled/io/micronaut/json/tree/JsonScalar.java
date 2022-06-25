package io.micronaut.json.tree;

import io.micronaut.core.annotation.NonNull;
import java.util.Map.Entry;

abstract class JsonScalar extends JsonNode {
   @Override
   public int size() {
      return 0;
   }

   @NonNull
   @Override
   public Iterable<JsonNode> values() {
      throw new IllegalStateException("Not a container");
   }

   @NonNull
   @Override
   public Iterable<Entry<String, JsonNode>> entries() {
      throw new IllegalStateException("Not an object");
   }

   @Override
   public boolean isValueNode() {
      return true;
   }

   @Override
   public JsonNode get(@NonNull String fieldName) {
      return null;
   }

   @Override
   public JsonNode get(int index) {
      return null;
   }
}
