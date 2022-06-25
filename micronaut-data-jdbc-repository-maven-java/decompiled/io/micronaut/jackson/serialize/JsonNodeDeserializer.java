package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import java.io.IOException;

@Singleton
final class JsonNodeDeserializer extends JsonDeserializer<JsonNode> {
   public JsonNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      return JsonNodeTreeCodec.getInstance().readTree(p);
   }
}
