package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;

public class DurationKeyDeserializer extends Jsr310KeyDeserializer {
   public static final DurationKeyDeserializer INSTANCE = new DurationKeyDeserializer();

   private DurationKeyDeserializer() {
   }

   protected Duration deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return Duration.parse(key);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, Duration.class, var4, key);
      }
   }
}
