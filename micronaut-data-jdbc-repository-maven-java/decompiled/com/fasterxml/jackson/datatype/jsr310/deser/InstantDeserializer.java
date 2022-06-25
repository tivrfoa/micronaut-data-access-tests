package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstantDeserializer<T extends Temporal> extends JSR310DateTimeDeserializerBase<T> {
   private static final long serialVersionUID = 1L;
   private static final Pattern ISO8601_UTC_ZERO_OFFSET_SUFFIX_REGEX = Pattern.compile("\\+00:?(00)?$");
   protected static final Pattern ISO8601_COLONLESS_OFFSET_REGEX = Pattern.compile("[+-][0-9]{4}(?=\\[|$)");
   public static final InstantDeserializer<Instant> INSTANT = new InstantDeserializer(
      Instant.class,
      DateTimeFormatter.ISO_INSTANT,
      Instant::from,
      a -> Instant.ofEpochMilli(a.value),
      a -> Instant.ofEpochSecond(a.integer, (long)a.fraction),
      null,
      true
   );
   public static final InstantDeserializer<OffsetDateTime> OFFSET_DATE_TIME = new InstantDeserializer(
      OffsetDateTime.class,
      DateTimeFormatter.ISO_OFFSET_DATE_TIME,
      OffsetDateTime::from,
      a -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
      a -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, (long)a.fraction), a.zoneId),
      (d, z) -> !d.isEqual(OffsetDateTime.MIN) && !d.isEqual(OffsetDateTime.MAX) ? d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime())) : d,
      true
   );
   public static final InstantDeserializer<ZonedDateTime> ZONED_DATE_TIME = new InstantDeserializer(
      ZonedDateTime.class,
      DateTimeFormatter.ISO_ZONED_DATE_TIME,
      ZonedDateTime::from,
      a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
      a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, (long)a.fraction), a.zoneId),
      ZonedDateTime::withZoneSameInstant,
      false
   );
   protected final Function<InstantDeserializer.FromIntegerArguments, T> fromMilliseconds;
   protected final Function<InstantDeserializer.FromDecimalArguments, T> fromNanoseconds;
   protected final Function<TemporalAccessor, T> parsedToValue;
   protected final BiFunction<T, ZoneId, T> adjust;
   protected final boolean replaceZeroOffsetAsZ;
   protected final Boolean _adjustToContextTZOverride;

   protected InstantDeserializer(
      Class<T> supportedType,
      DateTimeFormatter formatter,
      Function<TemporalAccessor, T> parsedToValue,
      Function<InstantDeserializer.FromIntegerArguments, T> fromMilliseconds,
      Function<InstantDeserializer.FromDecimalArguments, T> fromNanoseconds,
      BiFunction<T, ZoneId, T> adjust,
      boolean replaceZeroOffsetAsZ
   ) {
      super(supportedType, formatter);
      this.parsedToValue = parsedToValue;
      this.fromMilliseconds = fromMilliseconds;
      this.fromNanoseconds = fromNanoseconds;
      this.adjust = adjust == null ? (d, z) -> d : adjust;
      this.replaceZeroOffsetAsZ = replaceZeroOffsetAsZ;
      this._adjustToContextTZOverride = null;
   }

   protected InstantDeserializer(InstantDeserializer<T> base, DateTimeFormatter f) {
      super(base.handledType(), f);
      this.parsedToValue = base.parsedToValue;
      this.fromMilliseconds = base.fromMilliseconds;
      this.fromNanoseconds = base.fromNanoseconds;
      this.adjust = base.adjust;
      this.replaceZeroOffsetAsZ = this._formatter == DateTimeFormatter.ISO_INSTANT;
      this._adjustToContextTZOverride = base._adjustToContextTZOverride;
   }

   protected InstantDeserializer(InstantDeserializer<T> base, Boolean adjustToContextTimezoneOverride) {
      super(base.handledType(), base._formatter);
      this.parsedToValue = base.parsedToValue;
      this.fromMilliseconds = base.fromMilliseconds;
      this.fromNanoseconds = base.fromNanoseconds;
      this.adjust = base.adjust;
      this.replaceZeroOffsetAsZ = base.replaceZeroOffsetAsZ;
      this._adjustToContextTZOverride = adjustToContextTimezoneOverride;
   }

   protected InstantDeserializer(InstantDeserializer<T> base, DateTimeFormatter f, Boolean leniency) {
      super(base.handledType(), f, leniency);
      this.parsedToValue = base.parsedToValue;
      this.fromMilliseconds = base.fromMilliseconds;
      this.fromNanoseconds = base.fromNanoseconds;
      this.adjust = base.adjust;
      this.replaceZeroOffsetAsZ = this._formatter == DateTimeFormatter.ISO_INSTANT;
      this._adjustToContextTZOverride = base._adjustToContextTZOverride;
   }

   protected InstantDeserializer<T> withDateFormat(DateTimeFormatter dtf) {
      return dtf == this._formatter ? this : new InstantDeserializer<>(this, dtf);
   }

   protected InstantDeserializer<T> withLeniency(Boolean leniency) {
      return new InstantDeserializer<>(this, this._formatter, leniency);
   }

   protected InstantDeserializer<T> withShape(JsonFormat.Shape shape) {
      return this;
   }

   @Override
   protected JSR310DateTimeDeserializerBase<?> _withFormatOverrides(DeserializationContext ctxt, BeanProperty property, JsonFormat.Value formatOverrides) {
      InstantDeserializer<T> deser = (InstantDeserializer)super._withFormatOverrides(ctxt, property, formatOverrides);
      Boolean B = formatOverrides.getFeature(JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
      return !Objects.equals(B, deser._adjustToContextTZOverride) ? new InstantDeserializer<>(deser, B) : deser;
   }

   public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
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
            return this._fromLong(context, parser.getLongValue());
         case 8:
            return this._fromDecimal(context, parser.getDecimalValue());
         case 12:
            return (T)parser.getEmbeddedObject();
      }
   }

   protected boolean shouldAdjustToContextTimezone(DeserializationContext context) {
      return this._adjustToContextTZOverride != null
         ? this._adjustToContextTZOverride
         : context.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
   }

   protected int _countPeriods(String str) {
      int commas = 0;
      int i = 0;

      for(int end = str.length(); i < end; ++i) {
         int ch = str.charAt(i);
         if (ch < 48 || ch > 57) {
            if (ch != 46) {
               return -1;
            }

            ++commas;
         }
      }

      return commas;
   }

   protected T _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
      String string = string0.trim();
      if (string.length() == 0) {
         return this._fromEmptyString(p, ctxt, string);
      } else {
         if (this._formatter == DateTimeFormatter.ISO_INSTANT
            || this._formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME
            || this._formatter == DateTimeFormatter.ISO_ZONED_DATE_TIME) {
            int dots = this._countPeriods(string);
            if (dots >= 0) {
               try {
                  if (dots == 0) {
                     return this._fromLong(ctxt, Long.parseLong(string));
                  }

                  if (dots == 1) {
                     return this._fromDecimal(ctxt, new BigDecimal(string));
                  }
               } catch (NumberFormatException var8) {
               }
            }

            string = this.replaceZeroOffsetAsZIfNecessary(string);
         }

         if (this._formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME || this._formatter == DateTimeFormatter.ISO_ZONED_DATE_TIME) {
            string = this.addInColonToOffsetIfMissing(string);
         }

         T value;
         try {
            TemporalAccessor acc = this._formatter.parse(string);
            value = (T)this.parsedToValue.apply(acc);
            if (this.shouldAdjustToContextTimezone(ctxt)) {
               return (T)this.adjust.apply(value, this.getZone(ctxt));
            }
         } catch (DateTimeException var7) {
            value = this._handleDateTimeException(ctxt, var7, string);
         }

         return value;
      }
   }

   protected T _fromLong(DeserializationContext context, long timestamp) {
      return (T)(context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
         ? this.fromNanoseconds.apply(new InstantDeserializer.FromDecimalArguments(timestamp, 0, this.getZone(context)))
         : this.fromMilliseconds.apply(new InstantDeserializer.FromIntegerArguments(timestamp, this.getZone(context))));
   }

   protected T _fromDecimal(DeserializationContext context, BigDecimal value) {
      InstantDeserializer.FromDecimalArguments args = DecimalUtils.extractSecondsAndNanos(
         value, (s, ns) -> new InstantDeserializer.FromDecimalArguments(s, ns, this.getZone(context))
      );
      return (T)this.fromNanoseconds.apply(args);
   }

   private ZoneId getZone(DeserializationContext context) {
      return this._valueClass == Instant.class ? null : context.getTimeZone().toZoneId();
   }

   private String replaceZeroOffsetAsZIfNecessary(String text) {
      return this.replaceZeroOffsetAsZ ? ISO8601_UTC_ZERO_OFFSET_SUFFIX_REGEX.matcher(text).replaceFirst("Z") : text;
   }

   private String addInColonToOffsetIfMissing(String text) {
      Matcher matcher = ISO8601_COLONLESS_OFFSET_REGEX.matcher(text);
      if (matcher.find()) {
         StringBuilder sb = new StringBuilder(matcher.group(0));
         sb.insert(3, ":");
         return matcher.replaceFirst(sb.toString());
      } else {
         return text;
      }
   }

   public static class FromDecimalArguments {
      public final long integer;
      public final int fraction;
      public final ZoneId zoneId;

      FromDecimalArguments(long integer, int fraction, ZoneId zoneId) {
         this.integer = integer;
         this.fraction = fraction;
         this.zoneId = zoneId;
      }
   }

   public static class FromIntegerArguments {
      public final long value;
      public final ZoneId zoneId;

      FromIntegerArguments(long value, ZoneId zoneId) {
         this.value = value;
         this.zoneId = zoneId;
      }
   }
}
