package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.IOException;

@JacksonStdImpl
public class StringDeserializer extends StdScalarDeserializer<String> {
   private static final long serialVersionUID = 1L;
   public static final StringDeserializer instance = new StringDeserializer();

   public StringDeserializer() {
      super(String.class);
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Textual;
   }

   @Override
   public boolean isCachable() {
      return true;
   }

   @Override
   public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
      return "";
   }

   public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.VALUE_STRING)) {
         return p.getText();
      } else {
         JsonToken t = p.currentToken();
         if (t == JsonToken.START_ARRAY) {
            return this._deserializeFromArray(p, ctxt);
         } else if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = p.getEmbeddedObject();
            if (ob == null) {
               return null;
            } else {
               return ob instanceof byte[] ? ctxt.getBase64Variant().encode((byte[])ob, false) : ob.toString();
            }
         } else if (t == JsonToken.START_OBJECT) {
            return ctxt.extractScalarFromObject(p, this, this._valueClass);
         } else {
            if (t.isScalarValue()) {
               String text = p.getValueAsString();
               if (text != null) {
                  return text;
               }
            }

            return (String)ctxt.handleUnexpectedToken(this._valueClass, p);
         }
      }
   }

   public String deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return this.deserialize(p, ctxt);
   }
}
