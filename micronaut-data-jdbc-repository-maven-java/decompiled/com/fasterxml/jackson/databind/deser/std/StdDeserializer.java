package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadCapability;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsFailProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public abstract class StdDeserializer<T> extends JsonDeserializer<T> implements Serializable, ValueInstantiator.Gettable {
   private static final long serialVersionUID = 1L;
   protected static final int F_MASK_INT_COERCIONS = DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.getMask()
      | DeserializationFeature.USE_LONG_FOR_INTS.getMask();
   @Deprecated
   protected static final int F_MASK_ACCEPT_ARRAYS = DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS.getMask()
      | DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT.getMask();
   protected final Class<?> _valueClass;
   protected final JavaType _valueType;

   protected StdDeserializer(Class<?> vc) {
      this._valueClass = vc;
      this._valueType = null;
   }

   protected StdDeserializer(JavaType valueType) {
      this._valueClass = valueType == null ? Object.class : valueType.getRawClass();
      this._valueType = valueType;
   }

   protected StdDeserializer(StdDeserializer<?> src) {
      this._valueClass = src._valueClass;
      this._valueType = src._valueType;
   }

   @Override
   public Class<?> handledType() {
      return this._valueClass;
   }

   @Deprecated
   public final Class<?> getValueClass() {
      return this._valueClass;
   }

   public JavaType getValueType() {
      return this._valueType;
   }

   public JavaType getValueType(DeserializationContext ctxt) {
      return this._valueType != null ? this._valueType : ctxt.constructType(this._valueClass);
   }

   @Override
   public ValueInstantiator getValueInstantiator() {
      return null;
   }

   protected boolean isDefaultDeserializer(JsonDeserializer<?> deserializer) {
      return ClassUtil.isJacksonStdImpl(deserializer);
   }

   protected boolean isDefaultKeyDeserializer(KeyDeserializer keyDeser) {
      return ClassUtil.isJacksonStdImpl(keyDeser);
   }

   @Override
   public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
      return typeDeserializer.deserializeTypedFromAny(p, ctxt);
   }

   protected T _deserializeFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
      CoercionAction act = this._findCoercionFromEmptyArray(ctxt);
      boolean unwrap = ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
      if (unwrap || act != CoercionAction.Fail) {
         JsonToken t = p.nextToken();
         if (t == JsonToken.END_ARRAY) {
            switch(act) {
               case AsEmpty:
                  return (T)this.getEmptyValue(ctxt);
               case AsNull:
               case TryConvert:
                  return this.getNullValue(ctxt);
            }
         } else if (unwrap) {
            T parsed = this._deserializeWrappedValue(p, ctxt);
            if (p.nextToken() != JsonToken.END_ARRAY) {
               this.handleMissingEndArrayForSingle(p, ctxt);
            }

            return parsed;
         }
      }

      return (T)ctxt.handleUnexpectedToken(this.getValueType(ctxt), JsonToken.START_ARRAY, p, null);
   }

   @Deprecated
   protected T _deserializeFromEmpty(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.START_ARRAY) && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
         JsonToken t = p.nextToken();
         return (T)(t == JsonToken.END_ARRAY ? null : ctxt.handleUnexpectedToken(this.getValueType(ctxt), p));
      } else {
         return (T)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
      }
   }

   protected T _deserializeFromString(JsonParser p, DeserializationContext ctxt) throws IOException {
      ValueInstantiator inst = this.getValueInstantiator();
      Class<?> rawTargetType = this.handledType();
      String value = p.getValueAsString();
      if (inst != null && inst.canCreateFromString()) {
         return (T)inst.createFromString(ctxt, value);
      } else if (value.isEmpty()) {
         CoercionAction act = ctxt.findCoercionAction(this.logicalType(), rawTargetType, CoercionInputShape.EmptyString);
         return (T)this._deserializeFromEmptyString(p, ctxt, act, rawTargetType, "empty String (\"\")");
      } else if (_isBlank(value)) {
         CoercionAction act = ctxt.findCoercionFromBlankString(this.logicalType(), rawTargetType, CoercionAction.Fail);
         return (T)this._deserializeFromEmptyString(p, ctxt, act, rawTargetType, "blank String (all whitespace)");
      } else {
         if (inst != null) {
            value = value.trim();
            if (inst.canCreateFromInt() && ctxt.findCoercionAction(LogicalType.Integer, Integer.class, CoercionInputShape.String) == CoercionAction.TryConvert) {
               return (T)inst.createFromInt(ctxt, this._parseIntPrimitive(ctxt, value));
            }

            if (inst.canCreateFromLong() && ctxt.findCoercionAction(LogicalType.Integer, Long.class, CoercionInputShape.String) == CoercionAction.TryConvert) {
               return (T)inst.createFromLong(ctxt, this._parseLongPrimitive(ctxt, value));
            }

            if (inst.canCreateFromBoolean()
               && ctxt.findCoercionAction(LogicalType.Boolean, Boolean.class, CoercionInputShape.String) == CoercionAction.TryConvert) {
               String str = value.trim();
               if ("true".equals(str)) {
                  return (T)inst.createFromBoolean(ctxt, true);
               }

               if ("false".equals(str)) {
                  return (T)inst.createFromBoolean(ctxt, false);
               }
            }
         }

         return (T)ctxt.handleMissingInstantiator(
            rawTargetType, inst, ctxt.getParser(), "no String-argument constructor/factory method to deserialize from String value ('%s')", value
         );
      }
   }

   protected Object _deserializeFromEmptyString(JsonParser p, DeserializationContext ctxt, CoercionAction act, Class<?> rawTargetType, String desc) throws IOException {
      switch(act) {
         case AsEmpty:
            return this.getEmptyValue(ctxt);
         case Fail:
            this._checkCoercionFail(ctxt, act, rawTargetType, "", "empty String (\"\")");
         case AsNull:
         case TryConvert:
         default:
            return null;
      }
   }

   protected T _deserializeWrappedValue(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.START_ARRAY)) {
         String msg = String.format(
            "Cannot deserialize instance of %s out of %s token: nested Arrays not allowed with %s",
            ClassUtil.nameOf(this._valueClass),
            JsonToken.START_ARRAY,
            "DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS"
         );
         return (T)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p.currentToken(), p, msg);
      } else {
         return this.deserialize(p, ctxt);
      }
   }

   @Deprecated
   protected final boolean _parseBooleanPrimitive(DeserializationContext ctxt, JsonParser p, Class<?> targetType) throws IOException {
      return this._parseBooleanPrimitive(p, ctxt);
   }

   protected final boolean _parseBooleanPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, Boolean.TYPE);
            break;
         case 3:
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               p.nextToken();
               boolean parsed = this._parseBooleanPrimitive(p, ctxt);
               this._verifyEndArrayForSingle(p, ctxt);
               return parsed;
            }
         case 2:
         case 4:
         case 5:
         case 8:
         default:
            return ctxt.handleUnexpectedToken(Boolean.TYPE, p);
         case 6:
            text = p.getText();
            break;
         case 7:
            return Boolean.TRUE.equals(this._coerceBooleanFromInt(p, ctxt, Boolean.TYPE));
         case 9:
            return true;
         case 10:
            return false;
         case 11:
            this._verifyNullForPrimitive(ctxt);
            return false;
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Boolean, Boolean.TYPE);
      if (act == CoercionAction.AsNull) {
         this._verifyNullForPrimitive(ctxt);
         return false;
      } else if (act == CoercionAction.AsEmpty) {
         return false;
      } else {
         text = text.trim();
         int len = text.length();
         if (len == 4) {
            if (this._isTrue(text)) {
               return true;
            }
         } else if (len == 5 && this._isFalse(text)) {
            return false;
         }

         if (this._hasTextualNull(text)) {
            this._verifyNullForPrimitiveCoercion(ctxt, text);
            return false;
         } else {
            Boolean b = (Boolean)ctxt.handleWeirdStringValue(Boolean.TYPE, text, "only \"true\"/\"True\"/\"TRUE\" or \"false\"/\"False\"/\"FALSE\" recognized");
            return Boolean.TRUE.equals(b);
         }
      }
   }

   protected boolean _isTrue(String text) {
      char c = text.charAt(0);
      if (c == 't') {
         return "true".equals(text);
      } else if (c != 'T') {
         return false;
      } else {
         return "TRUE".equals(text) || "True".equals(text);
      }
   }

   protected boolean _isFalse(String text) {
      char c = text.charAt(0);
      if (c == 'f') {
         return "false".equals(text);
      } else if (c != 'F') {
         return false;
      } else {
         return "FALSE".equals(text) || "False".equals(text);
      }
   }

   protected final Boolean _parseBoolean(JsonParser p, DeserializationContext ctxt, Class<?> targetType) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, targetType);
            break;
         case 2:
         case 4:
         case 5:
         case 8:
         default:
            return (Boolean)ctxt.handleUnexpectedToken(targetType, p);
         case 3:
            return (Boolean)this._deserializeFromArray(p, ctxt);
         case 6:
            text = p.getText();
            break;
         case 7:
            return this._coerceBooleanFromInt(p, ctxt, targetType);
         case 9:
            return true;
         case 10:
            return false;
         case 11:
            return null;
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Boolean, targetType);
      if (act == CoercionAction.AsNull) {
         return null;
      } else if (act == CoercionAction.AsEmpty) {
         return false;
      } else {
         text = text.trim();
         int len = text.length();
         if (len == 4) {
            if (this._isTrue(text)) {
               return true;
            }
         } else if (len == 5 && this._isFalse(text)) {
            return false;
         }

         return this._checkTextualNull(ctxt, text) ? null : (Boolean)ctxt.handleWeirdStringValue(targetType, text, "only \"true\" or \"false\" recognized");
      }
   }

   protected final byte _parseBytePrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, Byte.TYPE);
            break;
         case 3:
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               p.nextToken();
               byte parsed = this._parseBytePrimitive(p, ctxt);
               this._verifyEndArrayForSingle(p, ctxt);
               return parsed;
            }
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return ctxt.handleUnexpectedToken(ctxt.constructType(Byte.TYPE), p);
         case 6:
            text = p.getText();
            break;
         case 7:
            return p.getByteValue();
         case 8:
            CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, Byte.TYPE);
            if (act == CoercionAction.AsNull) {
               return 0;
            }

            if (act == CoercionAction.AsEmpty) {
               return 0;
            }

            return p.getByteValue();
         case 11:
            this._verifyNullForPrimitive(ctxt);
            return 0;
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Integer, Byte.TYPE);
      if (act == CoercionAction.AsNull) {
         this._verifyNullForPrimitive(ctxt);
         return 0;
      } else if (act == CoercionAction.AsEmpty) {
         return 0;
      } else {
         text = text.trim();
         if (this._hasTextualNull(text)) {
            this._verifyNullForPrimitiveCoercion(ctxt, text);
            return 0;
         } else {
            int value;
            try {
               value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException var7) {
               return ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid `byte` value");
            }

            return this._byteOverflow(value)
               ? ctxt.handleWeirdStringValue(this._valueClass, text, "overflow, value cannot be represented as 8-bit value")
               : (byte)value;
         }
      }
   }

   protected final short _parseShortPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, Short.TYPE);
            break;
         case 3:
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               p.nextToken();
               short parsed = this._parseShortPrimitive(p, ctxt);
               this._verifyEndArrayForSingle(p, ctxt);
               return parsed;
            }
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return ctxt.handleUnexpectedToken(ctxt.constructType(Short.TYPE), p);
         case 6:
            text = p.getText();
            break;
         case 7:
            return p.getShortValue();
         case 8:
            CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, Short.TYPE);
            if (act == CoercionAction.AsNull) {
               return 0;
            }

            if (act == CoercionAction.AsEmpty) {
               return 0;
            }

            return p.getShortValue();
         case 11:
            this._verifyNullForPrimitive(ctxt);
            return 0;
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Integer, Short.TYPE);
      if (act == CoercionAction.AsNull) {
         this._verifyNullForPrimitive(ctxt);
         return 0;
      } else if (act == CoercionAction.AsEmpty) {
         return 0;
      } else {
         text = text.trim();
         if (this._hasTextualNull(text)) {
            this._verifyNullForPrimitiveCoercion(ctxt, text);
            return 0;
         } else {
            int value;
            try {
               value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException var7) {
               return ctxt.handleWeirdStringValue(Short.TYPE, text, "not a valid `short` value");
            }

            return this._shortOverflow(value)
               ? ctxt.handleWeirdStringValue(Short.TYPE, text, "overflow, value cannot be represented as 16-bit value")
               : (short)value;
         }
      }
   }

   protected final int _parseIntPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, Integer.TYPE);
            break;
         case 3:
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               p.nextToken();
               int parsed = this._parseIntPrimitive(p, ctxt);
               this._verifyEndArrayForSingle(p, ctxt);
               return parsed;
            }
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return ((Number)ctxt.handleUnexpectedToken(Integer.TYPE, p)).intValue();
         case 6:
            text = p.getText();
            break;
         case 7:
            return p.getIntValue();
         case 8:
            CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, Integer.TYPE);
            if (act == CoercionAction.AsNull) {
               return 0;
            }

            if (act == CoercionAction.AsEmpty) {
               return 0;
            }

            return p.getValueAsInt();
         case 11:
            this._verifyNullForPrimitive(ctxt);
            return 0;
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Integer, Integer.TYPE);
      if (act == CoercionAction.AsNull) {
         this._verifyNullForPrimitive(ctxt);
         return 0;
      } else if (act == CoercionAction.AsEmpty) {
         return 0;
      } else {
         text = text.trim();
         if (this._hasTextualNull(text)) {
            this._verifyNullForPrimitiveCoercion(ctxt, text);
            return 0;
         } else {
            return this._parseIntPrimitive(ctxt, text);
         }
      }
   }

   protected final int _parseIntPrimitive(DeserializationContext ctxt, String text) throws IOException {
      try {
         if (text.length() > 9) {
            long l = Long.parseLong(text);
            if (this._intOverflow(l)) {
               Number v = (Number)ctxt.handleWeirdStringValue(
                  Integer.TYPE, text, "Overflow: numeric value (%s) out of range of int (%d -%d)", text, Integer.MIN_VALUE, Integer.MAX_VALUE
               );
               return this._nonNullNumber(v).intValue();
            } else {
               return (int)l;
            }
         } else {
            return NumberInput.parseInt(text);
         }
      } catch (IllegalArgumentException var6) {
         Number v = (Number)ctxt.handleWeirdStringValue(Integer.TYPE, text, "not a valid `int` value");
         return this._nonNullNumber(v).intValue();
      }
   }

   protected final Integer _parseInteger(JsonParser p, DeserializationContext ctxt, Class<?> targetType) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, targetType);
            break;
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return (Integer)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
         case 3:
            return (Integer)this._deserializeFromArray(p, ctxt);
         case 6:
            text = p.getText();
            break;
         case 7:
            return p.getIntValue();
         case 8:
            CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, targetType);
            if (act == CoercionAction.AsNull) {
               return (Integer)this.getNullValue(ctxt);
            }

            if (act == CoercionAction.AsEmpty) {
               return (Integer)this.getEmptyValue(ctxt);
            }

            return p.getValueAsInt();
         case 11:
            return (Integer)this.getNullValue(ctxt);
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text);
      if (act == CoercionAction.AsNull) {
         return (Integer)this.getNullValue(ctxt);
      } else if (act == CoercionAction.AsEmpty) {
         return (Integer)this.getEmptyValue(ctxt);
      } else {
         text = text.trim();
         return this._checkTextualNull(ctxt, text) ? (Integer)this.getNullValue(ctxt) : this._parseIntPrimitive(ctxt, text);
      }
   }

   protected final long _parseLongPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, Long.TYPE);
            break;
         case 3:
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               p.nextToken();
               long parsed = this._parseLongPrimitive(p, ctxt);
               this._verifyEndArrayForSingle(p, ctxt);
               return parsed;
            }
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return ((Number)ctxt.handleUnexpectedToken(Long.TYPE, p)).longValue();
         case 6:
            text = p.getText();
            break;
         case 7:
            return p.getLongValue();
         case 8:
            CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, Long.TYPE);
            if (act == CoercionAction.AsNull) {
               return 0L;
            }

            if (act == CoercionAction.AsEmpty) {
               return 0L;
            }

            return p.getValueAsLong();
         case 11:
            this._verifyNullForPrimitive(ctxt);
            return 0L;
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Integer, Long.TYPE);
      if (act == CoercionAction.AsNull) {
         this._verifyNullForPrimitive(ctxt);
         return 0L;
      } else if (act == CoercionAction.AsEmpty) {
         return 0L;
      } else {
         text = text.trim();
         if (this._hasTextualNull(text)) {
            this._verifyNullForPrimitiveCoercion(ctxt, text);
            return 0L;
         } else {
            return this._parseLongPrimitive(ctxt, text);
         }
      }
   }

   protected final long _parseLongPrimitive(DeserializationContext ctxt, String text) throws IOException {
      try {
         return NumberInput.parseLong(text);
      } catch (IllegalArgumentException var4) {
         Number v = (Number)ctxt.handleWeirdStringValue(Long.TYPE, text, "not a valid `long` value");
         return this._nonNullNumber(v).longValue();
      }
   }

   protected final Long _parseLong(JsonParser p, DeserializationContext ctxt, Class<?> targetType) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, targetType);
            break;
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return (Long)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
         case 3:
            return (Long)this._deserializeFromArray(p, ctxt);
         case 6:
            text = p.getText();
            break;
         case 7:
            return p.getLongValue();
         case 8:
            CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, targetType);
            if (act == CoercionAction.AsNull) {
               return (Long)this.getNullValue(ctxt);
            }

            if (act == CoercionAction.AsEmpty) {
               return (Long)this.getEmptyValue(ctxt);
            }

            return p.getValueAsLong();
         case 11:
            return (Long)this.getNullValue(ctxt);
      }

      CoercionAction act = this._checkFromStringCoercion(ctxt, text);
      if (act == CoercionAction.AsNull) {
         return (Long)this.getNullValue(ctxt);
      } else if (act == CoercionAction.AsEmpty) {
         return (Long)this.getEmptyValue(ctxt);
      } else {
         text = text.trim();
         return this._checkTextualNull(ctxt, text) ? (Long)this.getNullValue(ctxt) : this._parseLongPrimitive(ctxt, text);
      }
   }

   protected final float _parseFloatPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, Float.TYPE);
            break;
         case 3:
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               p.nextToken();
               float parsed = this._parseFloatPrimitive(p, ctxt);
               this._verifyEndArrayForSingle(p, ctxt);
               return parsed;
            }
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return ((Number)ctxt.handleUnexpectedToken(Float.TYPE, p)).floatValue();
         case 6:
            text = p.getText();
            break;
         case 7:
         case 8:
            return p.getFloatValue();
         case 11:
            this._verifyNullForPrimitive(ctxt);
            return 0.0F;
      }

      Float nan = this._checkFloatSpecialValue(text);
      if (nan != null) {
         return nan;
      } else {
         CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Integer, Float.TYPE);
         if (act == CoercionAction.AsNull) {
            this._verifyNullForPrimitive(ctxt);
            return 0.0F;
         } else if (act == CoercionAction.AsEmpty) {
            return 0.0F;
         } else {
            text = text.trim();
            if (this._hasTextualNull(text)) {
               this._verifyNullForPrimitiveCoercion(ctxt, text);
               return 0.0F;
            } else {
               return this._parseFloatPrimitive(ctxt, text);
            }
         }
      }
   }

   protected final float _parseFloatPrimitive(DeserializationContext ctxt, String text) throws IOException {
      try {
         return Float.parseFloat(text);
      } catch (IllegalArgumentException var4) {
         Number v = (Number)ctxt.handleWeirdStringValue(Float.TYPE, text, "not a valid `float` value");
         return this._nonNullNumber(v).floatValue();
      }
   }

   protected Float _checkFloatSpecialValue(String text) {
      if (!text.isEmpty()) {
         switch(text.charAt(0)) {
            case '-':
               if (this._isNegInf(text)) {
                  return Float.NEGATIVE_INFINITY;
               }
               break;
            case 'I':
               if (this._isPosInf(text)) {
                  return Float.POSITIVE_INFINITY;
               }
               break;
            case 'N':
               if (this._isNaN(text)) {
                  return Float.NaN;
               }
         }
      }

      return null;
   }

   protected final double _parseDoublePrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, Double.TYPE);
            break;
         case 3:
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               p.nextToken();
               double parsed = this._parseDoublePrimitive(p, ctxt);
               this._verifyEndArrayForSingle(p, ctxt);
               return parsed;
            }
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         default:
            return ((Number)ctxt.handleUnexpectedToken(Double.TYPE, p)).doubleValue();
         case 6:
            text = p.getText();
            break;
         case 7:
         case 8:
            return p.getDoubleValue();
         case 11:
            this._verifyNullForPrimitive(ctxt);
            return 0.0;
      }

      Double nan = this._checkDoubleSpecialValue(text);
      if (nan != null) {
         return nan;
      } else {
         CoercionAction act = this._checkFromStringCoercion(ctxt, text, LogicalType.Integer, Double.TYPE);
         if (act == CoercionAction.AsNull) {
            this._verifyNullForPrimitive(ctxt);
            return 0.0;
         } else if (act == CoercionAction.AsEmpty) {
            return 0.0;
         } else {
            text = text.trim();
            if (this._hasTextualNull(text)) {
               this._verifyNullForPrimitiveCoercion(ctxt, text);
               return 0.0;
            } else {
               return this._parseDoublePrimitive(ctxt, text);
            }
         }
      }
   }

   protected final double _parseDoublePrimitive(DeserializationContext ctxt, String text) throws IOException {
      try {
         return _parseDouble(text);
      } catch (IllegalArgumentException var4) {
         Number v = (Number)ctxt.handleWeirdStringValue(Double.TYPE, text, "not a valid `double` value (as String to convert)");
         return this._nonNullNumber(v).doubleValue();
      }
   }

   protected static final double _parseDouble(String numStr) throws NumberFormatException {
      return "2.2250738585072012e-308".equals(numStr) ? Double.MIN_NORMAL : Double.parseDouble(numStr);
   }

   protected Double _checkDoubleSpecialValue(String text) {
      if (!text.isEmpty()) {
         switch(text.charAt(0)) {
            case '-':
               if (this._isNegInf(text)) {
                  return Double.NEGATIVE_INFINITY;
               }
               break;
            case 'I':
               if (this._isPosInf(text)) {
                  return Double.POSITIVE_INFINITY;
               }
               break;
            case 'N':
               if (this._isNaN(text)) {
                  return Double.NaN;
               }
         }
      }

      return null;
   }

   protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
      String text;
      switch(p.currentTokenId()) {
         case 1:
            text = ctxt.extractScalarFromObject(p, this, this._valueClass);
            break;
         case 2:
         case 4:
         case 5:
         case 8:
         case 9:
         case 10:
         default:
            return (Date)ctxt.handleUnexpectedToken(this._valueClass, p);
         case 3:
            return this._parseDateFromArray(p, ctxt);
         case 6:
            text = p.getText();
            break;
         case 7:
            long ts;
            try {
               ts = p.getLongValue();
            } catch (StreamReadException var8) {
               Number v = (Number)ctxt.handleWeirdNumberValue(this._valueClass, p.getNumberValue(), "not a valid 64-bit `long` for creating `java.util.Date`");
               ts = v.longValue();
            }

            return new Date(ts);
         case 11:
            return (Date)this.getNullValue(ctxt);
      }

      return this._parseDate(text.trim(), ctxt);
   }

   protected Date _parseDateFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
      CoercionAction act = this._findCoercionFromEmptyArray(ctxt);
      boolean unwrap = ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
      if (unwrap || act != CoercionAction.Fail) {
         JsonToken t = p.nextToken();
         if (t == JsonToken.END_ARRAY) {
            switch(act) {
               case AsEmpty:
                  return (Date)this.getEmptyValue(ctxt);
               case AsNull:
               case TryConvert:
                  return (Date)this.getNullValue(ctxt);
            }
         } else if (unwrap) {
            Date parsed = this._parseDate(p, ctxt);
            this._verifyEndArrayForSingle(p, ctxt);
            return parsed;
         }
      }

      return (Date)ctxt.handleUnexpectedToken(this._valueClass, JsonToken.START_ARRAY, p, null);
   }

   protected Date _parseDate(String value, DeserializationContext ctxt) throws IOException {
      try {
         if (value.isEmpty()) {
            CoercionAction act = this._checkFromStringCoercion(ctxt, value);
            switch(act) {
               case AsEmpty:
                  return new Date(0L);
               case AsNull:
               case TryConvert:
               default:
                  return null;
            }
         } else {
            return this._hasTextualNull(value) ? null : ctxt.parseDate(value);
         }
      } catch (IllegalArgumentException var4) {
         return (Date)ctxt.handleWeirdStringValue(this._valueClass, value, "not a valid representation (error: %s)", ClassUtil.exceptionMessage(var4));
      }
   }

   protected final String _parseString(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.VALUE_STRING)) {
         return p.getText();
      } else if (p.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
         Object ob = p.getEmbeddedObject();
         if (ob instanceof byte[]) {
            return ctxt.getBase64Variant().encode((byte[])ob, false);
         } else {
            return ob == null ? null : ob.toString();
         }
      } else if (p.hasToken(JsonToken.START_OBJECT)) {
         return ctxt.extractScalarFromObject(p, this, this._valueClass);
      } else {
         String value = p.getValueAsString();
         return value != null ? value : (String)ctxt.handleUnexpectedToken(String.class, p);
      }
   }

   protected boolean _hasTextualNull(String value) {
      return "null".equals(value);
   }

   protected final boolean _isNegInf(String text) {
      return "-Infinity".equals(text) || "-INF".equals(text);
   }

   protected final boolean _isPosInf(String text) {
      return "Infinity".equals(text) || "INF".equals(text);
   }

   protected final boolean _isNaN(String text) {
      return "NaN".equals(text);
   }

   protected static final boolean _isBlank(String text) {
      int len = text.length();

      for(int i = 0; i < len; ++i) {
         if (text.charAt(i) > ' ') {
            return false;
         }
      }

      return true;
   }

   protected CoercionAction _checkFromStringCoercion(DeserializationContext ctxt, String value) throws IOException {
      return this._checkFromStringCoercion(ctxt, value, this.logicalType(), this.handledType());
   }

   protected CoercionAction _checkFromStringCoercion(DeserializationContext ctxt, String value, LogicalType logicalType, Class<?> rawTargetType) throws IOException {
      if (value.isEmpty()) {
         CoercionAction act = ctxt.findCoercionAction(logicalType, rawTargetType, CoercionInputShape.EmptyString);
         return this._checkCoercionFail(ctxt, act, rawTargetType, value, "empty String (\"\")");
      } else if (_isBlank(value)) {
         CoercionAction act = ctxt.findCoercionFromBlankString(logicalType, rawTargetType, CoercionAction.Fail);
         return this._checkCoercionFail(ctxt, act, rawTargetType, value, "blank String (all whitespace)");
      } else if (ctxt.isEnabled(StreamReadCapability.UNTYPED_SCALARS)) {
         return CoercionAction.TryConvert;
      } else {
         CoercionAction act = ctxt.findCoercionAction(logicalType, rawTargetType, CoercionInputShape.String);
         if (act == CoercionAction.Fail) {
            ctxt.reportInputMismatch(
               this, "Cannot coerce String value (\"%s\") to %s (but might if coercion using `CoercionConfig` was enabled)", value, this._coercedTypeDesc()
            );
         }

         return act;
      }
   }

   protected CoercionAction _checkFloatToIntCoercion(JsonParser p, DeserializationContext ctxt, Class<?> rawTargetType) throws IOException {
      CoercionAction act = ctxt.findCoercionAction(LogicalType.Integer, rawTargetType, CoercionInputShape.Float);
      return act == CoercionAction.Fail
         ? this._checkCoercionFail(ctxt, act, rawTargetType, p.getNumberValue(), "Floating-point value (" + p.getText() + ")")
         : act;
   }

   protected Boolean _coerceBooleanFromInt(JsonParser p, DeserializationContext ctxt, Class<?> rawTargetType) throws IOException {
      CoercionAction act = ctxt.findCoercionAction(LogicalType.Boolean, rawTargetType, CoercionInputShape.Integer);
      switch(act) {
         case AsEmpty:
            return Boolean.FALSE;
         case AsNull:
            return null;
         case TryConvert:
         default:
            if (p.getNumberType() == JsonParser.NumberType.INT) {
               return p.getIntValue() != 0;
            }

            return !"0".equals(p.getText());
         case Fail:
            this._checkCoercionFail(ctxt, act, rawTargetType, p.getNumberValue(), "Integer value (" + p.getText() + ")");
            return Boolean.FALSE;
      }
   }

   protected CoercionAction _checkCoercionFail(DeserializationContext ctxt, CoercionAction act, Class<?> targetType, Object inputValue, String inputDesc) throws IOException {
      if (act == CoercionAction.Fail) {
         ctxt.reportBadCoercion(
            this,
            targetType,
            inputValue,
            "Cannot coerce %s to %s (but could if coercion was enabled using `CoercionConfig`)",
            inputDesc,
            this._coercedTypeDesc()
         );
      }

      return act;
   }

   protected boolean _checkTextualNull(DeserializationContext ctxt, String text) throws JsonMappingException {
      if (this._hasTextualNull(text)) {
         if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
            this._reportFailedNullCoerce(ctxt, true, MapperFeature.ALLOW_COERCION_OF_SCALARS, "String \"null\"");
         }

         return true;
      } else {
         return false;
      }
   }

   protected Object _coerceIntegral(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (ctxt.isEnabled(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)) {
         return p.getBigIntegerValue();
      } else {
         return ctxt.isEnabled(DeserializationFeature.USE_LONG_FOR_INTS) ? p.getLongValue() : p.getNumberValue();
      }
   }

   protected final void _verifyNullForPrimitive(DeserializationContext ctxt) throws JsonMappingException {
      if (ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
         ctxt.reportInputMismatch(
            this, "Cannot coerce `null` to %s (disable `DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES` to allow)", this._coercedTypeDesc()
         );
      }

   }

   protected final void _verifyNullForPrimitiveCoercion(DeserializationContext ctxt, String str) throws JsonMappingException {
      Enum<?> feat;
      boolean enable;
      if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
         feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
         enable = true;
      } else {
         if (!ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
            return;
         }

         feat = DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
         enable = false;
      }

      String strDesc = str.isEmpty() ? "empty String (\"\")" : String.format("String \"%s\"", str);
      this._reportFailedNullCoerce(ctxt, enable, feat, strDesc);
   }

   protected void _reportFailedNullCoerce(DeserializationContext ctxt, boolean state, Enum<?> feature, String inputDesc) throws JsonMappingException {
      String enableDesc = state ? "enable" : "disable";
      ctxt.reportInputMismatch(
         this,
         "Cannot coerce %s to Null value as %s (%s `%s.%s` to allow)",
         inputDesc,
         this._coercedTypeDesc(),
         enableDesc,
         feature.getDeclaringClass().getSimpleName(),
         feature.name()
      );
   }

   protected String _coercedTypeDesc() {
      JavaType t = this.getValueType();
      boolean structured;
      String typeDesc;
      if (t != null && !t.isPrimitive()) {
         structured = t.isContainerType() || t.isReferenceType();
         typeDesc = ClassUtil.getTypeDescription(t);
      } else {
         Class<?> cls = this.handledType();
         structured = cls.isArray() || Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls);
         typeDesc = ClassUtil.getClassDescription(cls);
      }

      return structured ? "element of " + typeDesc : typeDesc + " value";
   }

   @Deprecated
   protected boolean _parseBooleanFromInt(JsonParser p, DeserializationContext ctxt) throws IOException {
      this._verifyNumberForScalarCoercion(ctxt, p);
      return !"0".equals(p.getText());
   }

   @Deprecated
   protected void _verifyStringForScalarCoercion(DeserializationContext ctxt, String str) throws JsonMappingException {
      MapperFeature feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
      if (!ctxt.isEnabled(feat)) {
         ctxt.reportInputMismatch(
            this,
            "Cannot coerce String \"%s\" to %s (enable `%s.%s` to allow)",
            str,
            this._coercedTypeDesc(),
            feat.getDeclaringClass().getSimpleName(),
            feat.name()
         );
      }

   }

   @Deprecated
   protected Object _coerceEmptyString(DeserializationContext ctxt, boolean isPrimitive) throws JsonMappingException {
      Enum<?> feat;
      boolean enable;
      if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
         feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
         enable = true;
      } else {
         if (!isPrimitive || !ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
            return this.getNullValue(ctxt);
         }

         feat = DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
         enable = false;
      }

      this._reportFailedNullCoerce(ctxt, enable, feat, "empty String (\"\")");
      return null;
   }

   @Deprecated
   protected void _failDoubleToIntCoercion(JsonParser p, DeserializationContext ctxt, String type) throws IOException {
      ctxt.reportInputMismatch(
         this.handledType(),
         "Cannot coerce a floating-point value ('%s') into %s (enable `DeserializationFeature.ACCEPT_FLOAT_AS_INT` to allow)",
         p.getValueAsString(),
         type
      );
   }

   @Deprecated
   protected final void _verifyNullForScalarCoercion(DeserializationContext ctxt, String str) throws JsonMappingException {
      if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
         String strDesc = str.isEmpty() ? "empty String (\"\")" : String.format("String \"%s\"", str);
         this._reportFailedNullCoerce(ctxt, true, MapperFeature.ALLOW_COERCION_OF_SCALARS, strDesc);
      }

   }

   @Deprecated
   protected void _verifyNumberForScalarCoercion(DeserializationContext ctxt, JsonParser p) throws IOException {
      MapperFeature feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
      if (!ctxt.isEnabled(feat)) {
         String valueDesc = p.getText();
         ctxt.reportInputMismatch(
            this,
            "Cannot coerce Number (%s) to %s (enable `%s.%s` to allow)",
            valueDesc,
            this._coercedTypeDesc(),
            feat.getDeclaringClass().getSimpleName(),
            feat.name()
         );
      }

   }

   @Deprecated
   protected Object _coerceNullToken(DeserializationContext ctxt, boolean isPrimitive) throws JsonMappingException {
      if (isPrimitive) {
         this._verifyNullForPrimitive(ctxt);
      }

      return this.getNullValue(ctxt);
   }

   @Deprecated
   protected Object _coerceTextualNull(DeserializationContext ctxt, boolean isPrimitive) throws JsonMappingException {
      if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
         this._reportFailedNullCoerce(ctxt, true, MapperFeature.ALLOW_COERCION_OF_SCALARS, "String \"null\"");
      }

      return this.getNullValue(ctxt);
   }

   @Deprecated
   protected boolean _isEmptyOrTextualNull(String value) {
      return value.isEmpty() || "null".equals(value);
   }

   protected JsonDeserializer<Object> findDeserializer(DeserializationContext ctxt, JavaType type, BeanProperty property) throws JsonMappingException {
      return ctxt.findContextualValueDeserializer(type, property);
   }

   protected final boolean _isIntNumber(String text) {
      int len = text.length();
      if (len <= 0) {
         return false;
      } else {
         char c = text.charAt(0);
         int i;
         if (c != '-' && c != '+') {
            i = 0;
         } else {
            if (len == 1) {
               return false;
            }

            i = 1;
         }

         while(i < len) {
            int ch = text.charAt(i);
            if (ch > 57 || ch < 48) {
               return false;
            }

            ++i;
         }

         return true;
      }
   }

   protected JsonDeserializer<?> findConvertingContentDeserializer(DeserializationContext ctxt, BeanProperty prop, JsonDeserializer<?> existingDeserializer) throws JsonMappingException {
      AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
      if (_neitherNull(intr, prop)) {
         AnnotatedMember member = prop.getMember();
         if (member != null) {
            Object convDef = intr.findDeserializationContentConverter(member);
            if (convDef != null) {
               Converter<Object, Object> conv = ctxt.converterInstance(prop.getMember(), convDef);
               JavaType delegateType = conv.getInputType(ctxt.getTypeFactory());
               if (existingDeserializer == null) {
                  existingDeserializer = ctxt.findContextualValueDeserializer(delegateType, prop);
               }

               return new StdDelegatingDeserializer<>(conv, delegateType, existingDeserializer);
            }
         }
      }

      return existingDeserializer;
   }

   protected JsonFormat.Value findFormatOverrides(DeserializationContext ctxt, BeanProperty prop, Class<?> typeForDefaults) {
      return prop != null ? prop.findPropertyFormat(ctxt.getConfig(), typeForDefaults) : ctxt.getDefaultPropertyFormat(typeForDefaults);
   }

   protected Boolean findFormatFeature(DeserializationContext ctxt, BeanProperty prop, Class<?> typeForDefaults, JsonFormat.Feature feat) {
      JsonFormat.Value format = this.findFormatOverrides(ctxt, prop, typeForDefaults);
      return format != null ? format.getFeature(feat) : null;
   }

   protected final NullValueProvider findValueNullProvider(DeserializationContext ctxt, SettableBeanProperty prop, PropertyMetadata propMetadata) throws JsonMappingException {
      return prop != null ? this._findNullProvider(ctxt, prop, propMetadata.getValueNulls(), prop.getValueDeserializer()) : null;
   }

   protected NullValueProvider findContentNullProvider(DeserializationContext ctxt, BeanProperty prop, JsonDeserializer<?> valueDeser) throws JsonMappingException {
      Nulls nulls = this.findContentNullStyle(ctxt, prop);
      if (nulls == Nulls.SKIP) {
         return NullsConstantProvider.skipper();
      } else if (nulls == Nulls.FAIL) {
         if (prop == null) {
            JavaType type = ctxt.constructType(valueDeser.handledType());
            if (type.isContainerType()) {
               type = type.getContentType();
            }

            return NullsFailProvider.constructForRootValue(type);
         } else {
            return NullsFailProvider.constructForProperty(prop, prop.getType().getContentType());
         }
      } else {
         NullValueProvider prov = this._findNullProvider(ctxt, prop, nulls, valueDeser);
         return (NullValueProvider)(prov != null ? prov : valueDeser);
      }
   }

   protected Nulls findContentNullStyle(DeserializationContext ctxt, BeanProperty prop) throws JsonMappingException {
      return prop != null ? prop.getMetadata().getContentNulls() : ctxt.getConfig().getDefaultSetterInfo().getContentNulls();
   }

   protected final NullValueProvider _findNullProvider(DeserializationContext ctxt, BeanProperty prop, Nulls nulls, JsonDeserializer<?> valueDeser) throws JsonMappingException {
      if (nulls == Nulls.FAIL) {
         if (prop == null) {
            Class<?> rawType = valueDeser == null ? Object.class : valueDeser.handledType();
            return NullsFailProvider.constructForRootValue(ctxt.constructType(rawType));
         } else {
            return NullsFailProvider.constructForProperty(prop);
         }
      } else if (nulls == Nulls.AS_EMPTY) {
         if (valueDeser == null) {
            return null;
         } else {
            if (valueDeser instanceof BeanDeserializerBase) {
               BeanDeserializerBase bd = (BeanDeserializerBase)valueDeser;
               ValueInstantiator vi = bd.getValueInstantiator();
               if (!vi.canCreateUsingDefault()) {
                  JavaType type = prop == null ? bd.getValueType() : prop.getType();
                  return ctxt.reportBadDefinition(type, String.format("Cannot create empty instance of %s, no default Creator", type));
               }
            }

            AccessPattern access = valueDeser.getEmptyAccessPattern();
            if (access == AccessPattern.ALWAYS_NULL) {
               return NullsConstantProvider.nuller();
            } else {
               return (NullValueProvider)(access == AccessPattern.CONSTANT
                  ? NullsConstantProvider.forValue(valueDeser.getEmptyValue(ctxt))
                  : new NullsAsEmptyProvider(valueDeser));
            }
         }
      } else {
         return nulls == Nulls.SKIP ? NullsConstantProvider.skipper() : null;
      }
   }

   protected CoercionAction _findCoercionFromEmptyString(DeserializationContext ctxt) {
      return ctxt.findCoercionAction(this.logicalType(), this.handledType(), CoercionInputShape.EmptyString);
   }

   protected CoercionAction _findCoercionFromEmptyArray(DeserializationContext ctxt) {
      return ctxt.findCoercionAction(this.logicalType(), this.handledType(), CoercionInputShape.EmptyArray);
   }

   protected CoercionAction _findCoercionFromBlankString(DeserializationContext ctxt) {
      return ctxt.findCoercionFromBlankString(this.logicalType(), this.handledType(), CoercionAction.Fail);
   }

   protected void handleUnknownProperty(JsonParser p, DeserializationContext ctxt, Object instanceOrClass, String propName) throws IOException {
      if (instanceOrClass == null) {
         instanceOrClass = this.handledType();
      }

      if (!ctxt.handleUnknownProperty(p, this, instanceOrClass, propName)) {
         p.skipChildren();
      }
   }

   protected void handleMissingEndArrayForSingle(JsonParser p, DeserializationContext ctxt) throws IOException {
      ctxt.reportWrongTokenException(
         this,
         JsonToken.END_ARRAY,
         "Attempted to unwrap '%s' value from an array (with `DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS`) but it contains more than one value",
         this.handledType().getName()
      );
   }

   protected void _verifyEndArrayForSingle(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonToken t = p.nextToken();
      if (t != JsonToken.END_ARRAY) {
         this.handleMissingEndArrayForSingle(p, ctxt);
      }

   }

   protected static final boolean _neitherNull(Object a, Object b) {
      return a != null && b != null;
   }

   protected final boolean _byteOverflow(int value) {
      return value < -128 || value > 255;
   }

   protected final boolean _shortOverflow(int value) {
      return value < -32768 || value > 32767;
   }

   protected final boolean _intOverflow(long value) {
      return value < -2147483648L || value > 2147483647L;
   }

   protected Number _nonNullNumber(Number n) {
      if (n == null) {
         n = 0;
      }

      return n;
   }
}
