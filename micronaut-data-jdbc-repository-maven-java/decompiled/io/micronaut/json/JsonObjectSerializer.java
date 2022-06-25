package io.micronaut.json;

import io.micronaut.core.serialize.ObjectSerializer;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

@Singleton
public class JsonObjectSerializer implements ObjectSerializer {
   private final JsonMapper jsonMapper;

   public JsonObjectSerializer(JsonMapper jsonMapper) {
      this.jsonMapper = jsonMapper;
   }

   @Override
   public Optional<byte[]> serialize(Object object) throws SerializationException {
      try {
         return Optional.ofNullable(this.jsonMapper.writeValueAsBytes(object));
      } catch (IOException var3) {
         throw new SerializationException("Error serializing object to JSON: " + var3.getMessage(), var3);
      }
   }

   @Override
   public void serialize(Object object, OutputStream outputStream) throws SerializationException {
      try {
         this.jsonMapper.writeValue(outputStream, object);
      } catch (IOException var4) {
         throw new SerializationException("Error serializing object to JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(byte[] bytes, Class<T> requiredType) throws SerializationException {
      try {
         return Optional.ofNullable(this.jsonMapper.readValue(bytes, Argument.of(requiredType)));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(InputStream inputStream, Class<T> requiredType) throws SerializationException {
      try {
         return Optional.ofNullable(this.jsonMapper.readValue(inputStream, Argument.of(requiredType)));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(byte[] bytes, Argument<T> requiredType) throws SerializationException {
      try {
         return Optional.ofNullable(this.jsonMapper.<T>readValue(bytes, requiredType));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(InputStream inputStream, Argument<T> requiredType) throws SerializationException {
      try {
         return Optional.ofNullable(this.jsonMapper.<T>readValue(inputStream, requiredType));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }
}
