package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.PushCapableHttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.AbstractNettyHttpRequest;
import io.micronaut.http.netty.NettyHttpHeaders;
import io.micronaut.http.netty.NettyHttpParameters;
import io.micronaut.http.netty.NettyHttpRequestBuilder;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.http.netty.cookies.NettyCookies;
import io.micronaut.http.netty.stream.DefaultStreamedHttpRequest;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.InternalServerException;
import io.micronaut.web.router.RouteMatch;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.multipart.AbstractHttpData;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.MixedAttribute;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Internal
public class NettyHttpRequest<T> extends AbstractNettyHttpRequest<T> implements HttpRequest<T>, PushCapableHttpRequest<T> {
   private static final HttpHeaders SERVER_PUSH_EXCLUDE_HEADERS = new DefaultHttpHeaders();
   boolean destroyed = false;
   private final NettyHttpHeaders headers;
   private final ChannelHandlerContext channelHandlerContext;
   private final HttpServerConfiguration serverConfiguration;
   private MutableConvertibleValues<Object> attributes;
   private NettyCookies nettyCookies;
   private List<ByteBufHolder> receivedContent = new ArrayList();
   private Map<IdentityWrapper, HttpData> receivedData = new LinkedHashMap();
   private T bodyUnwrapped;
   private Supplier<Optional<T>> body;
   private RouteMatch<?> matchedRoute;
   private boolean bodyRequired;
   private final NettyHttpRequest.BodyConvertor bodyConvertor = this.newBodyConvertor();

   public NettyHttpRequest(
      io.netty.handler.codec.http.HttpRequest nettyRequest,
      ChannelHandlerContext ctx,
      ConversionService environment,
      HttpServerConfiguration serverConfiguration
   ) {
      super(nettyRequest, environment);
      Objects.requireNonNull(nettyRequest, "Netty request cannot be null");
      Objects.requireNonNull(ctx, "ChannelHandlerContext cannot be null");
      Objects.requireNonNull(environment, "Environment cannot be null");
      Channel channel = ctx.channel();
      if (channel != null) {
         channel.attr(ServerAttributeKeys.REQUEST_KEY).set(this);
      }

      this.serverConfiguration = serverConfiguration;
      this.channelHandlerContext = ctx;
      this.headers = new NettyHttpHeaders(nettyRequest.headers(), this.conversionService);
      this.body = SupplierUtil.memoizedNonEmpty(() -> {
         T built = (T)this.buildBody();
         this.bodyUnwrapped = built;
         return Optional.ofNullable(built);
      });
   }

   @Internal
   public final void prepareHttp2ResponseIfNecessary(@NonNull HttpResponse finalResponse) {
      HttpVersion httpVersion = this.getHttpVersion();
      boolean isHttp2 = httpVersion == HttpVersion.HTTP_2_0;
      if (isHttp2) {
         HttpHeaders nativeHeaders = this.nettyRequest.headers();
         String streamId = nativeHeaders.get(STREAM_ID);
         if (streamId != null) {
            finalResponse.headers().set(STREAM_ID, streamId);
         }
      }

   }

   @Override
   public MutableHttpRequest<T> mutate() {
      return new NettyHttpRequest.NettyMutableHttpRequest();
   }

   @NonNull
   @Override
   public Optional<Object> getAttribute(CharSequence name) {
      return Optional.ofNullable(this.getAttributes().getValue(((CharSequence)Objects.requireNonNull(name, "Name cannot be null")).toString()));
   }

   public String toString() {
      return this.getMethodName() + " " + this.getUri();
   }

   public io.netty.handler.codec.http.HttpRequest getNativeRequest() {
      return this.nettyRequest;
   }

   public ChannelHandlerContext getChannelHandlerContext() {
      return this.channelHandlerContext;
   }

