package io.micronaut.http.codec;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import java.util.Collection;
import java.util.Optional;

public interface MediaTypeCodecRegistry {
   Optional<MediaTypeCodec> findCodec(@Nullable MediaType mediaType);

   Optional<MediaTypeCodec> findCodec(@Nullable MediaType mediaType, Class<?> type);

   Collection<MediaTypeCodec> getCodecs();

   static MediaTypeCodecRegistry of(MediaTypeCodec... codecs) {
      return new DefaultMediaTypeCodecRegistry(codecs);
   }

   static MediaTypeCodecRegistry of(Collection<MediaTypeCodec> codecs) {
      return new DefaultMediaTypeCodecRegistry(codecs);
   }
}
