package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class OffsetTimeSerializer extends JSR310FormattedSerializerBase<OffsetTime> {
   private static final long serialVersionUID = 1L;
   public static final OffsetTimeSerializer INSTANCE = new OffsetTimeSerializer();

   protected OffsetTimeSerializer() {
      super(OffsetTime.class);
   }

   protected OffsetTimeSerializer(OffsetTimeSerializer base, Boolean useTimestamp, DateTimeFormatter dtf) {
      this(base, useTimestamp, null, dtf);
   }

   protected OffsetTimeSerializer(OffsetTimeSerializer base, Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter dtf) {
      super(base, useTimestamp, useNanoseconds, dtf, null);
   }

   protected OffsetTimeSerializer withFormat(Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape) {
      return new OffsetTimeSerializer(this, useTimestamp, dtf);
   }

   public void serialize(OffsetTime time, JsonGenerator g, SerializerProvider provider) throws IOException {
      if (this.useTimestamp(provider)) {
         g.writeStartArray();
         this._serializeAsArrayContents(time, g, provider);
         g.writeEndArray();
      } else {
         String str = this._formatter == null ? time.toString() : time.format(this._formatter);
         g.writeString(str);
      }

   }

   public void serializeWithType(OffsetTime value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, this.serializationShape(provider)));
      if (typeIdDef.valueShape == JsonToken.START_ARRAY) {
         this._serializeAsArrayContents(value, g, provider);
      } else {
         String str = this._formatter == null ? value.toString() : value.format(this._formatter);
         g.writeString(str);
      }

      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   private final void _serializeAsArrayContents(OffsetTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeNumber(value.getHour());
      g.writeNumber(value.getMinute());
      int secs = value.getSecond();
      int nanos = value.getNano();
      if (secs > 0 || nanos > 0) {
         g.writeNumber(secs);
         if (nanos > 0) {
            if (this.useNanoseconds(provider)) {
               g.writeNumber(nanos);
            } else {
               g.writeNumber(value.get(ChronoField.MILLI_OF_SECOND));
            }
         }
      }

      g.writeString(value.getOffset().toString());
   }

   @Override
   protected JsonToken serializationShape(SerializerProvider provider) {
      return this.useTimestamp(provider) ? JsonToken.START_ARRAY : JsonToken.VALUE_STRING;
   }

   @Override
   protected JSR310FormattedSerializerBase<?> withFeatures(Boolean writeZoneId, Boolean writeNanoseconds) {
      return new OffsetTimeSerializer(this, this._useTimestamp, writeNanoseconds, this._formatter);
   }
}
