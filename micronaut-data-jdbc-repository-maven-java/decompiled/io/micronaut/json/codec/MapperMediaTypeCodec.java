package io.micronaut.json.codec;

import io.micronaut.context.BeanProvider;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.json.JsonFeatures;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.runtime.ApplicationConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class MapperMediaTypeCodec implements MediaTypeCodec {
   public static final String REGULAR_JSON_MEDIA_TYPE_CODEC_NAME = "json";
   protected final ApplicationConfiguration applicationConfiguration;
   protected final List<MediaType> additionalTypes;
   protected final CodecConfiguration codecConfiguration;
   protected final MediaType mediaType;
   private final BeanProvider<JsonMapper> mapperProvider;
   private volatile JsonMapper mapper;

   public MapperMediaTypeCodec(
      BeanProvider<JsonMapper> mapperProvider, ApplicationConfiguration applicationConfiguration, CodecConfiguration codecConfiguration, MediaType mediaType
   ) {
      this.mapperProvider = mapperProvider;
      this.applicationConfiguration = applicationConfiguration;
      this.codecConfiguration = codecConfiguration;
      this.mediaType = mediaType;
      if (codecConfiguration != null) {
         this.additionalTypes = codecConfiguration.getAdditionalTypes();
      } else {
         this.additionalTypes = Collections.emptyList();
      }

   }

   public MapperMediaTypeCodec(JsonMapper mapper, ApplicationConfiguration applicationConfiguration, CodecConfiguration codecConfiguration, MediaType mediaType) {
      this(() -> mapper, applicationConfiguration, codecConfiguration, mediaType);
      ArgumentUtils.requireNonNull("objectMapper", mapper);
      this.mapper = mapper;
   }

   public JsonMapper getJsonMapper() {
      JsonMapper mapper = this.mapper;
      if (mapper == null) {
         synchronized(this) {
            mapper = this.mapper;
            if (mapper == null) {
               mapper = this.mapperProvider.get();
               this.mapper = mapper;
            }
         }
      }

      return mapper;
   }

   public MapperMediaTypeCodec cloneWithFeatures(JsonFeatures features) {
      return this.cloneWithMapper(this.getJsonMapper().cloneWithFeatures(features));
   }

   public final MapperMediaTypeCodec cloneWithViewClass(Class<?> viewClass) {
      return this.cloneWithMapper(this.getJsonMapper().cloneWithViewClass(viewClass));
   }

   protected abstract MapperMediaTypeCodec cloneWithMapper(JsonMapper mapper);

   @Override
   public Collection<MediaType> getMediaTypes() {
      List<MediaType> mediaTypes = new ArrayList();
      mediaTypes.add(this.mediaType);
      mediaTypes.addAll(this.additionalTypes);
      return mediaTypes;
   }

   @Override
   public boolean supportsType(Class<?> type) {
      return !CharSequence.class.isAssignableFrom(type);
   }

   @Override
   public <T> T decode(Argument<T> type, InputStream inputStream) throws CodecException {
      try {
         return this.getJsonMapper().readValue(inputStream, type);
      } catch (IOException var4) {
         throw new CodecException("Error decoding JSON stream for type [" + type.getName() + "]: " + var4.getMessage(), var4);
      }
   }

   public <T> T decode(Argument<T> type, JsonNode node) throws CodecException {
      try {
         JsonMapper om = this.getJsonMapper();
         return om.readValueFromTree(node, type);
      } catch (IOException var4) {
         throw new CodecException("Error decoding JSON stream for type [" + type.getName() + "]: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> T decode(Argument<T> type, ByteBuffer<?> buffer) throws CodecException {
      try {
         return (T)(CharSequence.class.isAssignableFrom(type.getType())
            ? buffer.toString(this.applicationConfiguration.getDefaultCharset())
            : this.getJsonMapper().readValue(buffer.toByteArray(), type));
      } catch (IOException var4) {
         throw new CodecException("Error decoding stream for type [" + type.getType() + "]: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> T decode(Argument<T> type, byte[] bytes) throws CodecException {
      try {
         return (T)(CharSequence.class.isAssignableFrom(type.getType())
            ? new String(bytes, this.applicationConfiguration.getDefaultCharset())
            : this.getJsonMapper().readValue(bytes, type));
      } catch (IOException var4) {
         throw new CodecException("Error decoding stream for type [" + type.getType() + "]: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> T decode(Argument<T> type, String data) throws CodecException {
      try {
         return this.getJsonMapper().readValue(data, type);
      } catch (IOException var4) {
         throw new CodecException("Error decoding JSON stream for type [" + type.getName() + "]: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> void encode(T object, OutputStream outputStream) throws CodecException {
      try {
         this.getJsonMapper().writeValue(outputStream, object);
      } catch (IOException var4) {
         throw new CodecException("Error encoding object [" + object + "] to JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> void encode(Argument<T> type, T object, OutputStream outputStream) throws CodecException {
      try {
         this.getJsonMapper().writeValue(outputStream, type, object);
      } catch (IOException var5) {
         throw new CodecException("Error encoding object [" + object + "] to JSON: " + var5.getMessage(), var5);
      }
   }

   @Override
   public <T> byte[] encode(T object) throws CodecException {
      try {
         return object instanceof byte[] ? (byte[])object : this.getJsonMapper().writeValueAsBytes(object);
      } catch (IOException var3) {
         throw new CodecException("Error encoding object [" + object + "] to JSON: " + var3.getMessage(), var3);
      }
   }

   @Override
   public <T> byte[] encode(Argument<T> type, T object) throws CodecException {
      try {
         return object instanceof byte[] ? (byte[])object : this.getJsonMapper().writeValueAsBytes(type, object);
      } catch (IOException var4) {
         throw new CodecException("Error encoding object [" + object + "] to JSON: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T, B> ByteBuffer<B> encode(T object, ByteBufferFactory<?, B> allocator) throws CodecException {
      if (object instanceof byte[]) {
         return allocator.copiedBuffer((byte[])object);
      } else {
         ByteBuffer<B> buffer = allocator.buffer();
         OutputStream outputStream = buffer.toOutputStream();
         this.encode(object, outputStream);
         return buffer;
      }
   }

   @Override
   public <T, B> ByteBuffer<B> encode(Argument<T> type, T object, ByteBufferFactory<?, B> allocator) throws CodecException {
      if (object instanceof byte[]) {
         return allocator.copiedBuffer((byte[])object);
      } else {
         ByteBuffer<B> buffer = allocator.buffer();
         OutputStream outputStream = buffer.toOutputStream();
         this.encode(type, object, outputStream);
         return buffer;
      }
   }
}
