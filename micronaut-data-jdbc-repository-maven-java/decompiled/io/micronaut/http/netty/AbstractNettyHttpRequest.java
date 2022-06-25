package io.micronaut.http.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.netty.stream.DefaultStreamedHttpRequest;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;
import io.netty.util.DefaultAttributeMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

@Internal
public abstract class AbstractNettyHttpRequest<B> extends DefaultAttributeMap implements HttpRequest<B>, NettyHttpRequestBuilder {
   public static final AsciiString STREAM_ID = HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text();
   public static final AsciiString HTTP2_SCHEME = HttpConversionUtil.ExtensionHeaderNames.SCHEME.text();
   protected final io.netty.handler.codec.http.HttpRequest nettyRequest;
   protected final ConversionService<?> conversionService;
   protected final HttpMethod httpMethod;
   protected final URI uri;
   protected final String httpMethodName;
   private NettyHttpParameters httpParameters;
   private Optional<MediaType> mediaType;
   private Charset charset;
   private Optional<Locale> locale;
   private String path;
   private Collection<MediaType> accept;

   public AbstractNettyHttpRequest(io.netty.handler.codec.http.HttpRequest nettyRequest, ConversionService conversionService) {
      this.nettyRequest = nettyRequest;
      this.conversionService = conversionService;
      URI fullUri = URI.create(nettyRequest.uri());
      if (fullUri.getAuthority() != null || fullUri.getScheme() != null) {
         try {
            fullUri = new URI(null, null, fullUri.getPath(), fullUri.getQuery(), fullUri.getFragment());
         } catch (URISyntaxException var5) {
            throw new IllegalArgumentException(var5);
         }
      }

      this.uri = fullUri;
      this.httpMethodName = nettyRequest.method().name();
      this.httpMethod = HttpMethod.parse(this.httpMethodName);
   }

   @NonNull
   @Override
   public io.netty.handler.codec.http.HttpRequest toHttpRequest() {
      return this.nettyRequest;
   }

   @NonNull
   @Override
   public FullHttpRequest toFullHttpRequest() {
      if (this.nettyRequest instanceof FullHttpRequest) {
         return (FullHttpRequest)this.nettyRequest;
      } else {
         DefaultFullHttpRequest httpRequest = new DefaultFullHttpRequest(
            this.nettyRequest.protocolVersion(), this.nettyRequest.method(), this.nettyRequest.uri()
         );
         httpRequest.headers().setAll(this.nettyRequest.headers());
         return httpRequest;
      }
   }

   @NonNull
   @Override
   public StreamedHttpRequest toStreamHttpRequest() {
      if (this.isStream()) {
         return (StreamedHttpRequest)this.nettyRequest;
      } else {
         return this.nettyRequest instanceof FullHttpRequest
            ? new DefaultStreamedHttpRequest(
               HttpVersion.HTTP_1_1,
               this.nettyRequest.method(),
               this.nettyRequest.uri(),
               true,
               Publishers.just(new DefaultLastHttpContent(((FullHttpRequest)this.nettyRequest).content()))
            )
            : new DefaultStreamedHttpRequest(
               HttpVersion.HTTP_1_1, this.nettyRequest.method(), this.nettyRequest.uri(), true, Publishers.just(LastHttpContent.EMPTY_LAST_CONTENT)
            );
      }
   }

   @Override
   public boolean isStream() {
      return this.nettyRequest instanceof StreamedHttpRequest;
   }

   @Override
   public io.micronaut.http.HttpVersion getHttpVersion() {
      return this.nettyRequest.headers().contains(STREAM_ID) ? io.micronaut.http.HttpVersion.HTTP_2_0 : io.micronaut.http.HttpVersion.HTTP_1_1;
   }

   public io.netty.handler.codec.http.HttpRequest getNettyRequest() {
      return this.nettyRequest;
   }

   @Override
   public HttpParameters getParameters() {
      NettyHttpParameters httpParameters = this.httpParameters;
      if (httpParameters == null) {
         synchronized(this) {
            httpParameters = this.httpParameters;
            if (httpParameters == null) {
               httpParameters = this.decodeParameters();
               this.httpParameters = httpParameters;
            }
         }
      }

      return httpParameters;
   }

   @Override
   public Collection<MediaType> accept() {
      if (this.accept == null) {
         this.accept = HttpRequest.super.accept();
      }

      return this.accept;
   }

   @Override
   public Optional<MediaType> getContentType() {
      if (this.mediaType == null) {
         this.mediaType = HttpRequest.super.getContentType();
      }

      return this.mediaType;
   }

   @Override
   public Charset getCharacterEncoding() {
      if (this.charset == null) {
         this.charset = this.initCharset(HttpRequest.super.getCharacterEncoding());
      }

      return this.charset;
   }

   @Override
   public Optional<Locale> getLocale() {
      if (this.locale == null) {
         this.locale = HttpRequest.super.getLocale();
      }

      return this.locale;
   }

   @Override
   public HttpMethod getMethod() {
      return this.httpMethod;
   }

   @Override
   public URI getUri() {
      return this.uri;
   }

   @Override
   public String getPath() {
      String path = this.path;
      if (path == null) {
         synchronized(this) {
            path = this.path;
            if (path == null) {
               path = this.decodePath();
               this.path = path;
            }
         }
      }

      return path;
   }

   protected abstract Charset initCharset(Charset characterEncoding);

   protected final QueryStringDecoder createDecoder(URI uri) {
      Charset charset = this.getCharacterEncoding();
      return charset != null ? new QueryStringDecoder(uri, charset) : new QueryStringDecoder(uri);
   }

   private String decodePath() {
      QueryStringDecoder queryStringDecoder = this.createDecoder(this.uri);
      return queryStringDecoder.rawPath();
   }

   private NettyHttpParameters decodeParameters() {
      QueryStringDecoder queryStringDecoder = this.createDecoder(this.uri);
      return new NettyHttpParameters(queryStringDecoder.parameters(), this.conversionService, null);
   }

   @Override
   public String getMethodName() {
      return this.httpMethodName;
   }
}
