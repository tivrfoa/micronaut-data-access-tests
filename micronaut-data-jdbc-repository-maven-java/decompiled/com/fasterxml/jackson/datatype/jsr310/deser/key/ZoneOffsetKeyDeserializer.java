package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneOffset;

public class ZoneOffsetKeyDeserializer extends Jsr310KeyDeserializer {
   public static final ZoneOffsetKeyDeserializer INSTANCE = new ZoneOffsetKeyDeserializer();

   private ZoneOffsetKeyDeserializer() {
   }

   protected ZoneOffset deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return ZoneOffset.of(key);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, ZoneOffset.class, var4, key);
      }
   }
}
