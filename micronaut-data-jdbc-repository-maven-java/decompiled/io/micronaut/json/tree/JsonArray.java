package io.micronaut.json.tree;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.List;
import java.util.Map.Entry;

@Internal
public class JsonArray extends JsonContainer {
   private final List<JsonNode> values;

   JsonArray(List<JsonNode> values) {
      this.values = values;
   }

   @Override
   public int size() {
      return this.values.size();
   }

   @Override
   public boolean isArray() {
      return true;
   }

   @Override
   public JsonNode get(@NonNull String fieldName) {
      return null;
   }

   @Override
   public JsonNode get(int index) {
      return index >= 0 && index < this.size() ? (JsonNode)this.values.get(index) : null;
   }

   @NonNull
   @Override
   public Iterable<JsonNode> values() {
      return this.values;
   }

   @NonNull
   @Override
   public Iterable<Entry<String, JsonNode>> entries() {
      throw new IllegalStateException("Not an object");
   }

   public boolean equals(Object o) {
      return o instanceof JsonArray && ((JsonArray)o).values.equals(this.values);
   }

   public int hashCode() {
      return this.values.hashCode();
   }
}
