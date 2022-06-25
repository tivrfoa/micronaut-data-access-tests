package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

public class MonthDaySerializer extends JSR310FormattedSerializerBase<MonthDay> {
   private static final long serialVersionUID = 1L;
   public static final MonthDaySerializer INSTANCE = new MonthDaySerializer();

   protected MonthDaySerializer() {
      this(null);
   }

   public MonthDaySerializer(DateTimeFormatter formatter) {
      super(MonthDay.class, formatter);
   }

   private MonthDaySerializer(MonthDaySerializer base, Boolean useTimestamp, DateTimeFormatter formatter) {
      super(base, useTimestamp, formatter, null);
   }

   protected MonthDaySerializer withFormat(Boolean useTimestamp, DateTimeFormatter formatter, JsonFormat.Shape shape) {
      return new MonthDaySerializer(this, useTimestamp, formatter);
   }

   public void serialize(MonthDay value, JsonGenerator g, SerializerProvider provider) throws IOException {
      if (this._useTimestampExplicitOnly(provider)) {
         g.writeStartArray();
         this._serializeAsArrayContents(value, g, provider);
         g.writeEndArray();
      } else {
         g.writeString(this._formatter == null ? value.toString() : value.format(this._formatter));
      }

   }

   public void serializeWithType(MonthDay value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
      WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, this.serializationShape(provider)));
      if (typeIdDef.valueShape == JsonToken.START_ARRAY) {
         this._serializeAsArrayContents(value, g, provider);
      } else {
         g.writeString(this._formatter == null ? value.toString() : value.format(this._formatter));
      }

      typeSer.writeTypeSuffix(g, typeIdDef);
   }

   protected void _serializeAsArrayContents(MonthDay value, JsonGenerator g, SerializerProvider provider) throws IOException {
      g.writeNumber(value.getMonthValue());
      g.writeNumber(value.getDayOfMonth());
   }

   @Override
   protected JsonToken serializationShape(SerializerProvider provider) {
      return this._useTimestampExplicitOnly(provider) ? JsonToken.START_ARRAY : JsonToken.VALUE_STRING;
   }
}
