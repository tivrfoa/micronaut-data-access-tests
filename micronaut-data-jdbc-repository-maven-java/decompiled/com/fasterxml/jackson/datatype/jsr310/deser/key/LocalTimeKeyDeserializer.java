package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeKeyDeserializer extends Jsr310KeyDeserializer {
   public static final LocalTimeKeyDeserializer INSTANCE = new LocalTimeKeyDeserializer();

   private LocalTimeKeyDeserializer() {
   }

   protected LocalTime deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return LocalTime.parse(key, DateTimeFormatter.ISO_LOCAL_TIME);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, LocalTime.class, var4, key);
      }
   }
}
