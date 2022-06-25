package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

public class MonthDayDeserializer extends JSR310DateTimeDeserializerBase<MonthDay> {
   private static final long serialVersionUID = 1L;
   public static final MonthDayDeserializer INSTANCE = new MonthDayDeserializer();

   public MonthDayDeserializer() {
      this(null);
   }

   public MonthDayDeserializer(DateTimeFormatter formatter) {
      super(MonthDay.class, formatter);
   }

   protected MonthDayDeserializer withDateFormat(DateTimeFormatter dtf) {
      return new MonthDayDeserializer(dtf);
   }

   protected MonthDayDeserializer(MonthDayDeserializer base, Boolean leniency) {
      super(base, leniency);
   }

   protected MonthDayDeserializer withLeniency(Boolean leniency) {
      return new MonthDayDeserializer(this, leniency);
   }

   protected MonthDayDeserializer withShape(JsonFormat.Shape shape) {
      return this;
   }

   public MonthDay deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      if (parser.hasToken(JsonToken.VALUE_STRING)) {
         return this._fromString(parser, context, parser.getText());
      } else if (parser.isExpectedStartObjectToken()) {
         return this._fromString(parser, context, context.extractScalarFromObject(parser, this, this.handledType()));
      } else if (parser.isExpectedStartArrayToken()) {
         JsonToken t = parser.nextToken();
         if (t == JsonToken.END_ARRAY) {
            return null;
         } else if ((t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT)
            && context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            MonthDay parsed = this.deserialize(parser, context);
            if (parser.nextToken() != JsonToken.END_ARRAY) {
               this.handleMissingEndArrayForSingle(parser, context);
            }

            return parsed;
         } else {
            if (t != JsonToken.VALUE_NUMBER_INT) {
               this._reportWrongToken(context, JsonToken.VALUE_NUMBER_INT, "month");
            }

            int month = parser.getIntValue();
            int day = parser.nextIntValue(-1);
            if (day == -1) {
               if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                  this._reportWrongToken(context, JsonToken.VALUE_NUMBER_INT, "day");
               }

               day = parser.getIntValue();
            }

            if (parser.nextToken() != JsonToken.END_ARRAY) {
               throw context.wrongTokenException(parser, this.handledType(), JsonToken.END_ARRAY, "Expected array to end");
            } else {
               return MonthDay.of(month, day);
            }
         }
      } else {
         return parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)
            ? (MonthDay)parser.getEmbeddedObject()
            : this._handleUnexpectedToken(context, parser, new JsonToken[]{JsonToken.VALUE_STRING, JsonToken.START_ARRAY});
      }
   }

   protected MonthDay _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
      String string = string0.trim();
      if (string.length() == 0) {
         return this._fromEmptyString(p, ctxt, string);
      } else {
         try {
            return this._formatter == null ? MonthDay.parse(string) : MonthDay.parse(string, this._formatter);
         } catch (DateTimeException var6) {
            return this._handleDateTimeException(ctxt, var6, string);
         }
      }
   }
}
