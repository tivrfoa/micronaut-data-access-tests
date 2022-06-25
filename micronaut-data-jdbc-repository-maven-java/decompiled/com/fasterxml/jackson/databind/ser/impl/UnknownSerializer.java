package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToEmptyObjectSerializer;
import java.io.IOException;

public class UnknownSerializer extends ToEmptyObjectSerializer {
   public UnknownSerializer() {
      super(Object.class);
   }

   public UnknownSerializer(Class<?> cls) {
      super(cls);
   }

   @Override
   public void serialize(Object value, JsonGenerator gen, SerializerProvider ctxt) throws IOException {
      if (ctxt.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)) {
         this.failForEmpty(ctxt, value);
      }

      super.serialize(value, gen, ctxt);
   }

   @Override
   public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider ctxt, TypeSerializer typeSer) throws IOException {
      if (ctxt.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)) {
         this.failForEmpty(ctxt, value);
      }

      super.serializeWithType(value, gen, ctxt, typeSer);
   }

   protected void failForEmpty(SerializerProvider prov, Object value) throws JsonMappingException {
      prov.reportBadDefinition(
         this.handledType(),
         String.format(
            "No serializer found for class %s and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS)",
            value.getClass().getName()
         )
      );
   }
}
