package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer extends JSR310DateTimeDeserializerBase<LocalDateTime> {
   private static final long serialVersionUID = 1L;
   private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
   public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer();

   protected LocalDateTimeDeserializer() {
      this(DEFAULT_FORMATTER);
   }

   public LocalDateTimeDeserializer(DateTimeFormatter formatter) {
      super(LocalDateTime.class, formatter);
   }

   protected LocalDateTimeDeserializer(LocalDateTimeDeserializer base, Boolean leniency) {
      super(base, leniency);
   }

   protected LocalDateTimeDeserializer withDateFormat(DateTimeFormatter formatter) {
      return new LocalDateTimeDeserializer(formatter);
   }

   protected LocalDateTimeDeserializer withLeniency(Boolean leniency) {
      return new LocalDateTimeDeserializer(this, leniency);
   }

   protected LocalDateTimeDeserializer withShape(JsonFormat.Shape shape) {
      return this;
   }

   public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      if (parser.hasTokenId(6)) {
         return this._fromString(parser, context, parser.getText());
      } else if (parser.isExpectedStartObjectToken()) {
         return this._fromString(parser, context, context.extractScalarFromObject(parser, this, this.handledType()));
      } else {
         if (parser.isExpectedStartArrayToken()) {
            JsonToken t = parser.nextToken();
            if (t == JsonToken.END_ARRAY) {
               return null;
            }

            if ((t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT) && context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
               LocalDateTime parsed = this.deserialize(parser, context);
               if (parser.nextToken() != JsonToken.END_ARRAY) {
                  this.handleMissingEndArrayForSingle(parser, context);
               }

               return parsed;
            }

            if (t == JsonToken.VALUE_NUMBER_INT) {
               int year = parser.getIntValue();
               int month = parser.nextIntValue(-1);
               int day = parser.nextIntValue(-1);
               int hour = parser.nextIntValue(-1);
               int minute = parser.nextIntValue(-1);
               t = parser.nextToken();
               LocalDateTime result;
               if (t == JsonToken.END_ARRAY) {
                  result = LocalDateTime.of(year, month, day, hour, minute);
               } else {
                  int second = parser.getIntValue();
                  t = parser.nextToken();
                  if (t == JsonToken.END_ARRAY) {
                     result = LocalDateTime.of(year, month, day, hour, minute, second);
                  } else {
                     int partialSecond = parser.getIntValue();
                     if (partialSecond < 1000 && !context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)) {
                        partialSecond *= 1000000;
                     }

                     if (parser.nextToken() != JsonToken.END_ARRAY) {
                        throw context.wrongTokenException(parser, this.handledType(), JsonToken.END_ARRAY, "Expected array to end");
                     }

                     result = LocalDateTime.of(year, month, day, hour, minute, second, partialSecond);
                  }
               }

               return result;
            }

            context.reportInputMismatch(this.handledType(), "Unexpected token (%s) within Array, expected VALUE_NUMBER_INT", t);
         }

         if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
            return (LocalDateTime)parser.getEmbeddedObject();
         } else {
            if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
               this._throwNoNumericTimestampNeedTimeZone(parser, context);
            }

            return this._handleUnexpectedToken(context, parser, "Expected array or string.", new Object[0]);
         }
      }
   }

   protected LocalDateTime _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
      String string = string0.trim();
      if (string.length() == 0) {
         return this._fromEmptyString(p, ctxt, string);
      } else {
         try {
            if (this._formatter != DEFAULT_FORMATTER || string.length() <= 10 || string.charAt(10) != 'T' || !string.endsWith("Z")) {
               return LocalDateTime.parse(string, this._formatter);
            } else if (this.isLenient()) {
               return LocalDateTime.parse(string.substring(0, string.length() - 1), this._formatter);
            } else {
               JavaType t = this.getValueType(ctxt);
               return (LocalDateTime)ctxt.handleWeirdStringValue(
                  t.getRawClass(), string, "Should not contain offset when 'strict' mode set for property or type (enable 'lenient' handling to allow)"
               );
            }
         } catch (DateTimeException var6) {
            return this._handleDateTimeException(ctxt, var6, string);
         }
      }
   }
}
