package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.stream.Stream;

public class StreamSerializer extends StdSerializer<Stream<?>> implements ContextualSerializer {
   private static final long serialVersionUID = 1L;
   private final JavaType elemType;
   private final transient JsonSerializer<Object> elemSerializer;

   public StreamSerializer(JavaType streamType, JavaType elemType) {
      this(streamType, elemType, null);
   }

   public StreamSerializer(JavaType streamType, JavaType elemType, JsonSerializer<Object> elemSerializer) {
      super(streamType);
      this.elemType = elemType;
      this.elemSerializer = elemSerializer;
   }

   @Override
   public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
      return this.elemType.hasRawClass(Object.class) || !provider.isEnabled(MapperFeature.USE_STATIC_TYPING) && !this.elemType.isFinal()
         ? this
         : new StreamSerializer(
            provider.getTypeFactory().constructParametricType(Stream.class, this.elemType),
            this.elemType,
            provider.findPrimaryPropertySerializer(this.elemType, property)
         );
   }

   public void serialize(Stream<?> stream, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      try {
         Stream<?> s = stream;
         Throwable var5 = null;

         try {
            jgen.writeStartArray();
            s.forEachOrdered(elem -> {
               try {
                  if (this.elemSerializer == null) {
                     provider.defaultSerializeValue(elem, jgen);
                  } else {
                     this.elemSerializer.serialize(elem, jgen, provider);
                  }

               } catch (IOException var5x) {
                  throw new WrappedIOException(var5x);
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
                     s.close();
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
