package io.micronaut.http.codec;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultMediaTypeCodecRegistry implements MediaTypeCodecRegistry {
   Map<String, Optional<MediaTypeCodec>> decodersByExtension = new LinkedHashMap(3);
   Map<MediaType, Optional<MediaTypeCodec>> decodersByType = new LinkedHashMap(3);
   private final Collection<MediaTypeCodec> codecs;

   DefaultMediaTypeCodecRegistry(MediaTypeCodec... codecs) {
      this(Arrays.asList(codecs));
   }

   DefaultMediaTypeCodecRegistry(Collection<MediaTypeCodec> codecs) {
      if (codecs != null) {
         this.codecs = Collections.unmodifiableCollection(codecs);

         for(MediaTypeCodec decoder : codecs) {
            for(MediaType mediaType : decoder.getMediaTypes()) {
               if (mediaType != null) {
                  this.decodersByExtension.put(mediaType.getExtension(), Optional.of(decoder));
                  this.decodersByType.put(mediaType, Optional.of(decoder));
               }
            }
         }
      } else {
         this.codecs = Collections.emptyList();
      }

   }

   @Override
   public Optional<MediaTypeCodec> findCodec(@Nullable MediaType mediaType) {
      if (mediaType == null) {
         return Optional.empty();
      } else {
         Optional<MediaTypeCodec> decoder = (Optional)this.decodersByType.get(mediaType);
         if (decoder == null) {
            decoder = (Optional)this.decodersByExtension.get(mediaType.getExtension());
         }

         return decoder == null ? Optional.empty() : decoder;
      }
   }

   @Override
   public Optional<MediaTypeCodec> findCodec(@Nullable MediaType mediaType, Class<?> type) {
      Optional<MediaTypeCodec> codec = this.findCodec(mediaType);
      if (codec.isPresent()) {
         MediaTypeCodec mediaTypeCodec = (MediaTypeCodec)codec.get();
         return mediaTypeCodec.supportsType(type) ? codec : Optional.empty();
      } else {
         return codec;
      }
   }

   @Override
   public Collection<MediaTypeCodec> getCodecs() {
      return this.codecs;
   }
}
