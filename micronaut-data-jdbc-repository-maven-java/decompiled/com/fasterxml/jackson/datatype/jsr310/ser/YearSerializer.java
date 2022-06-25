package com.fasterxml.jackson.datatype.jsr310.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import java.io.IOException;
import java.time.Year;
import java.time.format.DateTimeFormatter;

public class YearSerializer extends JSR310FormattedSerializerBase<Year> {
   private static final long serialVersionUID = 1L;
   public static final YearSerializer INSTANCE = new YearSerializer();

   protected YearSerializer() {
      this(null);
   }

   public YearSerializer(DateTimeFormatter formatter) {
      super(Year.class, formatter);
   }

   protected YearSerializer(YearSerializer base, Boolean useTimestamp, DateTimeFormatter formatter) {
      super(base, useTimestamp, formatter, null);
   }

   protected YearSerializer withFormat(Boolean useTimestamp, DateTimeFormatter formatter, JsonFormat.Shape shape) {
      return new YearSerializer(this, useTimestamp, formatter);
   }

   public void serialize(Year year, JsonGenerator generator, SerializerProvider provider) throws IOException {
      if (this.useTimestamp(provider)) {
         generator.writeNumber(year.getValue());
      } else {
         String str = this._formatter == null ? year.toString() : year.format(this._formatter);
         generator.writeString(str);
      }

   }

   @Override
   protected void _acceptTimestampVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      JsonIntegerFormatVisitor v2 = visitor.expectIntegerFormat(typeHint);
      if (v2 != null) {
         v2.numberType(JsonParser.NumberType.LONG);
      }

   }

   @Override
   protected JsonToken serializationShape(SerializerProvider provider) {
      return this.useTimestamp(provider) ? JsonToken.VALUE_NUMBER_INT : JsonToken.VALUE_STRING;
   }
}
