package com.fasterxml.jackson.datatype.jsr310.deser.key;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;
import java.time.DateTimeException;

abstract class Jsr310KeyDeserializer extends KeyDeserializer {
   @Override
   public final Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
      return "".equals(key) ? null : this.deserialize(key, ctxt);
   }

   protected abstract Object deserialize(String var1, DeserializationContext var2) throws IOException;

   protected <T> T _handleDateTimeException(DeserializationContext ctxt, Class<?> type, DateTimeException e0, String value) throws IOException {
      try {
         return (T)ctxt.handleWeirdKey(type, value, "Failed to deserialize %s: (%s) %s", type.getName(), e0.getClass().getName(), e0.getMessage());
      } catch (JsonMappingException var6) {
         var6.initCause(e0);
         throw var6;
      } catch (IOException var7) {
         if (null == var7.getCause()) {
            var7.initCause(e0);
         }

         throw JsonMappingException.fromUnexpectedIOE(var7);
      }
   }
}
