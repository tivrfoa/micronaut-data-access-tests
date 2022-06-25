package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeKeyDeserializer extends Jsr310KeyDeserializer {
   public static final OffsetDateTimeKeyDeserializer INSTANCE = new OffsetDateTimeKeyDeserializer();

   private OffsetDateTimeKeyDeserializer() {
   }

   protected OffsetDateTime deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return OffsetDateTime.parse(key, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, OffsetDateTime.class, var4, key);
      }
   }
}
