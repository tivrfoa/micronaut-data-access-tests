package io.micronaut.http.simple;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.cookies.SimpleCookies;
import java.util.Optional;
import java.util.Set;

@TypeHint({SimpleHttpResponse.class})
class SimpleHttpResponse<B> implements MutableHttpResponse<B> {
   private final MutableHttpHeaders headers = new SimpleHttpHeaders(ConversionService.SHARED);
   private final SimpleCookies cookies = new SimpleCookies(ConversionService.SHARED);
   private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
   private HttpStatus status = HttpStatus.OK;
   private Object body;

   @Override
   public MutableHttpResponse<B> cookie(Cookie cookie) {
      this.cookies.put(cookie.getName(), cookie);
      return this;
   }

   @Override
   public MutableHttpResponse<B> cookies(Set<Cookie> cookies) {
      for(Cookie cookie : cookies) {
         this.cookie(cookie);
      }

      return this;
   }

   @Override
   public MutableHttpHeaders getHeaders() {
      return this.headers;
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
   public <T> MutableHttpResponse<T> body(@Nullable T body) {
      this.body = body;
      return this;
   }

   @Override
   public MutableHttpResponse<B> status(HttpStatus status, CharSequence message) {
      this.status = status;
      return this;
   }

   @Override
   public HttpStatus getStatus() {
      return this.status;
   }

   @Override
   public Cookies getCookies() {
      return this.cookies;
   }
}
