package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class JSR310StringParsableDeserializer extends JSR310DeserializerBase<Object> implements ContextualDeserializer {
   private static final long serialVersionUID = 1L;
   protected static final int TYPE_PERIOD = 1;
   protected static final int TYPE_ZONE_ID = 2;
   protected static final int TYPE_ZONE_OFFSET = 3;
   public static final JsonDeserializer<Period> PERIOD = createDeserializer(Period.class, 1);
   public static final JsonDeserializer<ZoneId> ZONE_ID = createDeserializer(ZoneId.class, 2);
   public static final JsonDeserializer<ZoneOffset> ZONE_OFFSET = createDeserializer(ZoneOffset.class, 3);
   protected final int _typeSelector;

   protected JSR310StringParsableDeserializer(Class<?> supportedType, int typeSelector) {
      super(supportedType);
      this._typeSelector = typeSelector;
   }

   protected JSR310StringParsableDeserializer(JSR310StringParsableDeserializer base, Boolean leniency) {
      super(base, leniency);
      this._typeSelector = base._typeSelector;
   }

   protected static <T> JsonDeserializer<T> createDeserializer(Class<T> type, int typeId) {
      return new JSR310StringParsableDeserializer(type, typeId);
   }

   protected JSR310StringParsableDeserializer withLeniency(Boolean leniency) {
      return this._isLenient == !Boolean.FALSE.equals(leniency) ? this : new JSR310StringParsableDeserializer(this, leniency);
   }

   @Override
   public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
      JsonFormat.Value format = this.findFormatOverrides(ctxt, property, this.handledType());
      JSR310StringParsableDeserializer deser = this;
      if (format != null && format.hasLenient()) {
         Boolean leniency = format.getLenient();
         if (leniency != null) {
            deser = this.withLeniency(leniency);
         }
      }

      return deser;
   }

   @Override
   public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.VALUE_STRING)) {
         return this._fromString(p, ctxt, p.getText());
      } else if (p.isExpectedStartObjectToken()) {
         return this._fromString(p, ctxt, ctxt.extractScalarFromObject(p, this, this.handledType()));
      } else if (p.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
         return p.getEmbeddedObject();
      } else if (p.isExpectedStartArrayToken()) {
         return this._deserializeFromArray(p, ctxt);
      } else {
         throw ctxt.wrongTokenException(p, this.handledType(), JsonToken.VALUE_STRING, null);
      }
   }

   @Override
   public Object deserializeWithType(JsonParser parser, DeserializationContext context, TypeDeserializer deserializer) throws IOException {
      JsonToken t = parser.getCurrentToken();
      return t != null && t.isScalarValue() ? this.deserialize(parser, context) : deserializer.deserializeTypedFromAny(parser, context);
   }

   protected Object _fromString(JsonParser p, DeserializationContext ctxt, String string) throws IOException {
      string = string.trim();
      if (string.length() == 0) {
         CoercionAction act = ctxt.findCoercionAction(this.logicalType(), this._valueClass, CoercionInputShape.EmptyString);
         if (act == CoercionAction.Fail) {
            ctxt.reportInputMismatch(
               this, "Cannot coerce empty String (\"\") to %s (but could if enabling coercion using `CoercionConfig`)", this._coercedTypeDesc()
            );
         }

         if (!this.isLenient()) {
            return this._failForNotLenient(p, ctxt, JsonToken.VALUE_STRING);
         } else {
            return act == CoercionAction.AsEmpty ? this.getEmptyValue(ctxt) : null;
         }
      } else {
         try {
            switch(this._typeSelector) {
               case 1:
                  return Period.parse(string);
               case 2:
                  return ZoneId.of(string);
               case 3:
                  return ZoneOffset.of(string);
            }
         } catch (DateTimeException var5) {
            return this._handleDateTimeException(ctxt, var5, string);
         }

         VersionUtil.throwInternal();
         return null;
      }
   }
}
