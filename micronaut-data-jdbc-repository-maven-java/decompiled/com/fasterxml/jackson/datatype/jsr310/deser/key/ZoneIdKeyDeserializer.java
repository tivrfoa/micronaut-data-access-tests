package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;

public class ZoneIdKeyDeserializer extends Jsr310KeyDeserializer {
   public static final ZoneIdKeyDeserializer INSTANCE = new ZoneIdKeyDeserializer();

   private ZoneIdKeyDeserializer() {
   }

   @Override
   protected Object deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return ZoneId.of(key);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, ZoneId.class, var4, key);
      }
   }
}
