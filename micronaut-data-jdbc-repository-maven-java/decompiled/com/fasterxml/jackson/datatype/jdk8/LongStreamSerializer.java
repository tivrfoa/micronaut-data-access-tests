package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.stream.LongStream;

public class LongStreamSerializer extends StdSerializer<LongStream> {
   private static final long serialVersionUID = 1L;
   public static final LongStreamSerializer INSTANCE = new LongStreamSerializer();

   private LongStreamSerializer() {
      super(LongStream.class);
   }

   public void serialize(LongStream stream, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      try {
         LongStream ls = stream;
         Throwable var5 = null;

         try {
            jgen.writeStartArray();
            ls.forEachOrdered(value -> {
               try {
                  jgen.writeNumber(value);
               } catch (IOException var4x) {
                  throw new WrappedIOException(var4x);
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
                     ls.close();
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
