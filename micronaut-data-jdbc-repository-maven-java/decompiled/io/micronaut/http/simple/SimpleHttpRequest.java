package io.micronaut.http.simple;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.cookies.SimpleCookies;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;

public class SimpleHttpRequest<B> implements MutableHttpRequest<B> {
   private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
   private final SimpleCookies cookies = new SimpleCookies(ConversionService.SHARED);
   private final SimpleHttpHeaders headers = new SimpleHttpHeaders(ConversionService.SHARED);
   private final SimpleHttpParameters parameters = new SimpleHttpParameters(ConversionService.SHARED);
   private HttpMethod method;
   private URI uri;
   private Object body;

   public SimpleHttpRequest(HttpMethod method, String uri, B body) {
      this.method = method;

      try {
         this.uri = new URI(uri);
      } catch (URISyntaxException var5) {
         throw new IllegalArgumentException("Wrong URI", var5);
      }

      this.body = body;
   }

   @Override
   public MutableHttpRequest<B> cookie(Cookie cookie) {
      this.cookies.put(cookie.getName(), cookie);
      return this;
   }

   @Override
   public MutableHttpRequest<B> cookies(Set<Cookie> cookies) {
      for(Cookie cookie : cookies) {
         this.cookie(cookie);
      }

      return this;
   }

   @Override
   public MutableHttpRequest<B> uri(URI uri) {
      this.uri = uri;
      return this;
   }

   @Override
   public <T> MutableHttpRequest<T> body(T body) {
      this.body = body;
      return this;
   }

   @Override
   public MutableHttpHeaders getHeaders() {
      return this.headers;
   }

   @Override
   public Cookies getCookies() {
      return this.cookies;
   }

   @Override
   public MutableHttpParameters getParameters() {
      return this.parameters;
   }

   @Override
   public HttpMethod getMethod() {
      return this.method;
   }

   @Override
   public URI getUri() {
      return this.uri;
   }

   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      return this.attributes;
   }

   @Override
   public Optional<B> getBody() {
      return Optional.ofNullable(this.body);
   }
}
