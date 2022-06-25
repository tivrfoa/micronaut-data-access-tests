package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.stream.DoubleStream;

public class DoubleStreamSerializer extends StdSerializer<DoubleStream> {
   private static final long serialVersionUID = 1L;
   public static final DoubleStreamSerializer INSTANCE = new DoubleStreamSerializer();

   private DoubleStreamSerializer() {
      super(DoubleStream.class);
   }

   public void serialize(DoubleStream stream, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      try {
         DoubleStream ds = stream;
         Throwable var5 = null;

         try {
            jgen.writeStartArray();
            ds.forEachOrdered(value -> {
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
                     ds.close();
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
