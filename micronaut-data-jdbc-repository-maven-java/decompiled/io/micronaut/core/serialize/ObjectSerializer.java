package io.micronaut.core.serialize;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public interface ObjectSerializer {
   ObjectSerializer JDK = new JdkSerializer();

   void serialize(@Nullable Object object, OutputStream outputStream) throws SerializationException;

   <T> Optional<T> deserialize(@Nullable InputStream inputStream, Class<T> requiredType) throws SerializationException;

   default <T> Optional<T> deserialize(@Nullable InputStream inputStream, Argument<T> requiredType) throws SerializationException {
      return this.deserialize(inputStream, requiredType.getType());
   }

   default Optional<byte[]> serialize(@Nullable Object object) throws SerializationException {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      this.serialize(object, outputStream);
      return Optional.of(outputStream.toByteArray());
   }

   default <T> Optional<T> deserialize(@Nullable byte[] bytes, Class<T> requiredType) throws SerializationException {
      if (bytes == null) {
         return Optional.empty();
      } else {
         try {
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            Throwable var4 = null;

            Optional var5;
            try {
               var5 = this.deserialize((InputStream)input, requiredType);
            } catch (Throwable var15) {
               var4 = var15;
               throw var15;
            } finally {
               if (input != null) {
                  if (var4 != null) {
                     try {
                        input.close();
                     } catch (Throwable var14) {
                        var4.addSuppressed(var14);
                     }
                  } else {
                     input.close();
                  }
               }

            }

            return var5;
         } catch (IOException var17) {
            throw new SerializationException("I/O error occurred during deserialization: " + var17.getMessage(), var17);
         }
      }
   }

   default <T> Optional<T> deserialize(@Nullable byte[] bytes, Argument<T> requiredType) throws SerializationException {
      return this.deserialize(bytes, requiredType.getType());
   }

   default Optional<Object> deserialize(@Nullable byte[] bytes) throws SerializationException {
      return bytes == null ? Optional.empty() : this.deserialize(bytes, Object.class);
   }
}
