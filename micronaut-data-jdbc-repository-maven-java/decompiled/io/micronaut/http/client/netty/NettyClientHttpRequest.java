package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.NettyHttpHeaders;
import io.micronaut.http.netty.NettyHttpParameters;
import io.micronaut.http.netty.NettyHttpRequestBuilder;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.http.netty.stream.DefaultStreamedHttpRequest;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.uri.UriBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.reactivestreams.Publisher;

@Internal
class NettyClientHttpRequest<B> implements MutableHttpRequest<B>, NettyHttpRequestBuilder {
   static final CharSequence CHANNEL = "netty_channel";
   private final NettyHttpHeaders headers = new NettyHttpHeaders();
   private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
   private final HttpMethod httpMethod;
   private final String httpMethodName;
   private final Map<String, String> cookies = new LinkedHashMap(1);
   private URI uri;
   private Object body;
   private NettyHttpParameters httpParameters;

   NettyClientHttpRequest(HttpMethod httpMethod, URI uri, String httpMethodName) {
      this.httpMethod = httpMethod;
      this.uri = uri;
      this.httpMethodName = httpMethodName;
   }

   NettyClientHttpRequest(HttpMethod httpMethod, String uri) {
      this(httpMethod, uri, httpMethod.name());
   }

   NettyClientHttpRequest(HttpMethod httpMethod, String uri, String httpMethodName) {
      this(httpMethod, URI.create(uri), httpMethodName);
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
   public MutableHttpRequest<B> cookie(Cookie cookie) {
      if (cookie instanceof NettyCookie) {
         NettyCookie nettyCookie = (NettyCookie)cookie;
         String value = ClientCookieEncoder.LAX.encode(nettyCookie.getNettyCookie());
         this.cookies.put(cookie.getName(), value);
         String headerValue;
         if (this.cookies.size() > 1) {
            headerValue = String.join(";", this.cookies.values());
         } else {
            headerValue = value;
         }

         this.headers.set(HttpHeaderNames.COOKIE, headerValue);
         return this;
      } else {
         throw new IllegalArgumentException("Argument is not a Netty compatible Cookie");
      }
   }

   @Override
   public MutableHttpRequest<B> cookies(Set<Cookie> cookies) {
      if (cookies.size() > 1) {
         for(Cookie cookie : cookies) {
            if (!(cookie instanceof NettyCookie)) {
               throw new IllegalArgumentException("Argument is not a Netty compatible Cookie");
            }

            NettyCookie nettyCookie = (NettyCookie)cookie;
            String value = ClientCookieEncoder.LAX.encode(nettyCookie.getNettyCookie());
            this.cookies.put(cookie.getName(), value);
         }

         this.headers.set(HttpHeaderNames.COOKIE, String.join(";", this.cookies.values()));
      } else if (!cookies.isEmpty()) {
         this.cookie((Cookie)cookies.iterator().next());
      }

      return this;
   }

   @Override
   public MutableHttpRequest<B> uri(URI uri) {
      this.uri = uri;
      return this;
   }

   @Override
   public Optional<B> getBody() {
      return Optional.ofNullable(this.body);
   }

   @Override
   public <T> Optional<T> getBody(Class<T> type) {
      return this.getBody(Argument.of(type));
   }

   @Override
   public <T> Optional<T> getBody(Argument<T> type) {
      return this.getBody().flatMap(b -> ConversionService.SHARED.convert(b, ConversionContext.of(type)));
   }

   @Override
   public <T> MutableHttpRequest<T> body(T body) {
      this.body = body;
      return this;
   }

   @Override
   public Cookies getCookies() {
      throw new UnsupportedOperationException("not yet implemented");
   }

   @Override
   public MutableHttpParameters getParameters() {
      NettyHttpParameters httpParameters = this.httpParameters;
      if (httpParameters == null) {
         synchronized(this) {
            httpParameters = this.httpParameters;
            if (httpParameters == null) {
               httpParameters = this.decodeParameters(this.getUri());
               this.httpParameters = httpParameters;
            }
         }
      }

      return httpParameters;
   }

   @Override
   public HttpMethod getMethod() {
      return this.httpMethod;
   }

   @Override
   public URI getUri() {
      return this.uri;
   }

   private NettyHttpParameters decodeParameters(URI uri) {
      QueryStringDecoder queryStringDecoder = this.createDecoder(uri);
      return new NettyHttpParameters(queryStringDecoder.parameters(), ConversionService.SHARED, (name, value) -> {
         UriBuilder newUri = UriBuilder.of(this.getUri());
         newUri.replaceQueryParam(name.toString(), value.toArray());
         this.uri(newUri.build());
      });
   }

   protected QueryStringDecoder createDecoder(URI uri) {
      Charset charset = this.getCharacterEncoding();
      return charset != null ? new QueryStringDecoder(uri, charset) : new QueryStringDecoder(uri);
   }

   private static io.netty.handler.codec.http.HttpMethod getMethod(String httpMethodName) {
      return io.netty.handler.codec.http.HttpMethod.valueOf(httpMethodName);
   }

   private String resolveUriPath() {
      URI uri = this.getUri();
      if (StringUtils.isNotEmpty(uri.getScheme())) {
         try {
            uri = new URI(null, null, null, -1, uri.getPath(), uri.getQuery(), uri.getFragment());
         } catch (URISyntaxException var3) {
         }
      }

      return uri.toString();
   }

   public String toString() {
      return this.getMethodName() + " " + this.uri;
   }

   @Override
   public String getMethodName() {
      return this.httpMethodName;
   }

   @NonNull
   @Override
   public FullHttpRequest toFullHttpRequest() {
      String uriStr = this.resolveUriPath();
      io.netty.handler.codec.http.HttpMethod method = getMethod(this.httpMethodName);
      DefaultFullHttpRequest req;
      if (this.body != null) {
         if (this.body instanceof ByteBuf) {
            req = new DefaultFullHttpRequest(
               HttpVersion.HTTP_1_1, method, uriStr, (ByteBuf)this.body, this.headers.getNettyHeaders(), EmptyHttpHeaders.INSTANCE
            );
         } else {
            req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uriStr);
            req.headers().setAll(this.headers.getNettyHeaders());
         }
      } else {
         req = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, method, uriStr, Unpooled.EMPTY_BUFFER, this.headers.getNettyHeaders(), EmptyHttpHeaders.INSTANCE
         );
      }

      return req;
   }

   @NonNull
   @Override
   public StreamedHttpRequest toStreamHttpRequest() {
      if (this.body instanceof Publisher) {
         String uriStr = this.resolveUriPath();
         io.netty.handler.codec.http.HttpMethod method = getMethod(this.httpMethodName);
         DefaultStreamedHttpRequest req = new DefaultStreamedHttpRequest(HttpVersion.HTTP_1_1, method, uriStr, (Publisher<HttpContent>)this.body);
         req.headers().setAll(this.headers.getNettyHeaders());
         return req;
      } else {
         throw new IllegalStateException("Body must be set to a publisher of HTTP content first!");
      }
   }

   @NonNull
   @Override
   public HttpRequest toHttpRequest() {
      return (HttpRequest)(this.isStream() ? this.toStreamHttpRequest() : this.toFullHttpRequest());
   }

   @Override
   public boolean isStream() {
      return this.body instanceof Publisher;
   }
}
