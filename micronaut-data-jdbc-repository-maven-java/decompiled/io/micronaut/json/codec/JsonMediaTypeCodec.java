package io.micronaut.json.codec;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.json.JsonMapper;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("json")
@Singleton
@BootstrapContextCompatible
public class JsonMediaTypeCodec extends MapperMediaTypeCodec {
   public static final String CONFIGURATION_QUALIFIER = "json";

   public JsonMediaTypeCodec(
      JsonMapper jsonMapper, ApplicationConfiguration applicationConfiguration, @Named("json") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(jsonMapper, applicationConfiguration, codecConfiguration, MediaType.APPLICATION_JSON_TYPE);
   }

   @Inject
   public JsonMediaTypeCodec(
      BeanProvider<JsonMapper> jsonCodec, ApplicationConfiguration applicationConfiguration, @Named("json") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(jsonCodec, applicationConfiguration, codecConfiguration, MediaType.APPLICATION_JSON_TYPE);
   }

   @Override
   protected MapperMediaTypeCodec cloneWithMapper(JsonMapper mapper) {
      return new JsonMediaTypeCodec(mapper, this.applicationConfiguration, this.codecConfiguration);
   }
}
