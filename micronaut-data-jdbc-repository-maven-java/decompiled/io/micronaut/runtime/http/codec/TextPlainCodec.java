package io.micronaut.runtime.http.codec;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
@BootstrapContextCompatible
public class TextPlainCodec implements MediaTypeCodec {
   public static final String CONFIGURATION_QUALIFIER = "text";
   private final Charset defaultCharset;
   private final List<MediaType> additionalTypes;

   @Inject
   public TextPlainCodec(
      @Value("${micronaut.application.default-charset}") Optional<Charset> defaultCharset, @Named("text") @Nullable CodecConfiguration codecConfiguration
   ) {
      this.defaultCharset = (Charset)defaultCharset.orElse(StandardCharsets.UTF_8);
      if (codecConfiguration != null) {
         this.additionalTypes = codecConfiguration.getAdditionalTypes();
      } else {
         this.additionalTypes = Collections.emptyList();
      }

   }

   public TextPlainCodec(Charset defaultCharset) {
      this.defaultCharset = defaultCharset != null ? defaultCharset : StandardCharsets.UTF_8;
      this.additionalTypes = Collections.emptyList();
   }

   @Override
   public Collection<MediaType> getMediaTypes() {
      List<MediaType> mediaTypes = new ArrayList(this.additionalTypes.size() + 1);
      mediaTypes.add(MediaType.TEXT_PLAIN_TYPE);
      mediaTypes.addAll(this.additionalTypes);
      return mediaTypes;
   }

   @Override
   public <T> T decode(Argument<T> type, ByteBuffer<?> buffer) throws CodecException {
      String text = buffer.toString(this.defaultCharset);
      return (T)(CharSequence.class.isAssignableFrom(type.getType())
         ? text
         : ConversionService.SHARED
            .convert(text, type)
            .orElseThrow(() -> new CodecException("Cannot decode byte buffer with value [" + text + "] to type: " + type)));
   }

   @Override
   public <T> T decode(Argument<T> type, byte[] bytes) throws CodecException {
      String text = new String(bytes, this.defaultCharset);
      return (T)(CharSequence.class.isAssignableFrom(type.getType())
         ? text
         : ConversionService.SHARED.convert(text, type).orElseThrow(() -> new CodecException("Cannot decode bytes with value [" + text + "] to type: " + type)));
   }

   @Override
   public <T> T decode(Argument<T> type, InputStream inputStream) throws CodecException {
      if (CharSequence.class.isAssignableFrom(type.getType())) {
         try {
            return (T)IOUtils.readText(new BufferedReader(new InputStreamReader(inputStream, this.defaultCharset)));
         } catch (IOException var4) {
            throw new CodecException("Error decoding string from stream: " + var4.getMessage());
         }
      } else {
         throw new UnsupportedOperationException("codec only supports decoding objects to string");
      }
   }

   @Override
   public <T> void encode(T object, OutputStream outputStream) throws CodecException {
      byte[] bytes = this.encode(object);

      try {
         outputStream.write(bytes);
      } catch (IOException var5) {
         throw new CodecException("Error writing encoding bytes to stream: " + var5.getMessage(), var5);
      }
   }

   @Override
   public <T> byte[] encode(T object) throws CodecException {
      return object.toString().getBytes(this.defaultCharset);
   }

   @Override
   public <T, B> ByteBuffer<B> encode(T object, ByteBufferFactory<?, B> allocator) throws CodecException {
      byte[] bytes = this.encode(object);
      return allocator.wrap(bytes);
   }
}
