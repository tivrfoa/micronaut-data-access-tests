package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;

@Deprecated
public class ZonedDateTimeWithZoneIdSerializer extends InstantSerializerBase<ZonedDateTime> {
   private static final long serialVersionUID = 1L;
   public static final ZonedDateTimeWithZoneIdSerializer INSTANCE = new ZonedDateTimeWithZoneIdSerializer();

   protected ZonedDateTimeWithZoneIdSerializer() {
      super(ZonedDateTime.class, dt -> dt.toInstant().toEpochMilli(), ChronoZonedDateTime::toEpochSecond, ZonedDateTime::getNano, null);
   }

   protected ZonedDateTimeWithZoneIdSerializer(ZonedDateTimeWithZoneIdSerializer base, Boolean useTimestamp, DateTimeFormatter formatter) {
      this(base, useTimestamp, null, formatter);
   }

   protected ZonedDateTimeWithZoneIdSerializer(
      ZonedDateTimeWithZoneIdSerializer base, Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter formatter
   ) {
      super(base, useTimestamp, useNanoseconds, formatter);
   }

   @Override
   protected JSR310FormattedSerializerBase<?> withFormat(Boolean useTimestamp, DateTimeFormatter formatter, JsonFormat.Shape shape) {
      return new ZonedDateTimeWithZoneIdSerializer(this, useTimestamp, formatter);
   }

   @Override
   protected JSR310FormattedSerializerBase<?> withFeatures(Boolean writeZoneId, Boolean writeNanoseconds) {
      return new ZonedDateTimeWithZoneIdSerializer(this, this._useTimestamp, writeNanoseconds, this._formatter);
   }
}