   @Override
   public Cookies getCookies() {
      NettyCookies cookies = this.nettyCookies;
      if (cookies == null) {
         synchronized(this) {
            cookies = this.nettyCookies;
            if (cookies == null) {
               cookies = new NettyCookies(this.getPath(), this.headers.getNettyHeaders(), this.conversionService);
               this.nettyCookies = cookies;
            }
         }
      }

      return cookies;
   }

   @Override
   public InetSocketAddress getRemoteAddress() {
      return (InetSocketAddress)this.getChannelHandlerContext().channel().remoteAddress();
   }

   @Override
   public InetSocketAddress getServerAddress() {
      return (InetSocketAddress)this.getChannelHandlerContext().channel().localAddress();
   }

   @Override
   public String getServerName() {
      return this.getServerAddress().getHostName();
   }

   @Override
   public boolean isSecure() {
      ChannelHandlerContext channelHandlerContext = this.getChannelHandlerContext();
      return channelHandlerContext.pipeline().get(SslHandler.class) != null;
   }

   @Override
   public io.micronaut.http.HttpHeaders getHeaders() {
      return this.headers;
   }

   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      MutableConvertibleValues<Object> attributes = this.attributes;
      if (attributes == null) {
         synchronized(this) {
            attributes = this.attributes;
            if (attributes == null) {
               attributes = new MutableConvertibleValuesMap<>(new HashMap(4));
               this.attributes = attributes;
            }
         }
      }

