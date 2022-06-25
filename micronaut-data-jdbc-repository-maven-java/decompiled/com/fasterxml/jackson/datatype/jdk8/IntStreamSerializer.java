package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.stream.IntStream;

public class IntStreamSerializer extends StdSerializer<IntStream> {
   private static final long serialVersionUID = 1L;
   public static final IntStreamSerializer INSTANCE = new IntStreamSerializer();

   private IntStreamSerializer() {
      super(IntStream.class);
   }

   public void serialize(IntStream stream, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      try {
         IntStream is = stream;
         Throwable var5 = null;

         try {
            jgen.writeStartArray();
            is.forEachOrdered(value -> {
               try {
                  jgen.writeNumber(value);
               } catch (IOException var3x) {
                  throw new WrappedIOException(var3x);
               }
            });
            jgen.writeEndArray();
         } catch (Throwable var15) {
            var5 = var15;
            throw var15;
         } finally {
            if (stream != null) {
               if (var5 != null) {
                  try {
                     is.close();
                  } catch (Throwable var14) {
                     var5.addSuppressed(var14);
                  }
               } else {
                  stream.close();
               }
            }

         }

      } catch (WrappedIOException var17) {
         throw var17.getCause();
      }
   }
}
