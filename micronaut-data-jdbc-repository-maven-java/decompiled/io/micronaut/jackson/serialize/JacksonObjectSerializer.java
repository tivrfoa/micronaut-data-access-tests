package io.micronaut.jackson.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Bean;
import io.micronaut.core.serialize.ObjectSerializer;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

@Singleton
@Bean(
   typed = {JacksonObjectSerializer.class}
)
public class JacksonObjectSerializer implements ObjectSerializer {
   private final ObjectMapper objectMapper;

   public JacksonObjectSerializer(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
   }

   @Override
   public Optional<byte[]> serialize(Object object) throws SerializationException {
      try {
         return Optional.ofNullable(this.objectMapper.writeValueAsBytes(object));
      } catch (JsonProcessingException var3) {
         throw new SerializationException("Error serializing object to JSON: " + var3.getMessage(), var3);
      }
   }

   @Override
   public void serialize(Object object, OutputStream outputStream) throws SerializationException {
      try {
         this.objectMapper.writeValue(outputStream, object);
      } catch (IOException var4) {
         throw new SerializationException("Error serializing object to JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(byte[] bytes, Class<T> requiredType) throws SerializationException {
      try {
         return Optional.ofNullable(this.objectMapper.readValue(bytes, requiredType));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(InputStream inputStream, Class<T> requiredType) throws SerializationException {
      try {
         return Optional.ofNullable(this.objectMapper.readValue(inputStream, requiredType));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(byte[] bytes, Argument<T> requiredType) throws SerializationException {
      try {
         JavaType javaType = JacksonConfiguration.constructType(requiredType, this.objectMapper.getTypeFactory());
         return Optional.ofNullable(this.objectMapper.readValue(bytes, javaType));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> Optional<T> deserialize(InputStream inputStream, Argument<T> requiredType) throws SerializationException {
      try {
         JavaType javaType = JacksonConfiguration.constructType(requiredType, this.objectMapper.getTypeFactory());
         return Optional.ofNullable(this.objectMapper.readValue(inputStream, javaType));
      } catch (IOException var4) {
         throw new SerializationException("Error deserializing object from JSON: " + var4.getMessage(), var4);
      }
   }
}
