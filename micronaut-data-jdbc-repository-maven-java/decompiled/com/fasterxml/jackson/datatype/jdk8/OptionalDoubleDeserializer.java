package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.IOException;
import java.util.OptionalDouble;

class OptionalDoubleDeserializer extends BaseScalarOptionalDeserializer<OptionalDouble> {
   private static final long serialVersionUID = 1L;
   static final OptionalDoubleDeserializer INSTANCE = new OptionalDoubleDeserializer();

   public OptionalDoubleDeserializer() {
      super(OptionalDouble.class, OptionalDouble.empty());
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Float;
   }

   public OptionalDouble deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
         return OptionalDouble.of(p.getDoubleValue());
      } else {
         switch(p.currentTokenId()) {
            case 3:
               return this._deserializeFromArray(p, ctxt);
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            default:
               return (OptionalDouble)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
            case 6:
               String text = p.getText();
               Double specialValue = this._checkDoubleSpecialValue(text);
               if (specialValue != null) {
                  return OptionalDouble.of(specialValue);
               } else {
                  CoercionAction act = this._checkFromStringCoercion(ctxt, text);
                  if (act != CoercionAction.AsNull && act != CoercionAction.AsEmpty) {
                     text = text.trim();
                     return OptionalDouble.of(this._parseDoublePrimitive(ctxt, text));
                  }

                  return this._empty;
               }
            case 7:
               return OptionalDouble.of(p.getDoubleValue());
            case 11:
               return this.getNullValue(ctxt);
         }
      }
   }
}
