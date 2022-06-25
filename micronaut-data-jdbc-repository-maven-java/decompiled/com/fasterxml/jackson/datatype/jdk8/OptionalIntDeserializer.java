package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.IOException;
import java.util.OptionalInt;

public class OptionalIntDeserializer extends BaseScalarOptionalDeserializer<OptionalInt> {
   private static final long serialVersionUID = 1L;
   static final OptionalIntDeserializer INSTANCE = new OptionalIntDeserializer();

   public OptionalIntDeserializer() {
      super(OptionalInt.class, OptionalInt.empty());
   }

   @Override
   public LogicalType logicalType() {
      return LogicalType.Integer;
   }

   public OptionalInt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
         return OptionalInt.of(p.getIntValue());
      } else {
         switch(p.currentTokenId()) {
            case 3:
               return this._deserializeFromArray(p, ctxt);
            case 4:
            case 5:
            case 7:
            case 9:
            case 10:
            default:
               return (OptionalInt)ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
            case 6:
               String text = p.getText();
               CoercionAction act = this._checkFromStringCoercion(ctxt, text);
               if (act != CoercionAction.AsNull && act != CoercionAction.AsEmpty) {
                  text = text.trim();
                  return OptionalInt.of(this._parseIntPrimitive(ctxt, text));
               }

               return this._empty;
            case 8:
               CoercionAction act = this._checkFloatToIntCoercion(p, ctxt, this._valueClass);
               if (act != CoercionAction.AsNull && act != CoercionAction.AsEmpty) {
                  return OptionalInt.of(p.getValueAsInt());
               }

               return this._empty;
            case 11:
               return this._empty;
         }
      }
   }
}
