package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeKeyDeserializer extends Jsr310KeyDeserializer {
   public static final ZonedDateTimeKeyDeserializer INSTANCE = new ZonedDateTimeKeyDeserializer();

   private ZonedDateTimeKeyDeserializer() {
   }

   protected ZonedDateTime deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return ZonedDateTime.parse(key, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, ZonedDateTime.class, var4, key);
      }
   }
}
