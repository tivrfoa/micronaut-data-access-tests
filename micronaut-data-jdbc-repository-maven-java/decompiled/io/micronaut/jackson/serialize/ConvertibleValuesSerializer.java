package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.micronaut.core.convert.value.ConvertibleValues;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Map.Entry;

@Singleton
public class ConvertibleValuesSerializer extends JsonSerializer<ConvertibleValues<?>> {
   public boolean isEmpty(SerializerProvider provider, ConvertibleValues<?> value) {
      return value.isEmpty();
   }

   public void serialize(ConvertibleValues<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();

      for(Entry<String, ?> entry : value) {
         String fieldName = (String)entry.getKey();
         Object v = entry.getValue();
         if (v != null) {
            gen.writeFieldName(fieldName);
            gen.writeObject(v);
         }
      }

      gen.writeEndObject();
   }
}
