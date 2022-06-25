package io.micronaut.http.codec;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public interface MediaTypeCodec {
   Collection<MediaType> getMediaTypes();

   <T> T decode(Argument<T> type, InputStream inputStream) throws CodecException;

   <T> void encode(T object, OutputStream outputStream) throws CodecException;

   <T> byte[] encode(T object) throws CodecException;

   <T, B> ByteBuffer<B> encode(T object, ByteBufferFactory<?, B> allocator) throws CodecException;

   default <T> void encode(@NonNull Argument<T> type, @NonNull T object, @NonNull OutputStream outputStream) throws CodecException {
      this.encode(object, outputStream);
   }

   @NonNull
   default <T> byte[] encode(@NonNull Argument<T> type, T object) throws CodecException {
      return this.encode(object);
   }

   @NonNull
   default <T, B> ByteBuffer<B> encode(@NonNull Argument<T> type, T object, @NonNull ByteBufferFactory<?, B> allocator) throws CodecException {
      return this.encode(object, allocator);
   }

   default <T> T decode(Class<T> type, InputStream inputStream) throws CodecException {
      return this.decode(Argument.of(type), inputStream);
   }

   default <T> T decode(Class<T> type, byte[] bytes) throws CodecException {
      return this.decode(type, (InputStream)(new ByteArrayInputStream(bytes)));
   }

   default <T> T decode(Argument<T> type, byte[] bytes) throws CodecException {
      return this.decode(type, (InputStream)(new ByteArrayInputStream(bytes)));
   }

   default <T> T decode(Class<T> type, ByteBuffer<?> buffer) throws CodecException {
      return this.decode(type, buffer.toInputStream());
   }

   default <T> T decode(Argument<T> type, ByteBuffer<?> buffer) throws CodecException {
      return this.decode(type, buffer.toInputStream());
   }

   default <T> T decode(Class<T> type, String data) throws CodecException {
      return this.decode(type, (InputStream)(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))));
   }

   default <T> T decode(Argument<T> type, String data) throws CodecException {
      return this.decode(type, (InputStream)(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))));
   }

   default boolean supportsType(Class<?> type) {
      return true;
   }
}
