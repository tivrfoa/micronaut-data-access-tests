package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

@Singleton
public class ConvertibleMultiValuesSerializer extends JsonSerializer<ConvertibleMultiValues<?>> {
   public boolean isEmpty(SerializerProvider provider, ConvertibleMultiValues<?> value) {
      return value.isEmpty();
   }

   public void serialize(ConvertibleMultiValues<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();

      for(Entry<String, ? extends List<?>> entry : value) {
         String fieldName = (String)entry.getKey();
         List<?> v = (List)entry.getValue();
         int len = v.size();
         if (len > 0) {
            gen.writeFieldName(fieldName);
            if (len == 1) {
               gen.writeObject(v.get(0));
            } else {
               gen.writeStartArray();

               for(Object o : v) {
                  gen.writeObject(o);
               }

               gen.writeEndArray();
            }
         }
      }

      gen.writeEndObject();
   }
}
