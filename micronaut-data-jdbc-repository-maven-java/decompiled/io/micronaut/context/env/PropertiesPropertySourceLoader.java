package io.micronaut.context.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

public class PropertiesPropertySourceLoader extends AbstractPropertySourceLoader {
   public static final String PROPERTIES_EXTENSION = "properties";

   @Override
   public Set<String> getExtensions() {
      return Collections.singleton("properties");
   }

   @Override
   protected void processInput(String name, InputStream input, Map<String, Object> finalMap) throws IOException {
      Properties props = new Properties();
      props.load(input);

      for(Entry<Object, Object> entry : props.entrySet()) {
         finalMap.put(entry.getKey().toString(), entry.getValue());
      }

   }
}
