package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualKeyDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

@JacksonStdImpl
public class MapEntryDeserializer extends ContainerDeserializerBase<Entry<Object, Object>> implements ContextualDeserializer {
   private static final long serialVersionUID = 1L;
   protected final KeyDeserializer _keyDeserializer;
   protected final JsonDeserializer<Object> _valueDeserializer;
   protected final TypeDeserializer _valueTypeDeserializer;

   public MapEntryDeserializer(JavaType type, KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser) {
      super(type);
      if (type.containedTypeCount() != 2) {
         throw new IllegalArgumentException("Missing generic type information for " + type);
      } else {
         this._keyDeserializer = keyDeser;
         this._valueDeserializer = valueDeser;
         this._valueTypeDeserializer = valueTypeDeser;
      }
   }

   protected MapEntryDeserializer(MapEntryDeserializer src) {
      super(src);
      this._keyDeserializer = src._keyDeserializer;
      this._valueDeserializer = src._valueDeserializer;
      this._valueTypeDeserializer = src._valueTypeDeserializer;
   }

   protected MapEntryDeserializer(MapEntryDeserializer src, KeyDeserializer keyDeser, JsonDeserializer<Object> valueDeser, TypeDeserializer valueTypeDeser) {
      super(src);
      this._keyDeserializer = keyDeser;
      this._valueDeserializer = valueDeser;
      this._valueTypeDeserializer = valueTypeDeser;
   }

   protected MapEntryDeserializer withResolved(KeyDeserializer keyDeser, TypeDeserializer valueTypeDeser, JsonDeserializer<?> valueDeser) {
      return this._keyDeserializer == keyDeser && this._valueDeserializer == valueDeser && this._valueTypeDeserializer == valueTypeDeser
         ? this
         : new MapEntryDeserializer(this, keyDeser, valueDeser, valueTypeDeser);
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Map;
   }

   @Override
   public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
      KeyDeserializer kd = this._keyDeserializer;
      if (kd == null) {
         kd = ctxt.findKeyDeserializer(this._containerType.containedType(0), property);
      } else if (kd instanceof ContextualKeyDeserializer) {
         kd = ((ContextualKeyDeserializer)kd).createContextual(ctxt, property);
      }

      JsonDeserializer<?> vd = this._valueDeserializer;
      vd = this.findConvertingContentDeserializer(ctxt, property, vd);
      JavaType contentType = this._containerType.containedType(1);
      if (vd == null) {
         vd = ctxt.findContextualValueDeserializer(contentType, property);
      } else {
         vd = ctxt.handleSecondaryContextualization(vd, property, contentType);
      }

      TypeDeserializer vtd = this._valueTypeDeserializer;
      if (vtd != null) {
         vtd = vtd.forProperty(property);
      }

      return this.withResolved(kd, vtd, vd);
   }

   @Override
   public JavaType getContentType() {
      return this._containerType.containedType(1);
   }

   @Override
   public JsonDeserializer<Object> getContentDeserializer() {
      return this._valueDeserializer;
   }

   public Entry<Object, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonToken t = p.currentToken();
      if (t == JsonToken.START_OBJECT) {
         t = p.nextToken();
      } else if (t != JsonToken.FIELD_NAME && t != JsonToken.END_OBJECT) {
         if (t == JsonToken.START_ARRAY) {
            return this._deserializeFromArray(p, ctxt);
         }

         return (Entry<Object, Object>)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
      }

      if (t != JsonToken.FIELD_NAME) {
         return t == JsonToken.END_OBJECT
            ? ctxt.reportInputMismatch(this, "Cannot deserialize a Map.Entry out of empty JSON Object")
            : (Entry)ctxt.handleUnexpectedToken(this.handledType(), p);
      } else {
         KeyDeserializer keyDes = this._keyDeserializer;
         JsonDeserializer<Object> valueDes = this._valueDeserializer;
         TypeDeserializer typeDeser = this._valueTypeDeserializer;
         String keyStr = p.currentName();
         Object key = keyDes.deserializeKey(keyStr, ctxt);
         Object value = null;
         t = p.nextToken();

         try {
            if (t == JsonToken.VALUE_NULL) {
               value = valueDes.getNullValue(ctxt);
            } else if (typeDeser == null) {
               value = valueDes.deserialize(p, ctxt);
            } else {
               value = valueDes.deserializeWithType(p, ctxt, typeDeser);
            }
         } catch (Exception var11) {
            this.wrapAndThrow(ctxt, var11, Entry.class, keyStr);
         }

         t = p.nextToken();
         if (t != JsonToken.END_OBJECT) {
            if (t == JsonToken.FIELD_NAME) {
               ctxt.reportInputMismatch(this, "Problem binding JSON into Map.Entry: more than one entry in JSON (second field: '%s')", p.currentName());
            } else {
               ctxt.reportInputMismatch(this, "Problem binding JSON into Map.Entry: unexpected content after JSON Object entry: " + t);
            }

            return null;
         } else {
            return new SimpleEntry(key, value);
         }
      }
   }

   public Entry<Object, Object> deserialize(JsonParser p, DeserializationContext ctxt, Entry<Object, Object> result) throws IOException {
      throw new IllegalStateException("Cannot update Map.Entry values");
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return typeDeserializer.deserializeTypedFromObject(p, ctxt);
   }
}
