package io.micronaut.jackson.core.env;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import io.micronaut.context.env.AbstractPropertySourceLoader;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.json.tree.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonPropertySourceLoader extends AbstractPropertySourceLoader {
   public static final String FILE_EXTENSION = "json";

   @Override
   public Set<String> getExtensions() {
      return Collections.singleton("json");
   }

   @Override
   protected void processInput(String name, InputStream input, Map<String, Object> finalMap) throws IOException {
      Map<String, Object> map = this.readJsonAsMap(input);
      this.processMap(finalMap, map, "");
   }

   protected Map<String, Object> readJsonAsMap(InputStream input) throws IOException {
      return (Map<String, Object>)this.unwrap(this.readJsonAsObject(input));
   }

   private JsonNode readJsonAsObject(InputStream input) throws IOException {
      JsonParser parser = new JsonFactory().createParser(input);
      Throwable var3 = null;

      JsonNode var4;
      try {
         var4 = JsonNodeTreeCodec.getInstance().readTree(parser);
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (parser != null) {
            if (var3 != null) {
               try {
                  parser.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               parser.close();
            }
         }

      }

      return var4;
   }

   private Object unwrap(JsonNode value) {
      if (value.isNumber()) {
         return value.getNumberValue();
      } else if (value.isNull()) {
         return null;
      } else if (value.isBoolean()) {
         return value.getBooleanValue();
      } else if (value.isArray()) {
         List<Object> unwrapped = new ArrayList();
         value.values().forEach(v -> unwrapped.add(this.unwrap(v)));
         return unwrapped;
      } else if (value.isObject()) {
         Map<String, Object> unwrapped = new LinkedHashMap();
         value.entries().forEach(e -> unwrapped.put(e.getKey(), this.unwrap((JsonNode)e.getValue())));
         return unwrapped;
      } else {
         return value.getStringValue();
      }
   }
}
