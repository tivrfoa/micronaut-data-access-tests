package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Period;

public class PeriodKeyDeserializer extends Jsr310KeyDeserializer {
   public static final PeriodKeyDeserializer INSTANCE = new PeriodKeyDeserializer();

   private PeriodKeyDeserializer() {
   }

   protected Period deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return Period.parse(key);
      } catch (DateTimeException var4) {
         return this._handleDateTimeException(ctxt, Period.class, var4, key);
      }
   }
}
