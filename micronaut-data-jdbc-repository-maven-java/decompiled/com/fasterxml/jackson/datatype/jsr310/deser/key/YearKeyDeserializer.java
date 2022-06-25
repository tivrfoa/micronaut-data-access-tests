package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Year;

public class YearKeyDeserializer extends Jsr310KeyDeserializer {
   public static final YearKeyDeserializer INSTANCE = new YearKeyDeserializer();

   protected YearKeyDeserializer() {
   }

   protected Year deserialize(String key, DeserializationContext ctxt) throws IOException {
      try {
         return Year.of(Integer.parseInt(key));
      } catch (NumberFormatException var4) {
         return this._handleDateTimeException(ctxt, Year.class, new DateTimeException("Number format exception", var4), key);
      } catch (DateTimeException var5) {
         return this._handleDateTimeException(ctxt, Year.class, var5, key);
      }
   }
}
