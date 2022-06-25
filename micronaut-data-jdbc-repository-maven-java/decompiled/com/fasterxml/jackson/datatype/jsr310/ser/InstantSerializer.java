package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantSerializer extends InstantSerializerBase<Instant> {
   private static final long serialVersionUID = 1L;
   public static final InstantSerializer INSTANCE = new InstantSerializer();

   protected InstantSerializer() {
      super(Instant.class, Instant::toEpochMilli, Instant::getEpochSecond, Instant::getNano, null);
   }

   protected InstantSerializer(InstantSerializer base, Boolean useTimestamp, DateTimeFormatter formatter) {
      this(base, useTimestamp, null, formatter);
   }

   protected InstantSerializer(InstantSerializer base, Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter formatter) {
      super(base, useTimestamp, useNanoseconds, formatter);
   }

   @Override
   protected JSR310FormattedSerializerBase<Instant> withFormat(Boolean useTimestamp, DateTimeFormatter formatter, JsonFormat.Shape shape) {
      return new InstantSerializer(this, useTimestamp, formatter);
   }

   @Override
   protected JSR310FormattedSerializerBase<?> withFeatures(Boolean writeZoneId, Boolean writeNanoseconds) {
      return new InstantSerializer(this, this._useTimestamp, writeNanoseconds, this._formatter);
   }
}
