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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Secondary
@Singleton
@BootstrapContextCompatible
@Bean(
   typed = {JsonStreamMediaTypeCodec.class, JacksonMediaTypeCodec.class}
)
public class JsonStreamMediaTypeCodec extends JsonMediaTypeCodec {
   public static final String CONFIGURATION_QUALIFIER = "json-stream";
   private final List<MediaType> additionalTypes;

   public JsonStreamMediaTypeCodec(
      ObjectMapper objectMapper, ApplicationConfiguration applicationConfiguration, @Named("json-stream") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(objectMapper, applicationConfiguration, null);
      if (codecConfiguration != null) {
         this.additionalTypes = codecConfiguration.getAdditionalTypes();
      } else {
         this.additionalTypes = Collections.emptyList();
      }

   }

   @Inject
   public JsonStreamMediaTypeCodec(
      BeanProvider<ObjectMapper> objectMapper,
      ApplicationConfiguration applicationConfiguration,
      @Named("json-stream") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(objectMapper, applicationConfiguration, null);
      if (codecConfiguration != null) {
         this.additionalTypes = codecConfiguration.getAdditionalTypes();
      } else {
         this.additionalTypes = Collections.emptyList();
      }

   }

   @Override
   public Collection<MediaType> getMediaTypes() {
      List<MediaType> mediaTypes = new ArrayList();
      mediaTypes.add(MediaType.APPLICATION_JSON_STREAM_TYPE);
      mediaTypes.addAll(this.additionalTypes);
      return mediaTypes;
   }
}
