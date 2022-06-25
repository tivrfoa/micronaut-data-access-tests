package io.micronaut.http.server.codec;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecConfiguration;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.sse.Event;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Singleton
@Internal
@BootstrapContextCompatible
public class TextStreamCodec implements MediaTypeCodec {
   public static final String CONFIGURATION_QUALIFIER = "text-stream";
   private static final byte[] DATA_PREFIX = "data: ".getBytes(StandardCharsets.UTF_8);
   private static final byte[] EVENT_PREFIX = "event: ".getBytes(StandardCharsets.UTF_8);
   private static final byte[] ID_PREFIX = "id: ".getBytes(StandardCharsets.UTF_8);
   private static final byte[] RETRY_PREFIX = "retry: ".getBytes(StandardCharsets.UTF_8);
   private static final byte[] COMMENT_PREFIX = ": ".getBytes(StandardCharsets.UTF_8);
   private static final byte[] NEWLINE = "\n".getBytes(StandardCharsets.UTF_8);
   private final BeanProvider<MediaTypeCodecRegistry> codecRegistryProvider;
   private final ByteBufferFactory byteBufferFactory;
   private final List<MediaType> additionalTypes;
   private final Charset defaultCharset;
   private MediaTypeCodecRegistry codecRegistry;

   @Inject
   public TextStreamCodec(
      ApplicationConfiguration applicationConfiguration,
      ByteBufferFactory byteBufferFactory,
      BeanProvider<MediaTypeCodecRegistry> codecRegistryProvider,
      @Named("text-stream") @Nullable CodecConfiguration codecConfiguration
   ) {
      this(applicationConfiguration.getDefaultCharset(), byteBufferFactory, codecRegistryProvider, codecConfiguration);
   }

   protected TextStreamCodec(
      Charset defaultCharset,
      ByteBufferFactory byteBufferFactory,
      BeanProvider<MediaTypeCodecRegistry> codecRegistryProvider,
      @Named("text-stream") @Nullable CodecConfiguration codecConfiguration
   ) {
      this.defaultCharset = defaultCharset;
      this.byteBufferFactory = byteBufferFactory;
      this.codecRegistryProvider = codecRegistryProvider;
      if (codecConfiguration != null) {
         this.additionalTypes = codecConfiguration.getAdditionalTypes();
      } else {
         this.additionalTypes = Collections.emptyList();
      }

   }

   @Override
   public Collection<MediaType> getMediaTypes() {
      List<MediaType> mediaTypes = new ArrayList();
      mediaTypes.add(MediaType.TEXT_EVENT_STREAM_TYPE);
      mediaTypes.addAll(this.additionalTypes);
      return mediaTypes;
   }

   @Override
   public <T> T decode(Argument<T> type, InputStream inputStream) {
      throw new UnsupportedOperationException("This codec currently only supports encoding");
   }

   @Override
   public <T> T decode(Class<T> type, InputStream inputStream) {
      throw new UnsupportedOperationException("This codec currently only supports encoding");
   }

   @Override
   public <T> void encode(T object, OutputStream outputStream) {
      try {
         outputStream.write(this.encode(object));
      } catch (IOException var4) {
         throw new CodecException("I/O error occurred encoding object to output stream: " + var4.getMessage(), var4);
      }
   }

   @Override
   public <T> byte[] encode(T object) {
      ByteBuffer buffer = this.encode(object, this.byteBufferFactory);
      return buffer.toByteArray();
   }

   @Override
   public <T, B> ByteBuffer<B> encode(T object, ByteBufferFactory<?, B> allocator) {
      Event<Object> event;
      if (object instanceof Event) {
         event = (Event)object;
      } else {
         event = Event.of(object);
      }

      Object data = event.getData();
      byte[] body;
      if (data instanceof CharSequence) {
         body = data.toString().getBytes(this.defaultCharset);
      } else {
         MediaTypeCodec jsonCodec = (MediaTypeCodec)this.resolveMediaTypeCodecRegistry()
            .findCodec(MediaType.APPLICATION_JSON_TYPE)
            .orElseThrow(() -> new CodecException("No possible JSON encoders found!"));
         body = jsonCodec.encode(data);
      }

      ByteBuffer eventData = allocator.buffer(body.length + 10);
      this.writeAttribute(eventData, COMMENT_PREFIX, event.getComment());
      this.writeAttribute(eventData, ID_PREFIX, event.getId());
      this.writeAttribute(eventData, EVENT_PREFIX, event.getName());
      Duration retry = event.getRetry();
      if (retry != null) {
         this.writeAttribute(eventData, RETRY_PREFIX, String.valueOf(retry.toMillis()));
      }

      int end;
      for(int start = 0; start < body.length; start = end + 1) {
         end = indexOf(body, (byte)10, start);
         if (end == -1) {
            end = body.length - 1;
         }

         eventData.write(DATA_PREFIX).write(body, start, end - start + 1);
      }

      eventData.write(NEWLINE).write(NEWLINE);
      return eventData;
   }

   private static int indexOf(byte[] haystack, byte needle, int start) {
      for(int i = start; i < haystack.length; ++i) {
         if (haystack[i] == needle) {
            return i;
         }
      }

      return -1;
   }

   private MediaTypeCodecRegistry resolveMediaTypeCodecRegistry() {
      if (this.codecRegistry == null) {
         this.codecRegistry = this.codecRegistryProvider.get();
      }

      return this.codecRegistry;
   }

   protected void writeAttribute(ByteBuffer eventData, byte[] attribute, String value) {
      if (value != null) {
         eventData.write(attribute).write(value, this.defaultCharset).write(NEWLINE);
      }

   }
}
