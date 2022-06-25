package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.NettyHttpHeaders;
import io.micronaut.http.netty.NettyHttpResponseBuilder;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.http.netty.cookies.NettyCookies;
import io.micronaut.http.netty.stream.StreamedHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import java.util.Objects;
import java.util.Optional;

@Internal
class NettyStreamedHttpResponse<B> implements MutableHttpResponse<B>, NettyHttpResponseBuilder {
   private final StreamedHttpResponse nettyResponse;
   private HttpStatus status;
   private final NettyHttpHeaders headers;
   private final NettyCookies nettyCookies;
   private B body;
   private MutableConvertibleValues<Object> attributes;

   NettyStreamedHttpResponse(StreamedHttpResponse response, HttpStatus httpStatus) {
      this.nettyResponse = response;
      this.status = httpStatus;
      this.headers = new NettyHttpHeaders(response.headers(), ConversionService.SHARED);
      this.nettyCookies = new NettyCookies(response.headers(), ConversionService.SHARED);
   }

   public StreamedHttpResponse getNettyResponse() {
      return this.nettyResponse;
   }

   @Override
   public String reason() {
      return this.nettyResponse.status().reasonPhrase();
   }

   @Override
   public HttpStatus getStatus() {
      return this.status;
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
               attributes = new MutableConvertibleValuesMap<>();
               this.attributes = attributes;
            }
         }
      }

      return attributes;
   }

   public void setBody(B body) {
      this.body = body;
   }

   @Override
   public Optional<B> getBody() {
      return Optional.ofNullable(this.body);
   }

   @NonNull
   @Override
   public FullHttpResponse toFullHttpResponse() {
      throw new UnsupportedOperationException("Cannot convert a stream response to a full response");
   }

   @NonNull
   @Override
   public StreamedHttpResponse toStreamHttpResponse() {
      return this.nettyResponse;
   }

   @NonNull
   @Override
   public HttpResponse toHttpResponse() {
      return this.nettyResponse;
   }

   @Override
   public boolean isStream() {
      return true;
   }

   @Override
   public MutableHttpResponse<B> cookie(Cookie cookie) {
      if (cookie instanceof NettyCookie) {
         NettyCookie nettyCookie = (NettyCookie)cookie;
         String value = ClientCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie());
         this.headers.add(HttpHeaderNames.SET_COOKIE, value);
         return this;
      } else {
         throw new IllegalArgumentException("Argument is not a Netty compatible Cookie");
      }
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
   public <T> MutableHttpResponse<T> body(@Nullable T body) {
      this.body = (B)body;
      return this;
   }

   @Override
   public MutableHttpResponse<B> status(HttpStatus status, CharSequence message) {
      this.status = (HttpStatus)Objects.requireNonNull(status, "Status is required");
      return this;
   }
}
