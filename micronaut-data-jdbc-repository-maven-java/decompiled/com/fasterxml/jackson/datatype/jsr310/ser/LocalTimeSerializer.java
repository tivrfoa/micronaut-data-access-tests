package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class LocalTimeSerializer extends JSR310FormattedSerializerBase<LocalTime> {
   private static final long serialVersionUID = 1L;
   public static final LocalTimeSerializer INSTANCE = new LocalTimeSerializer();

   protected LocalTimeSerializer() {
      this(null);
   }

   public LocalTimeSerializer(DateTimeFormatter formatter) {
      super(LocalTime.class, formatter);
   }

   protected LocalTimeSerializer(LocalTimeSerializer base, Boolean useTimestamp, DateTimeFormatter formatter) {
      this(base, useTimestamp, null, formatter);
   }

   protected LocalTimeSerializer(LocalTimeSerializer base, Boolean useTimestamp, Boolean useNanoseconds, DateTimeFormatter formatter) {
      super(base, useTimestamp, useNanoseconds, formatter, null);
   }

   @Override
   protected JSR310FormattedSerializerBase<LocalTime> withFormat(Boolean useTimestamp, DateTimeFormatter dtf, JsonFormat.Shape shape) {
      return new LocalTimeSerializer(this, useTimestamp, dtf);
   }

   protected DateTimeFormatter _defaultFormatter() {
      return DateTimeFormatter.ISO_LOCAL_TIME;
   }

   public void serialize(LocalTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
      if (this.useTimestamp(provider)) {
         g.writeStartArray();
         this._serializeAsArrayContents(value, g, provider);
         g.writeEndArray();
      } else {
         DateTimeFormatter dtf = this._formatter;
         if (dtf == null) {
            dtf = this._defaultFormatter();
         }

         g.writeString(value.format(dtf));
      }

   }

   public void serializeWithType(LocalTime value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, this.serializationShape(provider)));
      if (typeIdDef.valueShape == JsonToken.START_ARRAY) {
         this._serializeAsArrayContents(value, g, provider);
      } else {
         DateTimeFormatter dtf = this._formatter;
         if (dtf == null) {
            dtf = this._defaultFormatter();
         }

         g.writeString(value.format(dtf));
      }

      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   private final void _serializeAsArrayContents(LocalTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
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

   }

   @Override
   protected JsonToken serializationShape(SerializerProvider provider) {
      return this.useTimestamp(provider) ? JsonToken.START_ARRAY : JsonToken.VALUE_STRING;
   }

   @Override
   protected JSR310FormattedSerializerBase<?> withFeatures(Boolean writeZoneId, Boolean writeNanoseconds) {
      return new LocalTimeSerializer(this, this._useTimestamp, writeNanoseconds, this._formatter);
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      if (this.useTimestamp(visitor.getProvider())) {
         this._acceptTimestampVisitor(visitor, typeHint);
      } else {
         JsonStringFormatVisitor v2 = visitor.expectStringFormat(typeHint);
         if (v2 != null) {
            v2.format(JsonValueFormat.TIME);
         }
      }

   }
}
