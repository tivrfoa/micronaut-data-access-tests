package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class OffsetTimeKeyDeserializer extends Jsr310KeyDeserializer {
   public static final OffsetTimeKeyDeserializer INSTANCE = new OffsetTimeKeyDeserializer();

   private OffsetTimeKeyDeserializer() {
   }

   protected OffsetTime deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return OffsetTime.parse(key, DateTimeFormatter.ISO_OFFSET_TIME);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, OffsetTime.class, var4, key);
      }
   }
}
