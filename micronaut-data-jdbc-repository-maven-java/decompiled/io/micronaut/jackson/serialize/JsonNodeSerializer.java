package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import java.io.IOException;

@Singleton
final class JsonNodeSerializer extends JsonSerializer<JsonNode> {
   public void serialize(JsonNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      if (value == null) {
         gen.writeNull();
      } else {
         JsonNodeTreeCodec.getInstance().writeTree(gen, value);
      }

   }
}
