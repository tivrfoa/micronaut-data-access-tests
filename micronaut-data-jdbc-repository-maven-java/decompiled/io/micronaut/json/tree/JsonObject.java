package io.micronaut.json.tree;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.Map;
import java.util.Map.Entry;

@Internal
public class JsonObject extends JsonContainer {
   private final Map<String, JsonNode> values;

   JsonObject(Map<String, JsonNode> values) {
      this.values = values;
   }

   @Override
   public int size() {
      return this.values.size();
   }

   @Override
   public boolean isObject() {
      return true;
   }

   @Override
   public JsonNode get(@NonNull String fieldName) {
      return (JsonNode)this.values.get(fieldName);
   }

   @Override
   public JsonNode get(int index) {
      return null;
   }

   @NonNull
   @Override
   public Iterable<JsonNode> values() {
      return this.values.values();
   }

   @NonNull
   @Override
   public Iterable<Entry<String, JsonNode>> entries() {
      return this.values.entrySet();
   }

   public boolean equals(Object o) {
      return o instanceof JsonObject && ((JsonObject)o).values.equals(this.values);
   }

   public int hashCode() {
      return this.values.hashCode();
   }
}
