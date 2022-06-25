package io.micronaut.http.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.http.netty.stream.DefaultStreamedHttpResponse;
import io.micronaut.http.netty.stream.StreamedHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Internal
@TypeHint({NettyMutableHttpResponse.class})
public class NettyMutableHttpResponse<B> implements MutableHttpResponse<B>, NettyHttpResponseBuilder {
   private static final ServerCookieEncoder DEFAULT_SERVER_COOKIE_ENCODER = ServerCookieEncoder.LAX;
   private final HttpVersion httpVersion;
   private HttpResponseStatus httpResponseStatus;
   private final NettyHttpHeaders headers;
   private Object body;
   private Optional<Object> optionalBody;
   private final HttpHeaders nettyHeaders;
   private final HttpHeaders trailingNettyHeaders;
   private final DecoderResult decoderResult;
   private final ConversionService conversionService;
   private MutableConvertibleValues<Object> attributes;
   private ServerCookieEncoder serverCookieEncoder = DEFAULT_SERVER_COOKIE_ENCODER;
   private final NettyMutableHttpResponse.BodyConvertor bodyConvertor = this.newBodyConvertor();

   public NettyMutableHttpResponse(FullHttpResponse nettyResponse, ConversionService conversionService) {
      this(
         nettyResponse.protocolVersion(),
         nettyResponse.status(),
         nettyResponse.headers(),
         nettyResponse.trailingHeaders(),
         nettyResponse.content(),
         nettyResponse.decoderResult(),
         conversionService
      );
   }

   public NettyMutableHttpResponse(ConversionService conversionService) {
      this(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, null, conversionService);
   }

   public NettyMutableHttpResponse(HttpVersion httpVersion, HttpResponseStatus httpResponseStatus, ConversionService conversionService) {
      this(httpVersion, httpResponseStatus, null, conversionService);
   }

   public NettyMutableHttpResponse(HttpVersion httpVersion, HttpResponseStatus httpResponseStatus, Object body, ConversionService conversionService) {
      this(httpVersion, httpResponseStatus, new DefaultHttpHeaders(), body, conversionService);
   }

   public NettyMutableHttpResponse(
      HttpVersion httpVersion, HttpResponseStatus httpResponseStatus, HttpHeaders nettyHeaders, Object body, ConversionService conversionService
   ) {
      this(httpVersion, httpResponseStatus, nettyHeaders, EmptyHttpHeaders.INSTANCE, body, null, conversionService);
   }

   private NettyMutableHttpResponse(
      HttpVersion httpVersion,
      HttpResponseStatus httpResponseStatus,
      HttpHeaders nettyHeaders,
      HttpHeaders trailingNettyHeaders,
      Object body,
      DecoderResult decoderResult,
      ConversionService conversionService
   ) {
      this.httpVersion = httpVersion;
      this.httpResponseStatus = httpResponseStatus;
      this.nettyHeaders = nettyHeaders;
      this.trailingNettyHeaders = trailingNettyHeaders;
      this.decoderResult = decoderResult;
      this.conversionService = conversionService;
      this.headers = new NettyHttpHeaders(nettyHeaders, conversionService);
      this.setBody(body);
   }

   public HttpVersion getNettyHttpVersion() {
      return this.httpVersion;
   }

   public HttpResponseStatus getNettyHttpStatus() {
      return this.httpResponseStatus;
   }

   public HttpHeaders getNettyHeaders() {
      return this.nettyHeaders;
   }

   public String toString() {
      HttpStatus status = this.getStatus();
      return status.getCode() + " " + status.getReason();
   }

   @Override
   public MutableHttpHeaders getHeaders() {
      return this.headers;
   }

   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      MutableConvertibleValues<Object> attributes = this.attributes;
      if (attributes == null) {
         synchronized(this) {
            attributes = this.attributes;
            if (attributes == null) {
               attributes = new MutableConvertibleValuesMap<>(new ConcurrentHashMap(4));
               this.attributes = attributes;
            }
         }
      }