      return attributes;
   }

   @Override
   public Optional<T> getBody() {
      return (Optional<T>)this.body.get();
   }

   protected Object buildBody() {
      if (this.receivedData.isEmpty()) {
         if (!this.receivedContent.isEmpty()) {
            int size = this.receivedContent.size();
            CompositeByteBuf byteBufs = this.channelHandlerContext.alloc().compositeBuffer(size);

            for(ByteBufHolder holder : this.receivedContent) {
               ByteBuf content = holder.content();
               if (content != null) {
                  content.touch();
                  byteBufs.addComponent(true, content.retain());
               }
            }

            return byteBufs;
         } else {
            return null;
         }
      } else {
         Map body = new LinkedHashMap(this.receivedData.size());

         for(HttpData data : this.receivedData.values()) {
            String newValue = this.getContent(data);
            body.compute(data.getName(), (key, oldValue) -> {
               if (oldValue == null) {
                  return newValue;
               } else if (oldValue instanceof Collection) {
                  ((Collection)oldValue).add(newValue);
                  return oldValue;
               } else {
                  ArrayList<Object> values = new ArrayList(2);
                  values.add(oldValue);
                  values.add(newValue);
                  return values;
               }
            });
         }

         return body;
      }
   }

   private String getContent(HttpData data) {
      try {
         return data.getString(this.serverConfiguration.getDefaultCharset());
      } catch (IOException var4) {
         throw new InternalServerException("Error retrieving or decoding the value for: " + data.getName());
      }
   }

   @Override
   public <T1> Optional<T1> getBody(Class<T1> type) {
      return this.getBody(Argument.of(type));
   }

   @Override
   public <T1> Optional<T1> getBody(Argument<T1> type) {
      return this.getBody().flatMap(t -> this.bodyConvertor.convert(type, (T)t));
   }

   @Internal
   public void release() {
      this.destroyed = true;
      Consumer<Object> releaseIfNecessary = this::releaseIfNecessary;
      this.getBody().ifPresent(releaseIfNecessary);
      this.receivedContent.forEach(releaseIfNecessary);
      this.receivedData.values().forEach(releaseIfNecessary);
      this.releaseIfNecessary(this.bodyUnwrapped);
      if (this.attributes != null) {
         this.attributes.values().forEach(releaseIfNecessary);
      }

      if (this.nettyRequest instanceof StreamedHttpRequest) {
         ((StreamedHttpRequest)this.nettyRequest).closeIfNoSubscriber();
      }

   }

   protected void releaseIfNecessary(Object value) {
      if (value instanceof ReferenceCounted) {
         ReferenceCounted referenceCounted = (ReferenceCounted)value;
         int i = referenceCounted.refCnt();
         if (i != 0) {
            referenceCounted.release();
         }
      }

   }

   @Internal
   public void setBody(T body) {
      ReferenceCountUtil.retain(body);
      this.bodyUnwrapped = body;
      this.body = () -> Optional.ofNullable(body);
      this.bodyConvertor.cleanup();
   }

   @Internal
   public RouteMatch<?> getMatchedRoute() {
      return this.matchedRoute;
   }

   @Internal
   public void addContent(ByteBufHolder httpContent) {
      httpContent.touch();
      if (!(httpContent instanceof AbstractHttpData) && !(httpContent instanceof MixedAttribute)) {
         this.receivedContent.add(httpContent.retain());
      } else {
         this.receivedData.computeIfAbsent(new IdentityWrapper(httpContent), key -> {
            httpContent.retain();
            return (HttpData)httpContent;
         });
      }

   }

   @Internal
   void setMatchedRoute(RouteMatch<?> matchedRoute) {
      this.matchedRoute = matchedRoute;
   }

   @Internal
   void setBodyRequired(boolean bodyRequired) {
      this.bodyRequired = bodyRequired;
   }

   @Internal
   boolean isBodyRequired() {
      return this.bodyRequired || HttpMethod.requiresRequestBody(this.getMethod());
   }

   @Override
   public boolean isServerPushSupported() {
      Http2ConnectionHandler http2ConnectionHandler = this.channelHandlerContext.pipeline().get(Http2ConnectionHandler.class);
      return http2ConnectionHandler != null && http2ConnectionHandler.connection().remote().allowPushTo();
   }

   @Override
   public PushCapableHttpRequest<T> serverPush(@NonNull HttpRequest<?> request) {
      ChannelHandlerContext connectionHandlerContext = this.channelHandlerContext.pipeline().context(Http2ConnectionHandler.class);
      if (connectionHandlerContext != null) {
         Http2ConnectionHandler connectionHandler = (Http2ConnectionHandler)connectionHandlerContext.handler();
         if (!connectionHandler.connection().remote().allowPushTo()) {
            throw new UnsupportedOperationException("Server push not supported by this client: Client is HTTP2 but does not report support for this feature");
         } else {
            URI configuredUri = request.getUri();
            String scheme = configuredUri.getScheme();
            if (scheme == null) {
               scheme = this.channelHandlerContext.pipeline().get(SslHandler.class) == null ? "http" : "https";
            }

            String authority = configuredUri.getAuthority();
            if (authority == null) {
               authority = this.getHeaders().get("Host");
            }

            String path = configuredUri.getPath();
            if (path != null && path.startsWith("/")) {
               String query = configuredUri.getQuery();
               String fragment = configuredUri.getFragment();

               URI fixedUri;
               try {
                  fixedUri = new URI(scheme, authority, path, query, fragment);
               } catch (URISyntaxException var15) {
                  throw new IllegalArgumentException("Illegal URI", var15);
               }

               io.netty.handler.codec.http.HttpRequest inboundRequest = NettyHttpRequestBuilder.toHttpRequest(request);
               Iterator<Entry<CharSequence, CharSequence>> itr = this.headers.getNettyHeaders().iteratorCharSequence();

               while(itr.hasNext()) {
                  Entry<CharSequence, CharSequence> entry = (Entry)itr.next();
                  if (!inboundRequest.headers().contains((CharSequence)entry.getKey()) && !SERVER_PUSH_EXCLUDE_HEADERS.contains((CharSequence)entry.getKey())) {
                     inboundRequest.headers().add((CharSequence)entry.getKey(), entry.getValue());
                  }
               }

               if (!inboundRequest.headers().contains(HttpHeaderNames.REFERER)) {
                  inboundRequest.headers().add(HttpHeaderNames.REFERER, this.getUri().toString());
               }

               io.netty.handler.codec.http.HttpRequest outboundRequest = new DefaultHttpRequest(
                  inboundRequest.protocolVersion(), inboundRequest.method(), fixedUri.toString(), inboundRequest.headers()
               );
               int ourStream = this.nettyRequest.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
               int newStream = connectionHandler.connection().local().incrementAndGetNextStreamId();
               connectionHandler.encoder()
                  .frameWriter()
                  .writePushPromise(
                     connectionHandlerContext,
                     ourStream,
                     newStream,
                     HttpConversionUtil.toHttp2Headers(outboundRequest, false),
                     0,
                     connectionHandlerContext.voidPromise()
                  );
               inboundRequest.headers().setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), newStream);
               inboundRequest.headers().setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), ourStream);
               connectionHandlerContext.executor().execute(() -> connectionHandlerContext.fireChannelRead(inboundRequest));
               return this;
            } else {
               throw new IllegalArgumentException("Request must have an absolute path");
            }
         }
      } else {
         throw new UnsupportedOperationException("Server push not supported by this client: Not a HTTP2 client");
      }
   }

   @Override
   protected Charset initCharset(Charset characterEncoding) {
      return characterEncoding == null ? this.serverConfiguration.getDefaultCharset() : characterEncoding;
   }

   @Internal
   final boolean isFormOrMultipartData() {
      MediaType ct = (MediaType)this.headers.contentType().orElse(null);
      return ct != null && (ct.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE) || ct.equals(MediaType.MULTIPART_FORM_DATA_TYPE));
   }

   @Internal
   final boolean isFormData() {
      MediaType ct = (MediaType)this.headers.contentType().orElse(null);
      return ct != null && ct.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
   }

   static NettyHttpRequest remove(ChannelHandlerContext ctx) {
      Channel channel = ctx.channel();
      Attribute<NettyHttpRequest> attr = channel.attr(ServerAttributeKeys.REQUEST_KEY);
      return attr.getAndSet(null);
   }

   private NettyHttpRequest.BodyConvertor newBodyConvertor() {
      return new NettyHttpRequest.BodyConvertor() {
         @Override
         public Optional convert(Argument valueType, Object value) {
            if (value == null) {
               return Optional.empty();
            } else {
               return Argument.OBJECT_ARGUMENT.equalsType(valueType)
                  ? Optional.of(value)
                  : this.convertFromNext(NettyHttpRequest.this.conversionService, valueType, (T)value);
            }
         }
      };
   }

   static {
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.ETAG, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.IF_MATCH, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.IF_MODIFIED_SINCE, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.IF_NONE_MATCH, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.IF_UNMODIFIED_SINCE, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.LAST_MODIFIED, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.ACCEPT_RANGES, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.CONTENT_RANGE, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.IF_RANGE, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.RANGE, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.EXPECT, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.REFERER, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.PROXY_AUTHENTICATE, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpHeaderNames.PROXY_AUTHORIZATION, "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpConversionUtil.ExtensionHeaderNames.PATH.text(), "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "");
      SERVER_PUSH_EXCLUDE_HEADERS.add(HttpConversionUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), "");
   }

   private abstract static class BodyConvertor<T> {
      private NettyHttpRequest.BodyConvertor<T> nextConvertor;

      private BodyConvertor() {
      }

      public abstract Optional<T> convert(Argument<T> valueType, T value);

      protected synchronized Optional<T> convertFromNext(ConversionService conversionService, Argument<T> conversionValueType, T value) {
         if (this.nextConvertor == null) {
            final Optional<T> conversion = conversionService.convert(value, ConversionContext.of(conversionValueType));
            this.nextConvertor = new NettyHttpRequest.BodyConvertor<T>() {
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

   private class NettyMutableHttpRequest implements MutableHttpRequest<T>, NettyHttpRequestBuilder {
      private URI uri;
      private MutableHttpParameters httpParameters;
      private Object body;

      private NettyMutableHttpRequest() {
         this.uri = NettyHttpRequest.this.uri;
      }

      @Override
      public MutableHttpRequest<T> cookie(Cookie cookie) {
         if (cookie instanceof NettyCookie) {
            NettyCookie nettyCookie = (NettyCookie)cookie;
            String value = ClientCookieEncoder.LAX.encode(nettyCookie.getNettyCookie());
            NettyHttpRequest.this.headers.add(HttpHeaderNames.COOKIE, value);
         }

         return this;
      }

      @Override
      public MutableHttpRequest<T> uri(URI uri) {
         this.uri = uri;
         if (uri.getQuery() != null) {
            this.httpParameters = null;
         }

         return this;
      }

      @Override
      public <T1> MutableHttpRequest<T1> body(T1 body) {
         this.body = body;
         return this;
      }

      @Override
      public MutableHttpHeaders getHeaders() {
         return NettyHttpRequest.this.headers;
      }

      @NonNull
      @Override
      public MutableConvertibleValues<Object> getAttributes() {
         return NettyHttpRequest.this.getAttributes();
      }

      @NonNull
      @Override
      public Optional<T> getBody() {
         return this.body != null ? Optional.of(this.body) : NettyHttpRequest.this.getBody();
      }

      @NonNull
      @Override
      public Cookies getCookies() {
         return NettyHttpRequest.this.getCookies();
      }

      @Override
      public MutableHttpParameters getParameters() {
         MutableHttpParameters httpParameters = this.httpParameters;
         if (httpParameters == null) {
            synchronized(this) {
               httpParameters = this.httpParameters;
               if (httpParameters == null) {
                  QueryStringDecoder queryStringDecoder = NettyHttpRequest.this.createDecoder(this.uri);
                  httpParameters = new NettyHttpParameters(queryStringDecoder.parameters(), NettyHttpRequest.this.conversionService, null);
                  this.httpParameters = httpParameters;
               }
            }
         }

         return httpParameters;
      }

      @NonNull
      @Override
      public HttpMethod getMethod() {
         return NettyHttpRequest.this.getMethod();
      }

      @NonNull
      @Override
      public URI getUri() {
         return this.uri != null ? this.uri : NettyHttpRequest.this.getUri();
      }

      @NonNull
      @Override
      public FullHttpRequest toFullHttpRequest() {
         io.netty.handler.codec.http.HttpRequest nr = NettyHttpRequest.this.nettyRequest;
         return (FullHttpRequest)(nr instanceof FullHttpRequest
            ? (FullHttpRequest)NettyHttpRequest.this.nettyRequest
            : new DefaultFullHttpRequest(nr.protocolVersion(), nr.method(), nr.uri(), Unpooled.EMPTY_BUFFER, nr.headers(), EmptyHttpHeaders.INSTANCE));
      }

      @NonNull
      @Override
      public StreamedHttpRequest toStreamHttpRequest() {
         if (this.isStream()) {
            return (StreamedHttpRequest)NettyHttpRequest.this.nettyRequest;
         } else {
            FullHttpRequest fullHttpRequest = this.toFullHttpRequest();
            DefaultStreamedHttpRequest request = new DefaultStreamedHttpRequest(
               fullHttpRequest.protocolVersion(),
               fullHttpRequest.method(),
               fullHttpRequest.uri(),
               true,
               Publishers.just(new DefaultLastHttpContent(fullHttpRequest.content()))
            );
            request.headers().setAll(fullHttpRequest.headers());
            return request;
         }
      }

      @NonNull
      @Override
      public io.netty.handler.codec.http.HttpRequest toHttpRequest() {
         return (io.netty.handler.codec.http.HttpRequest)(this.isStream() ? this.toStreamHttpRequest() : this.toFullHttpRequest());
      }

      @Override
      public boolean isStream() {
         return NettyHttpRequest.this.nettyRequest instanceof StreamedHttpRequest;
      }

      @Override
      public MutableHttpRequest<T> mutate() {
         return NettyHttpRequest.this.new NettyMutableHttpRequest();
      }
   }
}
