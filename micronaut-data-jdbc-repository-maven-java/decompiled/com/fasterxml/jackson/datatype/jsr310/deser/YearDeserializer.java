package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadCapability;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Year;
import java.time.format.DateTimeFormatter;

public class YearDeserializer extends JSR310DateTimeDeserializerBase<Year> {
   private static final long serialVersionUID = 1L;
   public static final YearDeserializer INSTANCE = new YearDeserializer();

   public YearDeserializer() {
      this(null);
   }

   public YearDeserializer(DateTimeFormatter formatter) {
      super(Year.class, formatter);
   }

   protected YearDeserializer(YearDeserializer base, Boolean leniency) {
      super(base, leniency);
   }

   protected YearDeserializer withDateFormat(DateTimeFormatter dtf) {
      return new YearDeserializer(dtf);
   }

   protected YearDeserializer withLeniency(Boolean leniency) {
      return new YearDeserializer(this, leniency);
   }

   protected YearDeserializer withShape(JsonFormat.Shape shape) {
      return this;
   }

   public Year deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      JsonToken t = parser.currentToken();
      if (t == JsonToken.VALUE_STRING) {
         return this._fromString(parser, context, parser.getText());
      } else if (t == JsonToken.START_OBJECT) {
         return this._fromString(parser, context, context.extractScalarFromObject(parser, this, this.handledType()));
      } else if (t == JsonToken.VALUE_NUMBER_INT) {
         return this._fromNumber(context, parser.getIntValue());
      } else if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
         return (Year)parser.getEmbeddedObject();
      } else {
         return parser.hasToken(JsonToken.START_ARRAY)
            ? this._deserializeFromArray(parser, context)
            : this._handleUnexpectedToken(context, parser, new JsonToken[]{JsonToken.VALUE_STRING, JsonToken.VALUE_NUMBER_INT});
      }
   }

   protected Year _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
      String string = string0.trim();
      if (string.length() == 0) {
         return this._fromEmptyString(p, ctxt, string);
      } else if (ctxt.isEnabled(StreamReadCapability.UNTYPED_SCALARS) && this._isValidTimestampString(string)) {
         return this._fromNumber(ctxt, NumberInput.parseInt(string));
      } else {
         try {
            return this._formatter == null ? Year.parse(string) : Year.parse(string, this._formatter);
         } catch (DateTimeException var6) {
            return this._handleDateTimeException(ctxt, var6, string);
         }
      }
   }

   protected Year _fromNumber(DeserializationContext ctxt, int value) {
      return Year.of(value);
   }
}