      return attributes;
   }

   @Override
   public HttpStatus getStatus() {
      return HttpStatus.valueOf(this.httpResponseStatus.code());
   }

   @Override
   public int code() {
      return this.httpResponseStatus.code();
   }

   @Override
   public String reason() {
      return this.httpResponseStatus.reasonPhrase();
   }

   @Override
   public MutableHttpResponse<B> cookie(Cookie cookie) {
      if (cookie instanceof NettyCookie) {
         NettyCookie nettyCookie = (NettyCookie)cookie;
         String value = this.serverCookieEncoder.encode(nettyCookie.getNettyCookie());
         this.headers.add(HttpHeaderNames.SET_COOKIE, value);
         return this;
      } else {
         throw new IllegalArgumentException("Argument is not a Netty compatible Cookie");
      }
   }

   @Override
   public MutableHttpResponse<B> cookies(Set<Cookie> cookies) {
      if (cookies != null && !cookies.isEmpty()) {
         for(Cookie cookie : cookies) {
            this.cookie(cookie);
         }

         return this;
      } else {
         return this;
      }
   }

   @Override
   public Optional<B> getBody() {
      return this.optionalBody;
   }

   @Override
   public <T1> Optional<T1> getBody(Class<T1> type) {
      return this.getBody(Argument.of(type));
   }

   @Override
   public <T> Optional<T> getBody(Argument<T> type) {
      return this.bodyConvertor.convert(type, (T)this.body);
   }

   @Override
   public MutableHttpResponse<B> status(HttpStatus status, CharSequence message) {
      message = (CharSequence)(message == null ? status.getReason() : message);
      this.httpResponseStatus = new HttpResponseStatus(status.getCode(), message.toString());
      return this;
   }

   @Override
   public <T> MutableHttpResponse<T> body(@Nullable T body) {
      if (this.body != body) {
         if (this.body instanceof ByteBuf) {
            ((ByteBuf)this.body).release();
         }

         this.setBody(body);
         this.bodyConvertor.cleanup();
      }

      return this;
   }

   public ServerCookieEncoder getServerCookieEncoder() {
      return this.serverCookieEncoder;
   }

   public void setServerCookieEncoder(ServerCookieEncoder serverCookieEncoder) {
      this.serverCookieEncoder = serverCookieEncoder;
   }

   @NonNull
   @Override
   public FullHttpResponse toFullHttpResponse() {
      ByteBuf content;
      if (this.body == null) {
         content = Unpooled.EMPTY_BUFFER;
      } else {
         if (!(this.body instanceof ByteBuf)) {
            throw new IllegalStateException("Body needs to be converted to ByteBuf from " + this.body.getClass());
         }

         content = (ByteBuf)this.body;
      }

      DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(
         this.httpVersion, this.httpResponseStatus, content, this.nettyHeaders, this.trailingNettyHeaders
      );
      if (this.decoderResult != null) {
         defaultFullHttpResponse.setDecoderResult(this.decoderResult);
      }

      return defaultFullHttpResponse;
   }

   @NonNull
   @Override
   public StreamedHttpResponse toStreamHttpResponse() {
      ByteBuf content;
      if (this.body == null) {
         content = Unpooled.EMPTY_BUFFER;
      } else {
         if (!(this.body instanceof ByteBuf)) {
            throw new IllegalStateException("Body needs to be converted to ByteBuf from " + this.body.getClass());
         }

         content = (ByteBuf)this.body;
      }

      DefaultStreamedHttpResponse streamedHttpResponse = new DefaultStreamedHttpResponse(
         HttpVersion.HTTP_1_1, this.httpResponseStatus, true, Publishers.just(new DefaultLastHttpContent(content))
      );
      streamedHttpResponse.headers().setAll(this.nettyHeaders);
      return streamedHttpResponse;
   }

   @NonNull
   @Override
   public HttpResponse toHttpResponse() {
      return this.toFullHttpResponse();
   }

   @Override
   public boolean isStream() {
      return false;
   }

   private void setBody(Object body) {
      this.body = body;
      this.optionalBody = Optional.ofNullable(body);
      Optional<MediaType> contentType = this.getContentType();
      if (!contentType.isPresent() && body != null) {
         MediaType.fromType(body.getClass()).ifPresent(this::contentType);
      }

   }

   private NettyMutableHttpResponse.BodyConvertor newBodyConvertor() {
      return new NettyMutableHttpResponse.BodyConvertor() {
         @Override
         public Optional convert(Argument valueType, Object value) {
            if (value == null) {
               return Optional.empty();
            } else {
               return Argument.OBJECT_ARGUMENT.equalsType(valueType)
                  ? Optional.of(value)
                  : this.convertFromNext(NettyMutableHttpResponse.this.conversionService, valueType, value);
            }
         }
      };
   }

   private abstract static class BodyConvertor<T> {
      private NettyMutableHttpResponse.BodyConvertor<T> nextConvertor;

      private BodyConvertor() {
      }

      public abstract Optional<T> convert(Argument<T> valueType, T value);

      protected synchronized Optional<T> convertFromNext(ConversionService conversionService, Argument<T> conversionValueType, T value) {
         if (this.nextConvertor == null) {
            ArgumentConversionContext<T> context = ConversionContext.of(conversionValueType);
            final Optional<T> conversion;
            if (value instanceof ByteBuffer) {
               conversion = conversionService.convert(((ByteBuffer)value).asNativeBuffer(), context);
            } else {
               conversion = conversionService.convert(value, context);
            }

            this.nextConvertor = new NettyMutableHttpResponse.BodyConvertor<T>() {
               @Override
               public Optional<T> convert(Argument<T> valueType, T value) {
                  return conversionValueType.equalsType(valueType) ? conversion : this.convertFromNext(conversionService, valueType, value);
               }
            };
            return conversion;
         } else {
            return this.nextConvertor.convert(conversionValueType, value);
         }
      }

      public void cleanup() {
         this.nextConvertor = null;
      }
   }
}
