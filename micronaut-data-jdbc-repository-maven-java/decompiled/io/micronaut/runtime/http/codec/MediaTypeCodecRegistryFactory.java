package io.micronaut.runtime.http.codec;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import jakarta.inject.Singleton;
import java.util.List;

@Factory
@BootstrapContextCompatible
public class MediaTypeCodecRegistryFactory {
   @Singleton
   @Primary
   @BootstrapContextCompatible
   MediaTypeCodecRegistry mediaTypeCodecRegistry(List<MediaTypeCodec> codecs) {
      return MediaTypeCodecRegistry.of(codecs);
   }
}
