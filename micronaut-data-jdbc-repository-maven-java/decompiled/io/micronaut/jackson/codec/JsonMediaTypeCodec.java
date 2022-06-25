package io.micronaut.jackson.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("json")
@Singleton
@Secondary
@BootstrapContextCompatible
@Bean(
   typed = {JsonMediaTypeCodec.class, JacksonMediaTypeCodec.class}
)
public class JsonMediaTypeCodec extends JacksonMediaTypeCodec {
   public static final String CONFIGURATION_QUALIFIER = "json";

   public JsonMediaTypeCodec(
      ObjectMapper objectMapper, ApplicationConfiguration applicationConfiguration, @Named("json") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(objectMapper, applicationConfiguration, codecConfiguration, MediaType.APPLICATION_JSON_TYPE);
   }

   @Inject
   public JsonMediaTypeCodec(
      BeanProvider<ObjectMapper> objectMapper,
      ApplicationConfiguration applicationConfiguration,
      @Named("json") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(objectMapper, applicationConfiguration, codecConfiguration, MediaType.APPLICATION_JSON_TYPE);
   }

   @Override
   public JacksonMediaTypeCodec cloneWithFeatures(JacksonFeatures jacksonFeatures) {
      ObjectMapper objectMapper = this.getObjectMapper().copy();
      jacksonFeatures.getDeserializationFeatures().forEach(objectMapper::configure);
      jacksonFeatures.getSerializationFeatures().forEach(objectMapper::configure);
      return new JsonMediaTypeCodec(objectMapper, this.applicationConfiguration, this.codecConfiguration);
   }
}
