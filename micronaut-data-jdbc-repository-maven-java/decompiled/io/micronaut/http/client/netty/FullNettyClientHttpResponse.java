package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.subscriber.Completable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.NettyHttpHeaders;
import io.micronaut.http.netty.NettyHttpResponseBuilder;
import io.micronaut.http.netty.cookies.NettyCookies;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class FullNettyClientHttpResponse<B> implements HttpResponse<B>, Completable, NettyHttpResponseBuilder {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpClient.class);
   private final HttpStatus status;
   private final NettyHttpHeaders headers;
   private final NettyCookies nettyCookies;
   private final MutableConvertibleValues<Object> attributes;
   private final FullHttpResponse nettyHttpResponse;
   private final Map<Argument, Optional> convertedBodies = new HashMap();
   private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
   private final ByteBufferFactory<ByteBufAllocator, ByteBuf> byteBufferFactory;
   private final B body;
   private boolean complete;
   private byte[] bodyBytes;

   FullNettyClientHttpResponse(
      FullHttpResponse fullHttpResponse,
      HttpStatus httpStatus,
      MediaTypeCodecRegistry mediaTypeCodecRegistry,
      ByteBufferFactory<ByteBufAllocator, ByteBuf> byteBufferFactory,
      Argument<B> bodyType,
      boolean convertBody
   ) {
      this.status = httpStatus;
      this.headers = new NettyHttpHeaders(fullHttpResponse.headers(), ConversionService.SHARED);
      this.attributes = new MutableConvertibleValuesMap<>();
      this.nettyHttpResponse = fullHttpResponse;
      this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
      this.byteBufferFactory = byteBufferFactory;
      this.nettyCookies = new NettyCookies(fullHttpResponse.headers(), ConversionService.SHARED);
      Class<?> rawBodyType = bodyType != null ? bodyType.getType() : null;
      if (rawBodyType == null || HttpStatus.class.isAssignableFrom(rawBodyType)) {
         this.body = null;
      } else if (HttpResponse.class.isAssignableFrom(bodyType.getType())) {
         Optional<Argument<?>> responseBodyType = bodyType.getFirstTypeVariable();
         if (responseBodyType.isPresent()) {
            Argument<B> finalResponseBodyType = (Argument)responseBodyType.get();
            this.body = (B)(!convertBody && !this.isParseableBodyType(finalResponseBodyType.getType())
               ? null
               : this.getBody(finalResponseBodyType).orElse(null));
         } else {
            this.body = null;
         }
      } else {
         this.body = (B)(!convertBody && !this.isParseableBodyType(rawBodyType) ? null : this.getBody(bodyType).orElse(null));
      }

   }

   @Override
   public String reason() {
      return this.nettyHttpResponse.status().reasonPhrase();
   }

   @Override
   public HttpStatus getStatus() {
      return this.status;
   }

   @Override
   public HttpHeaders getHeaders() {
      return this.headers;
   }

   @Override
   public Cookies getCookies() {
      return this.nettyCookies;
   }

   @Override
   public Optional<Cookie> getCookie(String name) {
      return this.nettyCookies.findCookie(name);
   }

   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      return this.attributes;
   }

   @Override
   public Optional<B> getBody() {
      return Optional.ofNullable(this.body);
   }

   @Override
   public <T> Optional<T> getBody(Class<T> type) {
      return type == null ? Optional.empty() : this.getBody(Argument.of(type));
   }

   public FullHttpResponse getNativeResponse() {
      return this.nettyHttpResponse;
   }

   @Override
   public <T> Optional<T> getBody(Argument<T> type) {
      if (type == null) {
         return Optional.empty();
      } else {
         Class<T> javaType = type.getType();
         if (javaType == Void.TYPE) {
            return Optional.empty();
         } else if (javaType == ByteBuffer.class) {
            return Optional.of(this.byteBufferFactory.wrap(this.nettyHttpResponse.content()));
         } else if (javaType == ByteBuf.class) {
            return Optional.of(this.nettyHttpResponse.content());
         } else if (javaType == byte[].class && this.bodyBytes != null) {
            return Optional.of(this.bodyBytes);
         } else {
            Optional<T> result = (Optional)this.convertedBodies.computeIfAbsent(type, argument -> {
               boolean isOptional = argument.getType() == Optional.class;
               Argument finalArgument = isOptional ? (Argument)argument.getFirstTypeVariable().orElse(argument) : argument;

               Optional<T> converted;
               try {
                  if (this.bodyBytes != null) {
                     return this.convertBytes(this.bodyBytes, finalArgument);
                  }

                  Optional<B> existing = this.getBody();
                  if (existing.isPresent()) {
                     converted = this.getBody().flatMap(b -> {
                        if (b instanceof ByteBuffer) {
                           ByteBuf var5x = (ByteBuf)((ByteBuffer)b).asNativeBuffer();
                           return this.convertByteBuf(var5x, finalArgument);
                        } else {
                           Optional opt = ConversionService.SHARED.convert(b, ConversionContext.of(finalArgument));
                           if (!opt.isPresent()) {
                              ByteBuf contentx = this.nettyHttpResponse.content();
                              return this.convertByteBuf(contentx, finalArgument);
                           } else {
                              return opt;
                           }
                        }
                     });
                  } else {
                     ByteBuf content = this.nettyHttpResponse.content();
                     converted = this.convertByteBuf(content, finalArgument);
                  }
               } catch (RuntimeException var7) {
                  if (this.status.getCode() < 400) {
                     throw var7;
                  }

                  if (LOG.isDebugEnabled()) {
                     LOG.debug("Error decoding HTTP error response body: " + var7.getMessage(), var7);
                  }

                  converted = Optional.empty();
               }

               return isOptional ? Optional.of(converted) : converted;
            });
            if (LOG.isTraceEnabled() && !result.isPresent()) {
               LOG.trace("Unable to convert response body to target type {}", javaType);
            }

            return result;
         }
      }
   }

   private boolean isParseableBodyType(Class<?> rawBodyType) {
      return CharSequence.class.isAssignableFrom(rawBodyType) || Map.class.isAssignableFrom(rawBodyType);
   }

   private <T> Optional convertByteBuf(ByteBuf content, Argument<T> type) {
      if (this.complete) {
         return Optional.empty();
      } else if (content.refCnt() != 0 && content.readableBytes() != 0) {
         Optional<MediaType> contentType = this.getContentType();
         if (contentType.isPresent()) {
            if (this.mediaTypeCodecRegistry != null) {
               this.bodyBytes = ByteBufUtil.getBytes(content);
               if (CharSequence.class.isAssignableFrom(type.getType())) {
                  Charset charset = (Charset)contentType.flatMap(MediaType::getCharset).orElse(StandardCharsets.UTF_8);
                  return Optional.of(new String(this.bodyBytes, charset));
               }

               if (type.getType() == byte[].class) {
                  return Optional.of(this.bodyBytes);
               }

               Optional<MediaTypeCodec> foundCodec = this.mediaTypeCodecRegistry.findCodec((MediaType)contentType.get());
               if (foundCodec.isPresent()) {
                  MediaTypeCodec codec = (MediaTypeCodec)foundCodec.get();
                  return Optional.of(codec.<T>decode(type, this.bodyBytes));
               }
            }
         } else if (LOG.isTraceEnabled()) {
            LOG.trace("Missing or unknown Content-Type received from server.");
         }

         return ConversionService.SHARED.convert(content, ConversionContext.of(type));
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Full HTTP response received an empty body");
         }

         if (!this.convertedBodies.isEmpty()) {
            for(Entry<Argument, Optional> entry : this.convertedBodies.entrySet()) {
               Argument existing = (Argument)entry.getKey();
               if (type.getType().isAssignableFrom(existing.getType())) {
                  return (Optional)entry.getValue();
               }
            }
         }

         return Optional.empty();
      }
   }

   private <T> Optional convertBytes(byte[] bytes, Argument<T> type) {
      Optional<MediaType> contentType = this.getContentType();
      boolean hasContentType = contentType.isPresent();
      if (this.mediaTypeCodecRegistry != null && hasContentType) {
         if (CharSequence.class.isAssignableFrom(type.getType())) {
            Charset charset = (Charset)contentType.flatMap(MediaType::getCharset).orElse(StandardCharsets.UTF_8);
            return Optional.of(new String(bytes, charset));
         }

         if (type.getType() == byte[].class) {
            return Optional.of(bytes);
         }

         Optional<MediaTypeCodec> foundCodec = this.mediaTypeCodecRegistry.findCodec((MediaType)contentType.get());
         if (foundCodec.isPresent()) {
            MediaTypeCodec codec = (MediaTypeCodec)foundCodec.get();
            return Optional.of(codec.<T>decode(type, bytes));
         }
      }

      return ConversionService.SHARED.convert(bytes, ConversionContext.of(type));
   }

   @Override
   public void onComplete() {
      this.complete = true;
   }

   @NonNull
   @Override
   public FullHttpResponse toFullHttpResponse() {
      return this.nettyHttpResponse;
   }

   @NonNull
   @Override
   public io.netty.handler.codec.http.HttpResponse toHttpResponse() {
      return this.nettyHttpResponse;
   }

   @Override
   public boolean isStream() {
      return false;
   }
}
