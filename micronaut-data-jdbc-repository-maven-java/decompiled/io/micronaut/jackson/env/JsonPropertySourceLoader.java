package io.micronaut.jackson.env;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.micronaut.context.env.AbstractPropertySourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
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
      ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
      TypeFactory factory = TypeFactory.defaultInstance();
      MapType mapType = factory.constructMapType(LinkedHashMap.class, String.class, Object.class);
      return objectMapper.readValue(input, mapType);
   }
}
