package io.micronaut.jackson.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.micronaut.context.BeanProvider;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.http.codec.CodecException;
import io.micronaut.jackson.JacksonConfiguration;
import io.micronaut.jackson.databind.JacksonDatabindMapper;
import io.micronaut.json.JsonFeatures;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.codec.MapperMediaTypeCodec;
import io.micronaut.runtime.ApplicationConfiguration;
import java.io.IOException;

public abstract class JacksonMediaTypeCodec extends MapperMediaTypeCodec {
   public static final String REGULAR_JSON_MEDIA_TYPE_CODEC_NAME = "json";

   public JacksonMediaTypeCodec(
      BeanProvider<ObjectMapper> objectMapperProvider,
      ApplicationConfiguration applicationConfiguration,
      CodecConfiguration codecConfiguration,
      MediaType mediaType
   ) {
      super(() -> new JacksonDatabindMapper(objectMapperProvider.get()), applicationConfiguration, codecConfiguration, mediaType);
   }

   public JacksonMediaTypeCodec(
      ObjectMapper objectMapper, ApplicationConfiguration applicationConfiguration, CodecConfiguration codecConfiguration, MediaType mediaType
   ) {
      super(new JacksonDatabindMapper(objectMapper), applicationConfiguration, codecConfiguration, mediaType);
   }

   public ObjectMapper getObjectMapper() {
      return ((JacksonDatabindMapper)this.getJsonMapper()).getObjectMapper();
   }

   @Override
   public MapperMediaTypeCodec cloneWithFeatures(JsonFeatures features) {
      return this.cloneWithFeatures((JacksonFeatures)features);
   }

   public abstract JacksonMediaTypeCodec cloneWithFeatures(JacksonFeatures jacksonFeatures);

   @Override
   protected MapperMediaTypeCodec cloneWithMapper(JsonMapper mapper) {
      throw new UnsupportedOperationException();
   }

   public <T> T decode(Argument<T> type, JsonNode node) throws CodecException {
      try {
         ObjectMapper objectMapper = this.getObjectMapper();
         if (type.hasTypeVariables()) {
            JsonParser jsonParser = objectMapper.treeAsTokens(node);
            return objectMapper.readValue(jsonParser, this.constructJavaType(type));
         } else {
            return objectMapper.treeToValue(node, type.getType());
         }
      } catch (IOException var5) {
         throw new CodecException("Error decoding JSON stream for type [" + type.getName() + "]: " + var5.getMessage(), var5);
      }
   }

   private <T> JavaType constructJavaType(Argument<T> type) {
      TypeFactory typeFactory = this.getObjectMapper().getTypeFactory();
      return JacksonConfiguration.constructType(type, typeFactory);
   }
}
