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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Singleton
@BootstrapContextCompatible
public class JsonStreamMediaTypeCodec extends JsonMediaTypeCodec {
   public static final String CONFIGURATION_QUALIFIER = "json-stream";
   private final List<MediaType> streamAdditionalTypes;

   public JsonStreamMediaTypeCodec(
      JsonMapper jsonMapper, ApplicationConfiguration applicationConfiguration, @Named("json-stream") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(jsonMapper, applicationConfiguration, null);
      if (codecConfiguration != null) {
         this.streamAdditionalTypes = codecConfiguration.getAdditionalTypes();
      } else {
         this.streamAdditionalTypes = Collections.emptyList();
      }

   }

   @Inject
   public JsonStreamMediaTypeCodec(
      BeanProvider<JsonMapper> jsonCodec,
      ApplicationConfiguration applicationConfiguration,
      @Named("json-stream") @Nullable CodecConfiguration codecConfiguration
   ) {
      super(jsonCodec, applicationConfiguration, null);
      if (codecConfiguration != null) {
         this.streamAdditionalTypes = codecConfiguration.getAdditionalTypes();
      } else {
         this.streamAdditionalTypes = Collections.emptyList();
      }

   }

   @Override
   public Collection<MediaType> getMediaTypes() {
      List<MediaType> mediaTypes = new ArrayList();
      mediaTypes.add(MediaType.APPLICATION_JSON_STREAM_TYPE);
      mediaTypes.addAll(this.streamAdditionalTypes);
      return mediaTypes;
   }

   @Override
   protected MapperMediaTypeCodec cloneWithMapper(JsonMapper mapper) {
      return new JsonStreamMediaTypeCodec(mapper, this.applicationConfiguration, this.codecConfiguration);
   }
}
