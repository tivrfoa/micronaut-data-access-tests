package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.micronaut.core.value.OptionalMultiValues;
import io.micronaut.core.value.OptionalValues;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.jackson.JacksonConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Singleton
public class OptionalValuesSerializer extends JsonSerializer<OptionalValues<?>> {
   private final boolean alwaysSerializeErrorsAsList;

   public OptionalValuesSerializer() {
      this.alwaysSerializeErrorsAsList = false;
   }

   @Inject
   public OptionalValuesSerializer(JacksonConfiguration jacksonConfiguration) {
      this.alwaysSerializeErrorsAsList = jacksonConfiguration.isAlwaysSerializeErrorsAsList();
   }

   public boolean isEmpty(SerializerProvider provider, OptionalValues<?> value) {
      return value.isEmpty();
   }

   public void serialize(OptionalValues<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();

      for(CharSequence key : value) {
         Optional<?> opt = value.get(key);
         if (opt.isPresent()) {
            String fieldName = key.toString();
            gen.writeFieldName(fieldName);
            Object v = opt.get();
            if (value instanceof OptionalMultiValues) {
               List<?> list = (List)v;
               if (list.size() == 1 && (list.get(0).getClass() != JsonError.class || !this.alwaysSerializeErrorsAsList)) {
                  gen.writeObject(list.get(0));
               } else {
                  gen.writeObject(list);
               }
            } else {
               gen.writeObject(v);
            }
         }
      }

      gen.writeEndObject();
   }
}
