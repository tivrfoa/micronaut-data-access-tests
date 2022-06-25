package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadCapability;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import com.fasterxml.jackson.datatype.jsr310.util.DurationUnitConverter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Duration;

public class DurationDeserializer extends JSR310DeserializerBase<Duration> implements ContextualDeserializer {
   private static final long serialVersionUID = 1L;
   public static final DurationDeserializer INSTANCE = new DurationDeserializer();
   protected final DurationUnitConverter _durationUnitConverter;

   public DurationDeserializer() {
      super(Duration.class);
      this._durationUnitConverter = null;
   }

   protected DurationDeserializer(DurationDeserializer base, Boolean leniency) {
      super(base, leniency);
      this._durationUnitConverter = base._durationUnitConverter;
   }

   protected DurationDeserializer(DurationDeserializer base, DurationUnitConverter converter) {
      super(base, base._isLenient);
      this._durationUnitConverter = converter;
   }

   protected DurationDeserializer withLeniency(Boolean leniency) {
      return new DurationDeserializer(this, leniency);
   }

   protected DurationDeserializer withConverter(DurationUnitConverter converter) {
      return new DurationDeserializer(this, converter);
   }

   @Override
   public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
      JsonFormat.Value format = this.findFormatOverrides(ctxt, property, this.handledType());
      DurationDeserializer deser = this;
      if (format != null) {
         if (format.hasLenient()) {
            Boolean leniency = format.getLenient();
            if (leniency != null) {
               deser = this.withLeniency(leniency);
            }
         }

         if (format.hasPattern()) {
            String pattern = format.getPattern();
            DurationUnitConverter p = DurationUnitConverter.from(pattern);
            if (p == null) {
               ctxt.reportBadDefinition(
                  this.getValueType(ctxt),
                  String.format("Bad 'pattern' definition (\"%s\") for `Duration`: expected one of [%s]", pattern, DurationUnitConverter.descForAllowed())
               );
            }

            deser = deser.withConverter(p);
         }
      }

      return deser;
   }

   public Duration deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      switch(parser.currentTokenId()) {
         case 1:
            return this._fromString(parser, context, context.extractScalarFromObject(parser, this, this.handledType()));
         case 2:
         case 4:
         case 5:
         case 9:
         case 10:
         case 11:
         default:
            return this._handleUnexpectedToken(
               context, parser, new JsonToken[]{JsonToken.VALUE_STRING, JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_NUMBER_FLOAT}
            );
         case 3:
            return this._deserializeFromArray(parser, context);
         case 6:
            return this._fromString(parser, context, parser.getText());
         case 7:
            return this._fromTimestamp(context, parser.getLongValue());
         case 8:
            BigDecimal value = parser.getDecimalValue();
            return DecimalUtils.extractSecondsAndNanos(value, Duration::ofSeconds);
         case 12:
            return (Duration)parser.getEmbeddedObject();
      }
   }

   protected Duration _fromString(JsonParser parser, DeserializationContext ctxt, String value0) throws IOException {
      String value = value0.trim();
      if (value.length() == 0) {
         return this._fromEmptyString(parser, ctxt, value);
      } else if (ctxt.isEnabled(StreamReadCapability.UNTYPED_SCALARS) && this._isValidTimestampString(value)) {
         return this._fromTimestamp(ctxt, NumberInput.parseLong(value));
      } else {
         try {
            return Duration.parse(value);
         } catch (DateTimeException var6) {
            return this._handleDateTimeException(ctxt, var6, value);
         }
      }
   }

   protected Duration _fromTimestamp(DeserializationContext ctxt, long ts) {
      if (this._durationUnitConverter != null) {
         return this._durationUnitConverter.convert(ts);
      } else {
         return ctxt.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS) ? Duration.ofSeconds(ts) : Duration.ofMillis(ts);
      }
   }
}
