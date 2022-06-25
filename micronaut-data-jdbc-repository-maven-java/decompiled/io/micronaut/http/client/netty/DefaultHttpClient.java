package io.micronaut.http.client.netty;

import io.micronaut.buffer.netty.NettyByteBufferFactory;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataResolver;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.beans.BeanMap;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.io.buffer.ByteBufferFactory;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseWrapper;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.client.ProxyRequestOptions;
import io.micronaut.http.client.StreamingHttpClient;
import io.micronaut.http.client.exceptions.ContentLengthExceededException;
import io.micronaut.http.client.exceptions.HttpClientErrorDecoder;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.exceptions.NoHostException;
import io.micronaut.http.client.exceptions.ReadTimeoutException;
import io.micronaut.http.client.filter.ClientFilterResolutionContext;
import io.micronaut.http.client.filter.DefaultHttpClientFilterResolver;
import io.micronaut.http.client.filters.ClientServerContextFilter;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.http.client.multipart.MultipartDataFactory;
import io.micronaut.http.client.netty.ssl.NettyClientSslBuilder;
import io.micronaut.http.client.netty.websocket.NettyWebSocketClientHandler;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.http.filter.HttpClientFilterResolver;
import io.micronaut.http.filter.HttpFilterResolver;
import io.micronaut.http.multipart.MultipartException;
import io.micronaut.http.netty.AbstractNettyHttpRequest;
import io.micronaut.http.netty.NettyHttpHeaders;
import io.micronaut.http.netty.NettyHttpRequestBuilder;
import io.micronaut.http.netty.NettyHttpResponseBuilder;
import io.micronaut.http.netty.channel.ChannelPipelineListener;
import io.micronaut.http.netty.channel.NettyThreadFactory;
import io.micronaut.http.netty.stream.DefaultHttp2Content;
import io.micronaut.http.netty.stream.DefaultStreamedHttpResponse;
import io.micronaut.http.netty.stream.Http2Content;
import io.micronaut.http.netty.stream.HttpStreamsClientHandler;
import io.micronaut.http.netty.stream.JsonSubscriber;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.netty.stream.StreamedHttpResponse;
import io.micronaut.http.netty.stream.StreamingInboundHttp2ToHttpAdapter;
import io.micronaut.http.sse.Event;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.http.uri.UriTemplate;
import io.micronaut.jackson.databind.JacksonDatabindMapper;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.codec.JsonMediaTypeCodec;
import io.micronaut.json.codec.JsonStreamMediaTypeCodec;
import io.micronaut.json.codec.MapperMediaTypeCodec;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.instrument.Instrumentation;
import io.micronaut.scheduling.instrument.InvocationInstrumenter;
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.context.WebSocketBean;
import io.micronaut.websocket.context.WebSocketBeanRegistry;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.Proxy.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Internal
public class DefaultHttpClient implements WebSocketClient, HttpClient, StreamingHttpClient, SseClient, ProxyHttpClient, Closeable, AutoCloseable {
   private static final Logger DEFAULT_LOG = LoggerFactory.getLogger(DefaultHttpClient.class);
   private static final AttributeKey<Http2Stream> STREAM_KEY = AttributeKey.valueOf("micronaut.http2.stream");
   private static final int DEFAULT_HTTP_PORT = 80;
   private static final int DEFAULT_HTTPS_PORT = 443;
   private static final HttpHeaders REDIRECT_HEADER_BLOCKLIST = new DefaultHttpHeaders();
   protected final Bootstrap bootstrap;
   protected EventLoopGroup group;
   protected MediaTypeCodecRegistry mediaTypeCodecRegistry;
   protected ByteBufferFactory<ByteBufAllocator, ByteBuf> byteBufferFactory = new NettyByteBufferFactory();
   private final List<HttpFilterResolver.FilterEntry<HttpClientFilter>> clientFilterEntries;
   private final HttpVersion httpVersion;
   private final Scheduler scheduler;
   private final LoadBalancer loadBalancer;
   private final HttpClientConfiguration configuration;
   private final String contextPath;
   private final SslContext sslContext;
   private final ThreadFactory threadFactory;
   private final boolean shutdownGroup;
   private final Charset defaultCharset;
   private final ChannelPoolMap<DefaultHttpClient.RequestKey, ChannelPool> poolMap;
   private final Logger log;
   @Nullable
   private final Long readTimeoutMillis;
   @Nullable
   private final Long connectionTimeAliveMillis;
   private final HttpClientFilterResolver<ClientFilterResolutionContext> filterResolver;
   private final WebSocketBeanRegistry webSocketRegistry;
   private final RequestBinderRegistry requestBinderRegistry;
   private final Collection<ChannelPipelineListener> pipelineListeners;
   private final List<InvocationInstrumenterFactory> invocationInstrumenterFactories;

   public DefaultHttpClient(
      @Nullable LoadBalancer loadBalancer,
      @NonNull HttpClientConfiguration configuration,
      @Nullable String contextPath,
      @Nullable ThreadFactory threadFactory,
      NettyClientSslBuilder nettyClientSslBuilder,
      MediaTypeCodecRegistry codecRegistry,
      @Nullable AnnotationMetadataResolver annotationMetadataResolver,
      List<InvocationInstrumenterFactory> invocationInstrumenterFactories,
      HttpClientFilter... filters
   ) {
      this(
         loadBalancer,
         configuration.getHttpVersion(),
         configuration,
         contextPath,
         new DefaultHttpClientFilterResolver(annotationMetadataResolver, Arrays.asList(filters)),
         null,
         threadFactory,
         nettyClientSslBuilder,
         codecRegistry,
         WebSocketBeanRegistry.EMPTY,
         new DefaultRequestBinderRegistry(ConversionService.SHARED),
         null,
         NioSocketChannel::new,
         Collections.emptySet(),
         invocationInstrumenterFactories
      );
   }

   public DefaultHttpClient(
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpVersion httpVersion,
      @NonNull HttpClientConfiguration configuration,
      @Nullable String contextPath,
      @NonNull HttpClientFilterResolver<ClientFilterResolutionContext> filterResolver,
      List<HttpFilterResolver.FilterEntry<HttpClientFilter>> clientFilterEntries,
      @Nullable ThreadFactory threadFactory,
      @NonNull NettyClientSslBuilder nettyClientSslBuilder,
      @NonNull MediaTypeCodecRegistry codecRegistry,
      @NonNull WebSocketBeanRegistry webSocketBeanRegistry,
      @NonNull RequestBinderRegistry requestBinderRegistry,
      @Nullable EventLoopGroup eventLoopGroup,
      @NonNull ChannelFactory socketChannelFactory,
      Collection<ChannelPipelineListener> pipelineListeners,
      List<InvocationInstrumenterFactory> invocationInstrumenterFactories
   ) {
      ArgumentUtils.requireNonNull("nettyClientSslBuilder", nettyClientSslBuilder);
      ArgumentUtils.requireNonNull("codecRegistry", codecRegistry);
      ArgumentUtils.requireNonNull("webSocketBeanRegistry", webSocketBeanRegistry);
      ArgumentUtils.requireNonNull("requestBinderRegistry", requestBinderRegistry);
      ArgumentUtils.requireNonNull("configuration", configuration);
      ArgumentUtils.requireNonNull("filterResolver", filterResolver);
      ArgumentUtils.requireNonNull("socketChannelFactory", socketChannelFactory);
      this.loadBalancer = loadBalancer;
      this.httpVersion = httpVersion != null ? httpVersion : configuration.getHttpVersion();
      this.defaultCharset = configuration.getDefaultCharset();
      if (StringUtils.isNotEmpty(contextPath)) {
         if (contextPath.charAt(0) != '/') {
            contextPath = '/' + contextPath;
         }

         this.contextPath = contextPath;
      } else {
         this.contextPath = null;
      }

      this.bootstrap = new Bootstrap();
      this.configuration = configuration;
      this.sslContext = (SslContext)nettyClientSslBuilder.build(configuration.getSslConfiguration(), this.httpVersion).orElse(null);
      if (eventLoopGroup != null) {
         this.group = eventLoopGroup;
         this.shutdownGroup = false;
      } else {
         this.group = this.createEventLoopGroup(configuration, threadFactory);
         this.shutdownGroup = true;
      }

      this.scheduler = Schedulers.fromExecutorService(this.group);
      this.threadFactory = threadFactory;
      this.bootstrap.group(this.group).channelFactory(socketChannelFactory).option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true));
      Optional<Duration> readTimeout = configuration.getReadTimeout();
      this.readTimeoutMillis = (Long)readTimeout.map(duration -> !duration.isNegative() ? duration.toMillis() : null).orElse(null);
      Optional<Duration> connectTtl = configuration.getConnectTtl();
      this.connectionTimeAliveMillis = (Long)connectTtl.map(duration -> !duration.isNegative() ? duration.toMillis() : null).orElse(null);
      this.invocationInstrumenterFactories = invocationInstrumenterFactories == null ? Collections.emptyList() : invocationInstrumenterFactories;
      final HttpClientConfiguration.ConnectionPoolConfiguration connectionPoolConfiguration = configuration.getConnectionPoolConfiguration();
      if (!connectionPoolConfiguration.isEnabled() && this.httpVersion != HttpVersion.HTTP_2_0) {
         this.poolMap = null;
      } else {
         final int maxConnections = connectionPoolConfiguration.getMaxConnections();
         if (maxConnections > -1) {
            this.poolMap = new AbstractChannelPoolMap<DefaultHttpClient.RequestKey, ChannelPool>() {
               protected ChannelPool newPool(DefaultHttpClient.RequestKey key) {
                  Bootstrap newBootstrap = DefaultHttpClient.this.bootstrap.clone(DefaultHttpClient.this.group);
                  DefaultHttpClient.this.initBootstrapForProxy(newBootstrap, key.isSecure(), key.getHost(), key.getPort());
                  newBootstrap.remoteAddress(key.getRemoteAddress());
                  AbstractChannelPoolHandler channelPoolHandler = DefaultHttpClient.this.newPoolHandler(key);
                  long acquireTimeoutMillis = connectionPoolConfiguration.getAcquireTimeout().map(Duration::toMillis).orElse(-1L);
                  return new FixedChannelPool(
                     newBootstrap,
                     channelPoolHandler,
                     ChannelHealthChecker.ACTIVE,
                     acquireTimeoutMillis > -1L ? FixedChannelPool.AcquireTimeoutAction.FAIL : null,
                     acquireTimeoutMillis,
                     maxConnections,
                     connectionPoolConfiguration.getMaxPendingAcquires()
                  );
               }
            };
         } else {
            this.poolMap = new AbstractChannelPoolMap<DefaultHttpClient.RequestKey, ChannelPool>() {
               protected ChannelPool newPool(DefaultHttpClient.RequestKey key) {
                  Bootstrap newBootstrap = DefaultHttpClient.this.bootstrap.clone(DefaultHttpClient.this.group);
                  DefaultHttpClient.this.initBootstrapForProxy(newBootstrap, key.isSecure(), key.getHost(), key.getPort());
                  newBootstrap.remoteAddress(key.getRemoteAddress());
                  AbstractChannelPoolHandler channelPoolHandler = DefaultHttpClient.this.newPoolHandler(key);
                  return new SimpleChannelPool(newBootstrap, channelPoolHandler);
               }
            };
         }
      }

      Optional<Duration> connectTimeout = configuration.getConnectTimeout();
      connectTimeout.ifPresent(duration -> {
         Bootstrap var10000 = this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.valueOf((int)duration.toMillis()));
      });

      for(Entry<String, Object> entry : configuration.getChannelOptions().entrySet()) {
         Object v = entry.getValue();
         if (v != null) {
            String channelOption = (String)entry.getKey();
            this.bootstrap.option(ChannelOption.valueOf(channelOption), v);
         }
      }

      this.mediaTypeCodecRegistry = codecRegistry;
      this.log = (Logger)configuration.getLoggerName().map(LoggerFactory::getLogger).orElse(DEFAULT_LOG);
      this.filterResolver = filterResolver;
      if (clientFilterEntries != null) {
         this.clientFilterEntries = clientFilterEntries;
      } else {
         this.clientFilterEntries = filterResolver.resolveFilterEntries(new ClientFilterResolutionContext(null, AnnotationMetadata.EMPTY_METADATA));
      }

      this.webSocketRegistry = webSocketBeanRegistry != null ? webSocketBeanRegistry : WebSocketBeanRegistry.EMPTY;
      this.requestBinderRegistry = requestBinderRegistry;
      this.pipelineListeners = pipelineListeners;
   }

   public DefaultHttpClient(@Nullable URI uri) {
      this(uri, new DefaultHttpClientConfiguration());
   }

   public DefaultHttpClient() {
      this(null, new DefaultHttpClientConfiguration(), Collections.emptyList());
   }

   public DefaultHttpClient(@Nullable URI uri, @NonNull HttpClientConfiguration configuration) {
      this(
         uri == null ? null : LoadBalancer.fixed(uri),
         configuration,
         null,
         new DefaultThreadFactory(MultithreadEventLoopGroup.class),
         new NettyClientSslBuilder(new ResourceResolver()),
         createDefaultMediaTypeRegistry(),
         AnnotationMetadataResolver.DEFAULT,
         Collections.emptyList()
      );
   }

   public DefaultHttpClient(
      @Nullable LoadBalancer loadBalancer, HttpClientConfiguration configuration, List<InvocationInstrumenterFactory> invocationInstrumenterFactories
   ) {
      this(
         loadBalancer,
         configuration,
         null,
         new DefaultThreadFactory(MultithreadEventLoopGroup.class),
         new NettyClientSslBuilder(new ResourceResolver()),
         createDefaultMediaTypeRegistry(),
         AnnotationMetadataResolver.DEFAULT,
         invocationInstrumenterFactories
      );
   }

   public HttpClientConfiguration getConfiguration() {
      return this.configuration;
   }

   public Logger getLog() {
      return this.log;
   }

   public HttpClient start() {
      if (!this.isRunning()) {
         this.group = this.createEventLoopGroup(this.configuration, this.threadFactory);
      }

      return this;
   }

   @Override
   public boolean isRunning() {
      return !this.group.isShutdown();
   }

   public HttpClient stop() {
      if (this.isRunning()) {
         if (this.poolMap instanceof Iterable) {
            for(Entry<DefaultHttpClient.RequestKey, ChannelPool> entry : (Iterable)this.poolMap) {
               ChannelPool cp = (ChannelPool)entry.getValue();

               try {
                  if (cp instanceof SimpleChannelPool) {
                     this.addInstrumentedListener(((SimpleChannelPool)cp).closeAsync(), futurex -> {
                        if (!futurex.isSuccess()) {
                           Throwable cause = futurex.cause();
                           if (cause != null) {
                              this.log.error("Error shutting down HTTP client connection pool: " + cause.getMessage(), cause);
                           }
                        }

                     });
                  } else {
                     cp.close();
                  }
               } catch (Exception var7) {
                  this.log.error("Error shutting down HTTP client connection pool: " + var7.getMessage(), var7);
               }
            }
         }

         if (this.shutdownGroup) {
            Duration shutdownTimeout = (Duration)this.configuration.getShutdownTimeout().orElse(Duration.ofMillis(100L));
            Duration shutdownQuietPeriod = (Duration)this.configuration.getShutdownQuietPeriod().orElse(Duration.ofMillis(1L));
            Future<?> future = this.group.shutdownGracefully(shutdownQuietPeriod.toMillis(), shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
            this.addInstrumentedListener(future, f -> {
               if (!f.isSuccess() && this.log.isErrorEnabled()) {
                  Throwable cause = f.cause();
                  this.log.error("Error shutting down HTTP client: " + cause.getMessage(), cause);
               }

            });

            try {
               future.await(shutdownTimeout.toMillis());
            } catch (InterruptedException var6) {
            }
         }
      }

      return this;
   }

   public MediaTypeCodecRegistry getMediaTypeCodecRegistry() {
      return this.mediaTypeCodecRegistry;
   }

   public void setMediaTypeCodecRegistry(MediaTypeCodecRegistry mediaTypeCodecRegistry) {
      if (mediaTypeCodecRegistry != null) {
         this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
      }

   }

   @Override
   public BlockingHttpClient toBlocking() {
      return new BlockingHttpClient() {
         public void close() {
            DefaultHttpClient.this.close();
         }

         @Override
         public <I, O, E> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType) {
            Flux<HttpResponse<O>> publisher = Flux.from(DefaultHttpClient.this.exchange(request, bodyType, errorType));
            return publisher.doOnNext(res -> {
               Optional<ByteBuf> byteBuf = res.getBody(ByteBuf.class);
               byteBuf.ifPresent(bb -> {
                  if (bb.refCnt() > 0) {
                     ReferenceCountUtil.safeRelease(bb);
                  }

               });
               if (res instanceof FullNettyClientHttpResponse) {
                  ((FullNettyClientHttpResponse)res).onComplete();
               }

            }).blockFirst();
         }
      };
   }

   @Override
   public <I> Publisher<Event<ByteBuffer<?>>> eventStream(@NonNull HttpRequest<I> request) {
      return this.eventStreamOrError(request, null);
   }

   private <I> Publisher<Event<ByteBuffer<?>>> eventStreamOrError(@NonNull HttpRequest<I> request, @NonNull Argument<?> errorType) {
      if (request instanceof MutableHttpRequest) {
         ((MutableHttpRequest)request).accept(MediaType.TEXT_EVENT_STREAM_TYPE);
      }

      return Flux.create(
         emitter -> this.dataStream(request, errorType)
               .subscribe(
                  new Subscriber<ByteBuffer<?>>() {
                     private Subscription dataSubscription;
                     private DefaultHttpClient.CurrentEvent currentEvent;
         
                     @Override
                     public void onSubscribe(Subscription s) {
                        this.dataSubscription = s;
                        Disposable cancellable = () -> this.dataSubscription.cancel();
                        emitter.onCancel(cancellable);
                        if (!emitter.isCancelled() && emitter.requestedFromDownstream() > 0L) {
                           this.dataSubscription.request(1L);
                        }
         
                     }
         
                     public void onNext(ByteBuffer<?> buffer) {
                        try {
                           int len = buffer.readableBytes();
                           if (len == 0) {
                              try {
                                 Event event = Event.of(DefaultHttpClient.this.byteBufferFactory.wrap(this.currentEvent.data))
                                    .name(this.currentEvent.name)
                                    .retry(this.currentEvent.retry)
                                    .id(this.currentEvent.id);
                                 emitter.next(event);
                              } finally {
                                 this.currentEvent = null;
                              }
                           } else {
                              if (this.currentEvent == null) {
                                 this.currentEvent = new DefaultHttpClient.CurrentEvent();
                              }
         
                              int colonIndex = buffer.indexOf((byte)58);
                              if (colonIndex > 0) {
                                 String type = buffer.slice(0, colonIndex).toString(StandardCharsets.UTF_8).trim();
                                 int fromIndex = colonIndex + 1;
                                 if (buffer.getByte(fromIndex) == 32) {
                                    ++fromIndex;
                                 }
         
                                 if (fromIndex < len) {
                                    int toIndex = len - fromIndex;
                                    switch(type) {
                                       case "data":
                                          ByteBuffer content = buffer.slice(fromIndex, toIndex);
                                          byte[] d = this.currentEvent.data;
                                          if (d == null) {
                                             this.currentEvent.data = content.toByteArray();
                                          } else {
                                             this.currentEvent.data = ArrayUtils.concat(d, content.toByteArray());
                                          }
                                          break;
                                       case "id":
                                          ByteBuffer id = buffer.slice(fromIndex, toIndex);
                                          this.currentEvent.id = id.toString(StandardCharsets.UTF_8).trim();
                                          break;
                                       case "event":
                                          ByteBuffer event = buffer.slice(fromIndex, toIndex);
                                          this.currentEvent.name = event.toString(StandardCharsets.UTF_8).trim();
                                          break;
                                       case "retry":
                                          ByteBuffer retry = buffer.slice(fromIndex, toIndex);
                                          String text = retry.toString(StandardCharsets.UTF_8);
                                          if (!StringUtils.isEmpty(text)) {
                                             Long millis = Long.valueOf(text);
                                             this.currentEvent.retry = Duration.ofMillis(millis);
                                          }
                                    }
                                 }
                              }
                           }
         
                           if (emitter.requestedFromDownstream() > 0L && !emitter.isCancelled()) {
                              this.dataSubscription.request(1L);
                           }
                        } catch (Throwable var24) {
                           this.onError(var24);
                        } finally {
                           if (buffer instanceof ReferenceCounted) {
                              ((ReferenceCounted)buffer).release();
                           }
         
                        }
         
                     }
         
                     @Override
                     public void onError(Throwable t) {
                        this.dataSubscription.cancel();
                        if (t instanceof HttpClientException) {
                           emitter.error(t);
                        } else {
                           emitter.error(new HttpClientException("Error consuming Server Sent Events: " + t.getMessage(), t));
                        }
         
                     }
         
                     @Override
                     public void onComplete() {
                        emitter.complete();
                     }
                  }
               ),
         FluxSink.OverflowStrategy.BUFFER
      );
   }

   @Override
   public <I, B> Publisher<Event<B>> eventStream(@NonNull HttpRequest<I> request, @NonNull Argument<B> eventType) {
      return this.eventStream(request, eventType, DEFAULT_ERROR_TYPE);
   }

   @Override
   public <I, B> Publisher<Event<B>> eventStream(@NonNull HttpRequest<I> request, @NonNull Argument<B> eventType, @NonNull Argument<?> errorType) {
      return Flux.from(this.eventStreamOrError(request, errorType)).map(byteBufferEvent -> {
         ByteBuffer<?> data = (ByteBuffer)byteBufferEvent.getData();
         Optional<MediaTypeCodec> registeredCodec;
         if (this.mediaTypeCodecRegistry != null) {
            registeredCodec = this.mediaTypeCodecRegistry.findCodec(MediaType.APPLICATION_JSON_TYPE);
         } else {
            registeredCodec = Optional.empty();
         }

         if (registeredCodec.isPresent()) {
            B decoded = ((MediaTypeCodec)registeredCodec.get()).decode(eventType, data);
            return Event.of(byteBufferEvent, decoded);
         } else {
            throw new CodecException("JSON codec not present");
         }
      });
   }

   @Override
   public <I> Publisher<ByteBuffer<?>> dataStream(@NonNull HttpRequest<I> request) {
      return this.dataStream(request, DEFAULT_ERROR_TYPE);
   }

   @Override
   public <I> Publisher<ByteBuffer<?>> dataStream(@NonNull HttpRequest<I> request, @NonNull Argument<?> errorType) {
      HttpRequest<Object> parentRequest = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      return new MicronautFlux<>(
            Flux.from(this.resolveRequestURI(request)).flatMap(requestURI -> this.dataStreamImpl(request, errorType, parentRequest, requestURI))
         )
         .doAfterNext(buffer -> {
            Object o = buffer.asNativeBuffer();
            if (o instanceof ByteBuf) {
               ByteBuf byteBuf = (ByteBuf)o;
               if (byteBuf.refCnt() > 0) {
                  ReferenceCountUtil.safeRelease(byteBuf);
               }
            }
   
         });
   }

   @Override
   public <I> Publisher<HttpResponse<ByteBuffer<?>>> exchangeStream(@NonNull HttpRequest<I> request) {
      return this.exchangeStream(request, DEFAULT_ERROR_TYPE);
   }

   @Override
   public <I> Publisher<HttpResponse<ByteBuffer<?>>> exchangeStream(@NonNull HttpRequest<I> request, @NonNull Argument<?> errorType) {
      HttpRequest<Object> parentRequest = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      return new MicronautFlux<>(Flux.from(this.resolveRequestURI(request)).flatMap(uri -> this.exchangeStreamImpl(parentRequest, request, errorType, uri)))
         .doAfterNext(byteBufferHttpResponse -> {
            ByteBuffer<?> buffer = (ByteBuffer)byteBufferHttpResponse.body();
            if (buffer instanceof ReferenceCounted) {
               ((ReferenceCounted)buffer).release();
            }
   
         });
   }

   @Override
   public <I, O> Publisher<O> jsonStream(@NonNull HttpRequest<I> request, @NonNull Argument<O> type) {
      return this.jsonStream(request, type, DEFAULT_ERROR_TYPE);
   }

   @Override
   public <I, O> Publisher<O> jsonStream(@NonNull HttpRequest<I> request, @NonNull Argument<O> type, @NonNull Argument<?> errorType) {
      HttpRequest<Object> parentRequest = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      return Flux.from(this.resolveRequestURI(request)).flatMap(requestURI -> this.jsonStreamImpl(parentRequest, request, type, errorType, requestURI));
   }

   @Override
   public <I> Publisher<Map<String, Object>> jsonStream(@NonNull HttpRequest<I> request) {
      return this.jsonStream(request, Map.class);
   }

   @Override
   public <I, O> Publisher<O> jsonStream(@NonNull HttpRequest<I> request, @NonNull Class<O> type) {
      return this.jsonStream(request, Argument.of(type));
   }

   @Override
   public <I, O, E> Publisher<HttpResponse<O>> exchange(@NonNull HttpRequest<I> request, @NonNull Argument<O> bodyType, @NonNull Argument<E> errorType) {
      HttpRequest<Object> parentRequest = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      Publisher<URI> uriPublisher = this.resolveRequestURI(request);
      return Flux.from(uriPublisher).switchMap(uri -> this.exchangeImpl(uri, parentRequest, request, bodyType, errorType));
   }

   @Override
   public <T extends AutoCloseable> Publisher<T> connect(Class<T> clientEndpointType, MutableHttpRequest<?> request) {
      Publisher<URI> uriPublisher = this.resolveRequestURI(request);
      return Flux.from(uriPublisher).switchMap(resolvedURI -> this.connectWebSocket(resolvedURI, request, clientEndpointType, null));
   }

   @Override
   public <T extends AutoCloseable> Publisher<T> connect(Class<T> clientEndpointType, Map<String, Object> parameters) {
      WebSocketBean<T> webSocketBean = this.webSocketRegistry.getWebSocket(clientEndpointType);
      String uri = (String)webSocketBean.getBeanDefinition().stringValue(ClientWebSocket.class).orElse("/ws");
      uri = UriTemplate.of(uri).expand(parameters);
      MutableHttpRequest<Object> request = HttpRequest.GET(uri);
      Publisher<URI> uriPublisher = this.resolveRequestURI(request);
      return Flux.from(uriPublisher).switchMap(resolvedURI -> this.connectWebSocket(resolvedURI, request, clientEndpointType, webSocketBean));
   }

   @Override
   public void close() {
      this.stop();
   }

   private <T> Flux<T> connectWebSocket(URI uri, MutableHttpRequest<?> request, Class<T> clientEndpointType, WebSocketBean<T> webSocketBean) {
      Bootstrap bootstrap = this.bootstrap.clone();
      if (webSocketBean == null) {
         webSocketBean = this.webSocketRegistry.getWebSocket(clientEndpointType);
      }

      WebSocketBean<T> finalWebSocketBean = webSocketBean;
      return Flux.create(
         emitter -> {
            SslContext sslContext = this.buildSslContext(uri);
            final WebSocketVersion protocolVersion = (WebSocketVersion)finalWebSocketBean.getBeanDefinition()
               .enumValue(ClientWebSocket.class, "version", WebSocketVersion.class)
               .orElse(WebSocketVersion.V13);
            final int maxFramePayloadLength = finalWebSocketBean.messageMethod()
               .map(m -> m.intValue(OnMessage.class, "maxPayloadLength").orElse(65536))
               .orElse(65536);
            final String subprotocol = (String)finalWebSocketBean.getBeanDefinition().stringValue(ClientWebSocket.class, "subprotocol").orElse("");
   
            DefaultHttpClient.RequestKey requestKey;
            try {
               requestKey = new DefaultHttpClient.RequestKey(uri);
            } catch (HttpClientException var12) {
               emitter.error(var12);
               return;
            }
   
            bootstrap.remoteAddress(requestKey.getHost(), requestKey.getPort());
            this.initBootstrapForProxy(bootstrap, sslContext != null, requestKey.getHost(), requestKey.getPort());
            bootstrap.handler(
               new DefaultHttpClient.HttpClientInitializer(sslContext, requestKey.getHost(), requestKey.getPort(), false, false, false, null) {
                  @Override
                  protected void addFinalHandler(ChannelPipeline pipeline) {
                     pipeline.remove("http-decoder");
                     ReadTimeoutHandler readTimeoutHandler = pipeline.get(ReadTimeoutHandler.class);
                     if (readTimeoutHandler != null) {
                        pipeline.remove(readTimeoutHandler);
                     }
      
                     Optional readIdleTime = DefaultHttpClient.this.configuration.getReadIdleTimeout();
                     if (readIdleTime.isPresent()) {
                        Duration duration = (Duration)readIdleTime.get();
                        if (!duration.isNegative()) {
                           pipeline.addLast(
                              "idle-state", new IdleStateHandler(duration.toMillis(), duration.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS)
                           );
                        }
                     }
      
                     try {
                        String scheme = this.sslContext == null ? "ws" : "wss";
                        URI webSocketURL = UriBuilder.of(uri).scheme(scheme).host(this.host).port(this.port).build();
                        MutableHttpHeaders headers = request.getHeaders();
                        HttpHeaders customHeaders = EmptyHttpHeaders.INSTANCE;
                        if (headers instanceof NettyHttpHeaders) {
                           customHeaders = ((NettyHttpHeaders)headers).getNettyHeaders();
                        }
      
                        if (StringUtils.isNotEmpty(subprotocol)) {
                           customHeaders.add("Sec-WebSocket-Protocol", subprotocol);
                        }
      
                        NettyWebSocketClientHandler webSocketHandler = new NettyWebSocketClientHandler(
                           request,
                           finalWebSocketBean,
                           WebSocketClientHandshakerFactory.newHandshaker(
                              webSocketURL, protocolVersion, subprotocol, true, customHeaders, maxFramePayloadLength
                           ),
                           DefaultHttpClient.this.requestBinderRegistry,
                           DefaultHttpClient.this.mediaTypeCodecRegistry,
                           emitter
                        );
                        pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                        pipeline.addLast("micronaut-websocket-client", webSocketHandler);
                     } catch (Throwable var9) {
                        emitter.error(new WebSocketSessionException("Error opening WebSocket client session: " + var9.getMessage(), var9));
                     }
      
                  }
               }
            );
            this.addInstrumentedListener(bootstrap.connect(), future -> {
               if (!future.isSuccess()) {
                  emitter.error(future.cause());
               }
   
            });
         },
         FluxSink.OverflowStrategy.ERROR
      );
   }

   private <I> Flux<HttpResponse<ByteBuffer<?>>> exchangeStreamImpl(
      HttpRequest<Object> parentRequest, HttpRequest<I> request, Argument<?> errorType, URI requestURI
   ) {
      Flux<HttpResponse<Object>> streamResponsePublisher = Flux.from(this.buildStreamExchange(parentRequest, request, requestURI, errorType));
      return streamResponsePublisher.<HttpResponse<ByteBuffer<?>>>switchMap(
            response -> {
               StreamedHttpResponse streamedHttpResponse = NettyHttpResponseBuilder.toStreamResponse(response);
               Flux<HttpContent> httpContentReactiveSequence = Flux.from(streamedHttpResponse);
               return httpContentReactiveSequence.filter(message -> !(message.content() instanceof EmptyByteBuf))
                  .map(
                     message -> {
                        ByteBuf byteBuf = message.content();
                        if (this.log.isTraceEnabled()) {
                           this.log
                              .trace(
                                 "HTTP Client Streaming Response Received Chunk (length: {}) for Request: {} {}",
                                 byteBuf.readableBytes(),
                                 request.getMethodName(),
                                 request.getUri()
                              );
                           this.traceBody("Response", byteBuf);
                        }
            
                        ByteBuffer<?> byteBuffer = this.byteBufferFactory.wrap(byteBuf);
                        NettyStreamedHttpResponse<ByteBuffer<?>> thisResponse = new NettyStreamedHttpResponse<>(streamedHttpResponse, response.status());
                        thisResponse.setBody(byteBuffer);
                        return new HttpResponseWrapper<>(thisResponse);
                     }
                  );
            }
         )
         .doOnTerminate(() -> {
            Object o = request.getAttribute(NettyClientHttpRequest.CHANNEL).orElse(null);
            if (o instanceof Channel) {
               Channel c = (Channel)o;
               if (c.isOpen()) {
                  c.close();
               }
            }
   
         });
   }

   private <I, O> Flux<O> jsonStreamImpl(HttpRequest<?> parentRequest, HttpRequest<I> request, Argument<O> type, Argument<?> errorType, URI requestURI) {
      Flux<HttpResponse<Object>> streamResponsePublisher = Flux.from(this.buildStreamExchange(parentRequest, request, requestURI, errorType));
      return streamResponsePublisher.<O>switchMap(
            response -> {
               if (!(response instanceof NettyStreamedHttpResponse)) {
                  throw new IllegalStateException(
                     "Response has been wrapped in non streaming type. Do not wrap the response in client filters for stream requests"
                  );
               } else {
                  MapperMediaTypeCodec mediaTypeCodec = (MapperMediaTypeCodec)this.mediaTypeCodecRegistry
                     .findCodec(MediaType.APPLICATION_JSON_TYPE)
                     .orElseThrow(() -> new IllegalStateException("No JSON codec found"));
                  StreamedHttpResponse streamResponse = NettyHttpResponseBuilder.toStreamResponse(response);
                  Flux<HttpContent> httpContentReactiveSequence = Flux.from(streamResponse);
                  boolean isJsonStream = response.getContentType().map(mediaType -> mediaType.equals(MediaType.APPLICATION_JSON_STREAM_TYPE)).orElse(false);
                  boolean streamArray = !Iterable.class.isAssignableFrom(type.getType()) && !isJsonStream;
                  Processor<byte[], JsonNode> jsonProcessor = mediaTypeCodec.getJsonMapper()
                     .createReactiveParser(
                        p -> httpContentReactiveSequence.map(
                                 content -> {
                                    ByteBuf chunk = content.content();
                                    if (this.log.isTraceEnabled()) {
                                       this.log
                                          .trace(
                                             "HTTP Client Streaming Response Received Chunk (length: {}) for Request: {} {}",
                                             chunk.readableBytes(),
                                             request.getMethodName(),
                                             request.getUri()
                                          );
                                       this.traceBody("Chunk", chunk);
                                    }
                  
                                    byte[] var4x;
                                    try {
                                       var4x = ByteBufUtil.getBytes(chunk);
                                    } finally {
                                       chunk.release();
                                    }
                  
                                    return var4x;
                                 }
                              )
                              .subscribe(p),
                        streamArray
                     );
                  return Flux.from(jsonProcessor).map(jsonNode -> mediaTypeCodec.<O>decode(type, jsonNode));
               }
            }
         )
         .doOnTerminate(() -> {
            Object o = request.getAttribute(NettyClientHttpRequest.CHANNEL).orElse(null);
            if (o instanceof Channel) {
               Channel c = (Channel)o;
               if (c.isOpen()) {
                  c.close();
               }
            }
   
         });
   }

   private <I> Flux<ByteBuffer<?>> dataStreamImpl(HttpRequest<I> request, Argument<?> errorType, HttpRequest<Object> parentRequest, URI requestURI) {
      Flux<HttpResponse<Object>> streamResponsePublisher = Flux.from(this.buildStreamExchange(parentRequest, request, requestURI, errorType));
      Function<HttpContent, ByteBuffer<?>> contentMapper = message -> {
         ByteBuf byteBuf = message.content();
         return this.byteBufferFactory.wrap(byteBuf);
      };
      return streamResponsePublisher.<ByteBuffer<?>>switchMap(response -> {
         if (!(response instanceof NettyStreamedHttpResponse)) {
            throw new IllegalStateException("Response has been wrapped in non streaming type. Do not wrap the response in client filters for stream requests");
         } else {
            NettyStreamedHttpResponse nettyStreamedHttpResponse = (NettyStreamedHttpResponse)response;
            Flux<HttpContent> httpContentReactiveSequence = Flux.from(nettyStreamedHttpResponse.getNettyResponse());
            return httpContentReactiveSequence.filter(message -> !(message.content() instanceof EmptyByteBuf)).map(contentMapper);
         }
      }).doOnTerminate(() -> {
         Object o = request.getAttribute(NettyClientHttpRequest.CHANNEL).orElse(null);
         if (o instanceof Channel) {
            Channel c = (Channel)o;
            if (c.isOpen()) {
               c.close();
            }
         }

      });
   }

   private <I> Publisher<MutableHttpResponse<Object>> buildStreamExchange(
      @Nullable HttpRequest<?> parentRequest, @NonNull HttpRequest<I> request, @NonNull URI requestURI, @Nullable Argument<?> errorType
   ) {
      AtomicReference<HttpRequest<?>> requestWrapper = new AtomicReference(request);
      Flux<MutableHttpResponse<Object>> streamResponsePublisher = this.connectAndStream(
         parentRequest, request, requestURI, this.buildSslContext(requestURI), requestWrapper, false, true
      );
      streamResponsePublisher = this.readBodyOnError(errorType, streamResponsePublisher);
      streamResponsePublisher = Flux.from(this.applyFilterToResponsePublisher(parentRequest, request, requestURI, requestWrapper, streamResponsePublisher));
      return streamResponsePublisher.subscribeOn(this.scheduler);
   }

   @Override
   public Publisher<MutableHttpResponse<?>> proxy(@NonNull HttpRequest<?> request) {
      return this.proxy(request, ProxyRequestOptions.getDefault());
   }

   @Override
   public Publisher<MutableHttpResponse<?>> proxy(@NonNull HttpRequest<?> request, @NonNull ProxyRequestOptions options) {
      Objects.requireNonNull(options, "options");
      return Flux.from(this.resolveRequestURI(request))
         .flatMap(
            requestURI -> {
               MutableHttpRequest<?> httpRequest = request instanceof MutableHttpRequest ? (MutableHttpRequest)request : request.mutate();
               if (!options.isRetainHostHeader()) {
                  httpRequest.headers((Consumer<MutableHttpHeaders>)(headers -> headers.remove(HttpHeaderNames.HOST)));
               }
      
               AtomicReference<HttpRequest<?>> requestWrapper = new AtomicReference(httpRequest);
               Flux<MutableHttpResponse<Object>> proxyResponsePublisher = this.connectAndStream(
                  request, request, requestURI, this.buildSslContext(requestURI), requestWrapper, true, false
               );
               return Flux.from(
                  this.applyFilterToResponsePublisher(request, (HttpRequest)requestWrapper.get(), requestURI, requestWrapper, proxyResponsePublisher)
               );
            }
         );
   }

   private <I> Flux<MutableHttpResponse<Object>> connectAndStream(
      HttpRequest<?> parentRequest,
      HttpRequest<I> request,
      URI requestURI,
      SslContext sslContext,
      AtomicReference<HttpRequest<?>> requestWrapper,
      boolean isProxy,
      boolean failOnError
   ) {
      return Flux.create(
         emitter -> {
            ChannelFuture channelFuture;
            try {
               if (this.httpVersion == HttpVersion.HTTP_2_0) {
                  channelFuture = this.doConnect(
                     request,
                     requestURI,
                     sslContext,
                     true,
                     isProxy,
                     channelHandlerContext -> {
                        try {
                           Channel channel = channelHandlerContext.channel();
                           request.setAttribute(NettyClientHttpRequest.CHANNEL, channel);
                           this.streamRequestThroughChannel(parentRequest, (HttpRequest<?>)requestWrapper.get(), channel, failOnError)
                              .subscribe(new ForwardingSubscriber<>(emitter));
                        } catch (Exception var8x) {
                           emitter.error(var8x);
                        }
      
                     }
                  );
               } else {
                  channelFuture = this.doConnect(request, requestURI, sslContext, true, isProxy, null);
                  this.addInstrumentedListener(
                     channelFuture,
                     f -> {
                        if (f.isSuccess()) {
                           Channel channel = f.channel();
                           request.setAttribute(NettyClientHttpRequest.CHANNEL, channel);
                           this.streamRequestThroughChannel(parentRequest, (HttpRequest<?>)requestWrapper.get(), channel, failOnError)
                              .subscribe(new ForwardingSubscriber<>(emitter));
                        } else {
                           Throwable var8x = f.cause();
                           emitter.error(new HttpClientException("Connect error:" + var8x.getMessage(), var8x));
                        }
      
                     }
                  );
               }
            } catch (HttpClientException var11) {
               emitter.error(var11);
               return;
            }
   
            Disposable disposable = this.buildDisposableChannel(channelFuture);
            emitter.onDispose(disposable);
            emitter.onCancel(disposable);
         },
         FluxSink.OverflowStrategy.BUFFER
      );
   }

   private <I, O, E> Publisher<? extends HttpResponse<O>> exchangeImpl(
      URI requestURI, HttpRequest<?> parentRequest, HttpRequest<I> request, @NonNull Argument<O> bodyType, @NonNull Argument<E> errorType
   ) {
      AtomicReference<HttpRequest<?>> requestWrapper = new AtomicReference(request);
      Flux<HttpResponse<O>> responsePublisher = Flux.create(
         emitter -> {
            boolean multipart = MediaType.MULTIPART_FORM_DATA_TYPE.equals(request.getContentType().orElse(null));
            if (this.poolMap != null && !multipart) {
               try {
                  DefaultHttpClient.RequestKey var12x = new DefaultHttpClient.RequestKey(requestURI);
                  ChannelPool channelPool = this.poolMap.get(var12x);
                  Future<Channel> channelFuture = channelPool.acquire();
                  this.addInstrumentedListener(
                     channelFuture,
                     future -> {
                        if (future.isSuccess()) {
                           Channel channel = (Channel)future.get();
      
                           try {
                              this.sendRequestThroughChannel(
                                 (HttpRequest<I>)requestWrapper.get(), bodyType, errorType, emitter, channel, var12x.isSecure(), channelPool
                              );
                           } catch (Exception var10x) {
                              emitter.error(var10x);
                           }
                        } else {
                           Throwable var11xx = future.cause();
                           emitter.error(new HttpClientException("Connect Error: " + var11xx.getMessage(), var11xx));
                        }
      
                     }
                  );
               } catch (HttpClientException var11x) {
                  emitter.error(var11x);
               }
            } else {
               SslContext sslContext = this.buildSslContext(requestURI);
               ChannelFuture connectionFuture = this.doConnect(request, requestURI, sslContext, false, null);
               this.addInstrumentedListener(
                  connectionFuture,
                  future -> {
                     if (!future.isSuccess()) {
                        Throwable cause = future.cause();
                        if (emitter.isCancelled()) {
                           this.log.trace("Connection to {} failed, but emitter already cancelled.", requestURI, cause);
                        } else {
                           emitter.error(new HttpClientException("Connect Error: " + cause.getMessage(), cause));
                        }
                     } else {
                        try {
                           this.sendRequestThroughChannel(
                              (HttpRequest<I>)requestWrapper.get(), bodyType, errorType, emitter, connectionFuture.channel(), sslContext != null, null
                           );
                        } catch (Exception var10x) {
                           emitter.error(var10x);
                        }
                     }
      
                  }
               );
            }
   
         },
         FluxSink.OverflowStrategy.ERROR
      );
      Publisher<HttpResponse<O>> finalPublisher = this.applyFilterToResponsePublisher(parentRequest, request, requestURI, requestWrapper, responsePublisher);
      Flux<HttpResponse<O>> finalReactiveSequence = Flux.from(finalPublisher);
      Optional<Duration> readTimeout = this.configuration.getReadTimeout();
      if (readTimeout.isPresent()) {
         Duration rt = (Duration)readTimeout.get();
         if (!rt.isNegative()) {
            Duration duration = rt.plus(Duration.ofSeconds(1L));
            finalReactiveSequence = finalReactiveSequence.timeout(duration)
               .onErrorResume(throwable -> throwable instanceof TimeoutException ? Flux.error(ReadTimeoutException.TIMEOUT_EXCEPTION) : Flux.error(throwable));
         }
      }

      return finalReactiveSequence;
   }

   protected void closeChannelAsync(Channel channel) {
      if (channel.isOpen()) {
         ChannelFuture closeFuture = channel.closeFuture();
         closeFuture.addListener(f2 -> {
            if (!f2.isSuccess() && this.log.isErrorEnabled()) {
               Throwable cause = f2.cause();
               this.log.error("Error closing request connection: " + cause.getMessage(), cause);
            }

         });
      }

   }

   protected <I> Publisher<URI> resolveRequestURI(HttpRequest<I> request) {
      return this.resolveRequestURI(request, true);
   }

   protected <I> Publisher<URI> resolveRequestURI(HttpRequest<I> request, boolean includeContextPath) {
      URI requestURI = request.getUri();
      return (Publisher<URI>)(requestURI.getScheme() != null ? Flux.just(requestURI) : this.resolveURI(request, includeContextPath));
   }

   protected <I> Publisher<URI> resolveRedirectURI(HttpRequest<?> parentRequest, HttpRequest<I> request) {
      URI requestURI = request.getUri();
      if (requestURI.getScheme() != null) {
         return Flux.just(requestURI);
      } else if (parentRequest != null && parentRequest.getUri().getHost() != null) {
         URI parentURI = parentRequest.getUri();
         UriBuilder uriBuilder = UriBuilder.of(requestURI)
            .scheme(parentURI.getScheme())
            .userInfo(parentURI.getUserInfo())
            .host(parentURI.getHost())
            .port(parentURI.getPort());
         return Flux.just(uriBuilder.build());
      } else {
         return this.resolveURI(request, false);
      }
   }

   protected URI prependContextPath(URI requestURI) {
      if (StringUtils.isNotEmpty(this.contextPath)) {
         try {
            return new URI(StringUtils.prependUri(this.contextPath, requestURI.toString()));
         } catch (URISyntaxException var3) {
            throw new HttpClientException("Failed to construct the request URI", var3);
         }
      } else {
         return requestURI;
      }
   }

   protected Object getLoadBalancerDiscriminator() {
      return null;
   }

   private void initBootstrapForProxy(Bootstrap bootstrap, boolean ssl, String host, int port) {
      Proxy proxy = this.configuration.resolveProxy(ssl, host, port);
      if (proxy.type() != Type.DIRECT) {
         bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
      }

   }

   protected ChannelFuture doConnect(
      HttpRequest<?> request, URI uri, @Nullable SslContext sslCtx, boolean isStream, Consumer<ChannelHandlerContext> contextConsumer
   ) throws HttpClientException {
      return this.doConnect(request, uri, sslCtx, isStream, false, contextConsumer);
   }

   protected ChannelFuture doConnect(
      HttpRequest<?> request, URI uri, @Nullable SslContext sslCtx, boolean isStream, boolean isProxy, Consumer<ChannelHandlerContext> contextConsumer
   ) throws HttpClientException {
      DefaultHttpClient.RequestKey requestKey = new DefaultHttpClient.RequestKey(uri);
      return this.doConnect(request, requestKey.getHost(), requestKey.getPort(), sslCtx, isStream, isProxy, contextConsumer);
   }

   protected ChannelFuture doConnect(
      HttpRequest<?> request, String host, int port, @Nullable SslContext sslCtx, boolean isStream, Consumer<ChannelHandlerContext> contextConsumer
   ) {
      return this.doConnect(request, host, port, sslCtx, isStream, false, contextConsumer);
   }

   protected ChannelFuture doConnect(
      HttpRequest<?> request,
      String host,
      int port,
      @Nullable SslContext sslCtx,
      boolean isStream,
      boolean isProxy,
      Consumer<ChannelHandlerContext> contextConsumer
   ) {
      Bootstrap localBootstrap = this.bootstrap.clone();
      this.initBootstrapForProxy(localBootstrap, sslCtx != null, host, port);
      String acceptHeader = request.getHeaders().get("Accept");
      localBootstrap.handler(
         new DefaultHttpClient.HttpClientInitializer(
            sslCtx, host, port, isStream, isProxy, acceptHeader != null && acceptHeader.equalsIgnoreCase("text/event-stream"), contextConsumer
         )
      );
      return this.doConnect(localBootstrap, host, port);
   }

   protected NioEventLoopGroup createEventLoopGroup(HttpClientConfiguration configuration, ThreadFactory threadFactory) {
      OptionalInt numOfThreads = configuration.getNumOfThreads();
      Optional<Class<? extends ThreadFactory>> threadFactoryType = configuration.getThreadFactory();
      boolean hasThreads = numOfThreads.isPresent();
      boolean hasFactory = threadFactoryType.isPresent();
      NioEventLoopGroup group;
      if (hasThreads && hasFactory) {
         group = new NioEventLoopGroup(numOfThreads.getAsInt(), InstantiationUtils.instantiate((Class<ThreadFactory>)threadFactoryType.get()));
      } else if (hasThreads) {
         if (threadFactory != null) {
            group = new NioEventLoopGroup(numOfThreads.getAsInt(), threadFactory);
         } else {
            group = new NioEventLoopGroup(numOfThreads.getAsInt());
         }
      } else if (threadFactory != null) {
         group = new NioEventLoopGroup(NettyThreadFactory.DEFAULT_EVENT_LOOP_THREADS, threadFactory);
      } else {
         group = new NioEventLoopGroup();
      }

      return group;
   }

   protected ChannelFuture doConnect(Bootstrap bootstrap, String host, int port) {
      return bootstrap.connect(host, port);
   }

   protected SslContext buildSslContext(URI uriObject) {
      SslContext sslCtx;
      if (isSecureScheme(uriObject.getScheme())) {
         sslCtx = this.sslContext;
         if (sslCtx == null && !this.configuration.getProxyAddress().isPresent()) {
            throw new HttpClientException("Cannot send HTTPS request. SSL is disabled");
         }
      } else {
         sslCtx = null;
      }

      return sslCtx;
   }

   protected void configureProxy(ChannelPipeline pipeline, Proxy proxy) {
      this.configureProxy(pipeline, proxy.type(), proxy.address());
   }

   protected void configureProxy(ChannelPipeline pipeline, Type proxyType, SocketAddress proxyAddress) {
      String username = (String)this.configuration.getProxyUsername().orElse(null);
      String password = (String)this.configuration.getProxyPassword().orElse(null);
      if (proxyAddress instanceof InetSocketAddress) {
         InetSocketAddress isa = (InetSocketAddress)proxyAddress;
         if (isa.isUnresolved()) {
            proxyAddress = new InetSocketAddress(isa.getHostString(), isa.getPort());
         }
      }

      if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
         switch(proxyType) {
            case HTTP:
               pipeline.addLast("http-proxy", new HttpProxyHandler(proxyAddress, username, password));
               break;
            case SOCKS:
               pipeline.addLast("socks5-proxy", new Socks5ProxyHandler(proxyAddress, username, password));
         }
      } else {
         switch(proxyType) {
            case HTTP:
               pipeline.addLast("http-proxy", new HttpProxyHandler(proxyAddress));
               break;
            case SOCKS:
               pipeline.addLast("socks5-proxy", new Socks5ProxyHandler(proxyAddress));
         }
      }

   }

   private <I, O, R extends HttpResponse<O>> Publisher<R> applyFilterToResponsePublisher(
      HttpRequest<?> parentRequest, HttpRequest<I> request, URI requestURI, AtomicReference<HttpRequest<?>> requestWrapper, Publisher<R> responsePublisher
   ) {
      if (request instanceof MutableHttpRequest) {
         ((MutableHttpRequest)request).uri(requestURI);
         List<HttpClientFilter> filters = this.filterResolver.resolveFilters(request, this.clientFilterEntries);
         if (parentRequest != null) {
            filters.add(new ClientServerContextFilter(parentRequest));
         }

         OrderUtil.reverseSort(filters);
         Publisher<R> finalResponsePublisher = responsePublisher;
         filters.add((HttpClientFilter)(req, chain) -> finalResponsePublisher);
         ClientFilterChain filterChain = this.buildChain(requestWrapper, filters);
         if (parentRequest != null) {
            responsePublisher = ServerRequestContext.with(
               parentRequest,
               (Supplier<Publisher<R>>)(() -> {
                  try {
                     return Flux.from(((HttpClientFilter)filters.get(0)).doFilter(request, filterChain))
                        .contextWrite(ctx -> ctx.put("micronaut.http.server.request", parentRequest));
                  } catch (Throwable var5x) {
                     return Flux.error(var5x);
                  }
               })
            );
         } else {
            try {
               responsePublisher = ((HttpClientFilter)filters.get(0)).doFilter(request, filterChain);
            } catch (Throwable var10) {
               responsePublisher = Flux.error(var10);
            }
         }
      }

      return responsePublisher;
   }

   protected DefaultHttpClient.NettyRequestWriter buildNettyRequest(
      MutableHttpRequest request,
      URI requestURI,
      MediaType requestContentType,
      boolean permitsBody,
      @Nullable Argument<?> bodyType,
      Consumer<? super Throwable> onError,
      boolean closeChannelAfterWrite
   ) throws HttpPostRequestEncoder.ErrorDataEncoderException {
      HttpPostRequestEncoder postRequestEncoder = null;
      io.netty.handler.codec.http.HttpRequest nettyRequest;
      if (permitsBody) {
         Optional body = request.getBody();
         boolean hasBody = body.isPresent();
         if (requestContentType.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE) && hasBody) {
            Object bodyValue = body.get();
            if (bodyValue instanceof CharSequence) {
               ByteBuf byteBuf = this.charSequenceToByteBuf((CharSequence)bodyValue, requestContentType);
               request.body(byteBuf);
               nettyRequest = NettyHttpRequestBuilder.toHttpRequest(request);
            } else {
               postRequestEncoder = this.buildFormDataRequest(request, bodyValue);
               nettyRequest = postRequestEncoder.finalizeRequest();
            }
         } else if (requestContentType.equals(MediaType.MULTIPART_FORM_DATA_TYPE) && hasBody) {
            Object bodyValue = body.get();
            postRequestEncoder = this.buildMultipartRequest(request, bodyValue);
            nettyRequest = postRequestEncoder.finalizeRequest();
         } else {
            ByteBuf bodyContent = null;
            if (hasBody) {
               Object bodyValue = body.get();
               if (Publishers.isConvertibleToPublisher(bodyValue)) {
                  boolean isSingle = Publishers.isSingle(bodyValue.getClass());
                  Publisher<?> publisher = (Publisher)ConversionService.SHARED
                     .convert(bodyValue, Publisher.class)
                     .orElseThrow(() -> new IllegalArgumentException("Unconvertible reactive type: " + bodyValue));
                  Flux<HttpContent> requestBodyPublisher = Flux.from(publisher)
                     .map(
                        o -> {
                           if (o instanceof CharSequence) {
                              ByteBuf var9x = Unpooled.copiedBuffer((CharSequence)o, (Charset)requestContentType.getCharset().orElse(StandardCharsets.UTF_8));
                              if (this.log.isTraceEnabled()) {
                                 this.traceChunk(var9x);
                              }
      
                              return new DefaultHttpContent(var9x);
                           } else if (o instanceof ByteBuf) {
                              ByteBuf var8x = (ByteBuf)o;
                              if (this.log.isTraceEnabled()) {
                                 this.log.trace("Sending Bytes Chunk. Length: {}", var8x.readableBytes());
                              }
      
                              return new DefaultHttpContent(var8x);
                           } else if (o instanceof byte[]) {
                              byte[] var7x = (byte[])o;
                              if (this.log.isTraceEnabled()) {
                                 this.log.trace("Sending Bytes Chunk. Length: {}", var7x.length);
                              }
      
                              return new DefaultHttpContent(Unpooled.wrappedBuffer(var7x));
                           } else if (o instanceof ByteBuffer) {
                              ByteBuffer<?> var6x = (ByteBuffer)o;
                              Object var10x = var6x.asNativeBuffer();
                              if (this.log.isTraceEnabled()) {
                                 this.log.trace("Sending Bytes Chunk. Length: {}", var6x.readableBytes());
                              }
      
                              return var10x instanceof ByteBuf
                                 ? new DefaultHttpContent((ByteBuf)var10x)
                                 : new DefaultHttpContent(Unpooled.wrappedBuffer(var6x.toByteArray()));
                           } else {
                              if (this.mediaTypeCodecRegistry != null) {
                                 Optional<MediaTypeCodec> registeredCodecx = this.mediaTypeCodecRegistry.findCodec(requestContentType);
                                 ByteBuf encoded = (ByteBuf)registeredCodecx.map(
                                       codec -> bodyType != null && bodyType.isInstance(o)
                                             ? codec.encode(bodyType, o, this.byteBufferFactory).asNativeBuffer()
                                             : codec.encode(o, this.byteBufferFactory).asNativeBuffer()
                                    )
                                    .orElse(null);
                                 if (encoded != null) {
                                    if (this.log.isTraceEnabled()) {
                                       this.traceChunk(encoded);
                                    }
      
                                    return new DefaultHttpContent(encoded);
                                 }
                              }
      
                              throw new CodecException("Cannot encode value [" + o + "]. No possible encoders found");
                           }
                        }
                     );
                  if (!isSingle && MediaType.APPLICATION_JSON_TYPE.equals(requestContentType)) {
                     requestBodyPublisher = JsonSubscriber.lift(requestBodyPublisher);
                  }

                  requestBodyPublisher = requestBodyPublisher.doOnError(onError);
                  request.body(requestBodyPublisher);
                  nettyRequest = NettyHttpRequestBuilder.toHttpRequest(request);

                  try {
                     nettyRequest.setUri(requestURI.toURL().getFile());
                  } catch (MalformedURLException var22) {
                  }

                  return new DefaultHttpClient.NettyRequestWriter(requestURI.getScheme(), nettyRequest, null, closeChannelAfterWrite);
               }

               if (bodyValue instanceof CharSequence) {
                  bodyContent = this.charSequenceToByteBuf((CharSequence)bodyValue, requestContentType);
               } else if (this.mediaTypeCodecRegistry != null) {
                  Optional<MediaTypeCodec> registeredCodec = this.mediaTypeCodecRegistry.findCodec(requestContentType);
                  bodyContent = (ByteBuf)registeredCodec.map(
                        codec -> bodyType != null && bodyType.isInstance(bodyValue)
                              ? codec.encode(bodyType, bodyValue, this.byteBufferFactory).asNativeBuffer()
                              : codec.encode(bodyValue, this.byteBufferFactory).asNativeBuffer()
                     )
                     .orElse(null);
               }

               if (bodyContent == null) {
                  bodyContent = (ByteBuf)ConversionService.SHARED
                     .convert(bodyValue, ByteBuf.class)
                     .orElseThrow(
                        () -> new HttpClientException(
                              "Body ["
                                 + bodyValue
                                 + "] cannot be encoded to content type ["
                                 + requestContentType
                                 + "]. No possible codecs or converters found."
                           )
                     );
               }
            }

            request.body(bodyContent);

            try {
               nettyRequest = NettyHttpRequestBuilder.toHttpRequest(request);
            } finally {
               request.body(body.orElse(null));
            }
         }
      } else {
         nettyRequest = NettyHttpRequestBuilder.toHttpRequest(request);
      }

      try {
         nettyRequest.setUri(requestURI.toURL().getFile());
      } catch (MalformedURLException var23) {
      }

      return new DefaultHttpClient.NettyRequestWriter(requestURI.getScheme(), nettyRequest, postRequestEncoder, closeChannelAfterWrite);
   }

   protected void configureHttp2Ssl(
      DefaultHttpClient.HttpClientInitializer httpClientInitializer,
      @NonNull SocketChannel ch,
      @NonNull SslContext sslCtx,
      String host,
      int port,
      HttpToHttp2ConnectionHandler connectionHandler
   ) {
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc(), host, port));
      pipeline.addLast("http2-protocol-negotiator", new ApplicationProtocolNegotiationHandler("h2") {
         @Override
         public void handlerRemoved(ChannelHandlerContext ctx) {
            Consumer<ChannelHandlerContext> contextConsumer = httpClientInitializer.contextConsumer;
            if (contextConsumer != null) {
               contextConsumer.accept(ctx);
            }

         }

         @Override
         protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
            if ("h2".equals(protocol)) {
               ChannelPipeline p = ctx.pipeline();
               if (httpClientInitializer.stream) {
                  ctx.channel().config().setAutoRead(false);
               }

               p.addLast("http2-settings", DefaultHttpClient.this.new Http2SettingsHandler(ch.newPromise()));
               httpClientInitializer.addEventStreamHandlerIfNecessary(p);
               httpClientInitializer.addFinalHandler(p);

               for(ChannelPipelineListener pipelineListener : DefaultHttpClient.this.pipelineListeners) {
                  pipelineListener.onConnect(p);
               }
            } else {
               if (!"http/1.1".equals(protocol)) {
                  ctx.close();
                  throw new HttpClientException("Unknown Protocol: " + protocol);
               }

               ChannelPipeline p = ctx.pipeline();
               httpClientInitializer.addHttp1Handlers(p);
            }

         }
      });
      pipeline.addLast("http2-connection", connectionHandler);
   }

   protected void configureHttp2ClearText(
      DefaultHttpClient.HttpClientInitializer httpClientInitializer, @NonNull SocketChannel ch, @NonNull HttpToHttp2ConnectionHandler connectionHandler
   ) {
      HttpClientCodec sourceCodec = new HttpClientCodec();
      Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec("http2-connection", connectionHandler);
      HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, 65536);
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast("http-client-codec", sourceCodec);
      httpClientInitializer.settingsHandler = new DefaultHttpClient.Http2SettingsHandler(ch.newPromise());
      pipeline.addLast(upgradeHandler);
      pipeline.addLast("http2-upgrade-request", new DefaultHttpClient.UpgradeRequestHandler(httpClientInitializer) {
         @Override
         public void handlerRemoved(ChannelHandlerContext ctx) {
            Consumer<ChannelHandlerContext> contextConsumer = httpClientInitializer.contextConsumer;
            if (contextConsumer != null) {
               contextConsumer.accept(ctx);
            }

         }
      });
   }

   @NonNull
   protected HttpToHttp2ConnectionHandlerBuilder newHttp2ConnectionHandlerBuilder(
      @NonNull Http2Connection connection, @NonNull HttpClientConfiguration configuration, boolean stream
   ) {
      HttpToHttp2ConnectionHandlerBuilder builder = new HttpToHttp2ConnectionHandlerBuilder();
      builder.validateHeaders(true);
      Http2FrameListener http2ToHttpAdapter;
      if (!stream) {
         http2ToHttpAdapter = new InboundHttp2ToHttpAdapterBuilder(connection)
            .maxContentLength(configuration.getMaxContentLength())
            .validateHttpHeaders(true)
            .propagateSettings(true)
            .build();
      } else {
         http2ToHttpAdapter = new StreamingInboundHttp2ToHttpAdapter(connection, configuration.getMaxContentLength());
      }

      return builder.connection(connection).frameListener(new DelegatingDecompressorFrameListener(connection, http2ToHttpAdapter));
   }

   private Flux<MutableHttpResponse<Object>> readBodyOnError(@Nullable Argument<?> errorType, @NonNull Flux<MutableHttpResponse<Object>> publisher) {
      return errorType != null && errorType != HttpClient.DEFAULT_ERROR_TYPE
         ? publisher.onErrorResume(
            clientException -> {
               if (clientException instanceof HttpClientResponseException) {
                  HttpResponse<?> response = ((HttpClientResponseException)clientException).getResponse();
                  if (response instanceof NettyStreamedHttpResponse) {
                     return Mono.create(
                        emitter -> {
                           NettyStreamedHttpResponse<?> streamedResponse = (NettyStreamedHttpResponse)response;
                           final StreamedHttpResponse nettyResponse = streamedResponse.getNettyResponse();
                           nettyResponse.subscribe(
                              new Subscriber<HttpContent>() {
                                 final CompositeByteBuf buffer = DefaultHttpClient.this.byteBufferFactory.getNativeAllocator().compositeBuffer();
                                 Subscription s;
            
                                 @Override
                                 public void onSubscribe(Subscription s) {
                                    this.s = s;
                                    s.request(1L);
                                 }
            
                                 public void onNext(HttpContent httpContent) {
                                    this.buffer.addComponent(true, httpContent.content());
                                    this.s.request(1L);
                                 }
            
                                 @Override
                                 public void onError(Throwable t) {
                                    this.buffer.release();
                                    emitter.error(t);
                                 }
            
                                 @Override
                                 public void onComplete() {
                                    try {
                                       FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                                          nettyResponse.protocolVersion(),
                                          nettyResponse.status(),
                                          this.buffer,
                                          nettyResponse.headers(),
                                          new DefaultHttpHeaders(true)
                                       );
                                       FullNettyClientHttpResponse fullNettyClientHttpResponse = new FullNettyClientHttpResponse(
                                          fullHttpResponse,
                                          response.status(),
                                          DefaultHttpClient.this.mediaTypeCodecRegistry,
                                          DefaultHttpClient.this.byteBufferFactory,
                                          errorType,
                                          true
                                       );
                                       fullNettyClientHttpResponse.onComplete();
                                       emitter.error(
                                          new HttpClientResponseException(
                                             fullHttpResponse.status().reasonPhrase(), null, fullNettyClientHttpResponse, new HttpClientErrorDecoder() {
                                                @Override
                                                public Argument<?> getErrorType(MediaType mediaType) {
                                                   return errorType;
                                                }
                                             }
                                          )
                                       );
                                    } finally {
                                       this.buffer.release();
                                    }
            
                                 }
                              }
                           );
                        }
                     );
                  }
               }
      
               return Mono.error(clientException);
            }
         )
         : publisher;
   }

   private <I> Publisher<URI> resolveURI(HttpRequest<I> request, boolean includeContextPath) {
      URI requestURI = request.getUri();
      return this.loadBalancer == null
         ? Flux.error(new NoHostException("Request URI specifies no host to connect to"))
         : Flux.from(this.loadBalancer.select(this.getLoadBalancerDiscriminator())).map(server -> {
            Optional<String> authInfo = server.getMetadata().get("Authorization-Info", String.class);
            if (request instanceof MutableHttpRequest && authInfo.isPresent()) {
               ((MutableHttpRequest)request).getHeaders().auth((String)authInfo.get());
            }
   
            return server.resolve(includeContextPath ? this.prependContextPath(requestURI) : requestURI);
         });
   }

   private <I, O, E> void sendRequestThroughChannel(
      HttpRequest<I> finalRequest,
      Argument<O> bodyType,
      Argument<E> errorType,
      FluxSink<? super HttpResponse<O>> emitter,
      Channel channel,
      boolean secure,
      ChannelPool channelPool
   ) throws HttpPostRequestEncoder.ErrorDataEncoderException {
      URI requestURI = finalRequest.getUri();
      MediaType requestContentType = (MediaType)finalRequest.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
      boolean permitsBody = HttpMethod.permitsRequestBody(finalRequest.getMethod());
      MutableHttpRequest clientHttpRequest = (MutableHttpRequest)finalRequest;
      DefaultHttpClient.NettyRequestWriter requestWriter = this.buildNettyRequest(
         clientHttpRequest, requestURI, requestContentType, permitsBody, bodyType, throwable -> {
            if (!emitter.isCancelled()) {
               emitter.error(throwable);
            }
   
         }, true
      );
      io.netty.handler.codec.http.HttpRequest nettyRequest = requestWriter.getNettyRequest();
      this.prepareHttpHeaders(requestURI, finalRequest, nettyRequest, permitsBody, this.poolMap == null);
      if (this.log.isDebugEnabled()) {
         this.debugRequest(requestURI, nettyRequest);
      }

      if (this.log.isTraceEnabled()) {
         this.traceRequest(finalRequest, nettyRequest);
      }

      Promise<HttpResponse<O>> responsePromise = channel.eventLoop().newPromise();
      channel.pipeline()
         .addLast(
            "micronaut-full-http-response",
            new DefaultHttpClient.FullHttpResponseHandler<>(responsePromise, channelPool, secure, finalRequest, bodyType, errorType)
         );
      Publisher<HttpResponse<O>> publisher = new NettyFuturePublisher<>(responsePromise, true);
      if (bodyType != null && bodyType.isVoid()) {
         publisher = Flux.from(publisher).filter(r -> false);
      }

      publisher.subscribe(new ForwardingSubscriber<>(emitter));
      requestWriter.writeAndClose(channel, channelPool, emitter);
   }

   private Flux<MutableHttpResponse<Object>> streamRequestThroughChannel(
      HttpRequest<?> parentRequest, HttpRequest<?> request, Channel channel, boolean failOnError
   ) {
      return Flux.create(sink -> {
         try {
            this.streamRequestThroughChannel0(parentRequest, request, sink, channel);
         } catch (HttpPostRequestEncoder.ErrorDataEncoderException var6) {
            sink.error(var6);
         }

      }).flatMap(resp -> this.handleStreamHttpError(resp, failOnError));
   }

   private <R extends HttpResponse<?>> Flux<R> handleStreamHttpError(R response, boolean failOnError) {
      boolean errorStatus = response.code() >= 400;
      return errorStatus && failOnError ? Flux.error(new HttpClientResponseException(response.getStatus().getReason(), response)) : Flux.just(response);
   }

   private void streamRequestThroughChannel0(HttpRequest<?> parentRequest, final HttpRequest<?> finalRequest, FluxSink emitter, Channel channel) throws HttpPostRequestEncoder.ErrorDataEncoderException {
      DefaultHttpClient.NettyRequestWriter requestWriter = this.prepareRequest(finalRequest, finalRequest.getUri(), emitter, false);
      io.netty.handler.codec.http.HttpRequest nettyRequest = requestWriter.getNettyRequest();
      Promise<HttpResponse<?>> responsePromise = channel.eventLoop().newPromise();
      ChannelPipeline pipeline = channel.pipeline();
      pipeline.addLast("micronaut-http-response-full", new DefaultHttpClient.StreamFullHttpResponseHandler(responsePromise, parentRequest, finalRequest));
      pipeline.addLast("micronaut-http-response-stream", new DefaultHttpClient.StreamStreamHttpResponseHandler(responsePromise, parentRequest, finalRequest));
      if (this.log.isDebugEnabled()) {
         this.debugRequest(finalRequest.getUri(), nettyRequest);
      }

      if (this.log.isTraceEnabled()) {
         this.traceRequest(finalRequest, nettyRequest);
      }

      requestWriter.writeAndClose(channel, null, emitter);
      responsePromise.addListener(future -> {
         if (future.isSuccess()) {
            emitter.next(future.getNow());
            emitter.complete();
         } else {
            emitter.error(future.cause());
         }

      });
   }

   private ByteBuf charSequenceToByteBuf(CharSequence bodyValue, MediaType requestContentType) {
      return this.byteBufferFactory
         .copiedBuffer(bodyValue.toString().getBytes((Charset)requestContentType.getCharset().orElse(this.defaultCharset)))
         .asNativeBuffer();
   }

   private String getHostHeader(URI requestURI) {
      DefaultHttpClient.RequestKey requestKey = new DefaultHttpClient.RequestKey(requestURI);
      StringBuilder host = new StringBuilder(requestKey.getHost());
      int port = requestKey.getPort();
      if (port > -1 && port != 80 && port != 443) {
         host.append(":").append(port);
      }

      return host.toString();
   }

   private <I> void prepareHttpHeaders(
      URI requestURI, HttpRequest<I> request, io.netty.handler.codec.http.HttpRequest nettyRequest, boolean permitsBody, boolean closeConnection
   ) {
      HttpHeaders headers = nettyRequest.headers();
      if (!headers.contains(HttpHeaderNames.HOST)) {
         headers.set(HttpHeaderNames.HOST, this.getHostHeader(requestURI));
      }

      if (this.httpVersion != HttpVersion.HTTP_2_0) {
         if (closeConnection) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
         } else {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
         }
      }

      if (permitsBody) {
         Optional<I> body = request.getBody();
         if (body.isPresent()) {
            if (!headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
               MediaType mediaType = (MediaType)request.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
               headers.set(HttpHeaderNames.CONTENT_TYPE, mediaType);
            }

            if (nettyRequest instanceof FullHttpRequest) {
               FullHttpRequest fullHttpRequest = (FullHttpRequest)nettyRequest;
               headers.set(HttpHeaderNames.CONTENT_LENGTH, fullHttpRequest.content().readableBytes());
            } else if (!headers.contains(HttpHeaderNames.CONTENT_LENGTH) && !headers.contains(HttpHeaderNames.TRANSFER_ENCODING)) {
               headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            }
         } else if (!(nettyRequest instanceof StreamedHttpRequest)) {
            headers.set(HttpHeaderNames.CONTENT_LENGTH, 0);
         }
      }

   }

   private boolean discardH2cStream(HttpMessage message) {
      if (this.httpVersion == HttpVersion.HTTP_2_0) {
         int streamId = message.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), -1);
         if (streamId == 1) {
            if (this.log.isDebugEnabled()) {
               this.log.debug("Received response on HTTP2 stream 1, the stream used to respond to the initial upgrade request. Ignoring.");
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void addReadTimeoutHandler(ChannelPipeline pipeline) {
      if (this.readTimeoutMillis != null) {
         if (this.httpVersion == HttpVersion.HTTP_2_0) {
            DefaultHttpClient.Http2SettingsHandler settingsHandler = (DefaultHttpClient.Http2SettingsHandler)pipeline.get("http2-settings");
            if (settingsHandler != null) {
               this.addInstrumentedListener(settingsHandler.promise, future -> {
                  if (future.isSuccess()) {
                     pipeline.addBefore("http2-connection", "read-timeout", new ReadTimeoutHandler(this.readTimeoutMillis, TimeUnit.MILLISECONDS));
                  }

               });
            } else {
               pipeline.addBefore("http2-connection", "read-timeout", new ReadTimeoutHandler(this.readTimeoutMillis, TimeUnit.MILLISECONDS));
            }
         } else {
            pipeline.addBefore("http-client-codec", "read-timeout", new ReadTimeoutHandler(this.readTimeoutMillis, TimeUnit.MILLISECONDS));
         }
      }

   }

   private void removeReadTimeoutHandler(ChannelPipeline pipeline) {
      if (this.readTimeoutMillis != null && pipeline.context("read-timeout") != null) {
         pipeline.remove("read-timeout");
      }

   }

   private ClientFilterChain buildChain(AtomicReference<HttpRequest<?>> requestWrapper, List<HttpClientFilter> filters) {
      final AtomicInteger integer = new AtomicInteger();
      final int len = filters.size();
      return new ClientFilterChain() {
         @Override
         public Publisher<? extends HttpResponse<?>> proceed(MutableHttpRequest<?> request) {
            int pos = integer.incrementAndGet();
            if (pos > len) {
               throw new IllegalStateException(
                  "The FilterChain.proceed(..) method should be invoked exactly once per filter execution. The method has instead been invoked multiple times by an erroneous filter definition."
               );
            } else {
               HttpClientFilter httpFilter = (HttpClientFilter)filters.get(pos);

               try {
                  return httpFilter.doFilter((HttpRequest<?>)requestWrapper.getAndSet(request), this);
               } catch (Throwable var5) {
                  return Flux.error(var5);
               }
            }
         }
      };
   }

   private HttpPostRequestEncoder buildFormDataRequest(MutableHttpRequest clientHttpRequest, Object bodyValue) throws HttpPostRequestEncoder.ErrorDataEncoderException {
      HttpPostRequestEncoder postRequestEncoder = new HttpPostRequestEncoder(NettyHttpRequestBuilder.toHttpRequest(clientHttpRequest), false);
      Map<String, Object> formData;
      if (bodyValue instanceof Map) {
         formData = (Map)bodyValue;
      } else {
         formData = BeanMap.of(bodyValue);
      }

      for(Entry<String, Object> entry : formData.entrySet()) {
         Object value = entry.getValue();
         if (value != null) {
            if (value instanceof Collection) {
               for(Object val : (Collection)value) {
                  this.addBodyAttribute(postRequestEncoder, (String)entry.getKey(), val);
               }
            } else {
               this.addBodyAttribute(postRequestEncoder, (String)entry.getKey(), value);
            }
         }
      }

      return postRequestEncoder;
   }

   private void addBodyAttribute(HttpPostRequestEncoder postRequestEncoder, String key, Object value) throws HttpPostRequestEncoder.ErrorDataEncoderException {
      Optional<String> converted = ConversionService.SHARED.convert(value, String.class);
      if (converted.isPresent()) {
         postRequestEncoder.addBodyAttribute(key, (String)converted.get());
      }

   }

   private HttpPostRequestEncoder buildMultipartRequest(MutableHttpRequest clientHttpRequest, Object bodyValue) throws HttpPostRequestEncoder.ErrorDataEncoderException {
      final HttpDataFactory factory = new DefaultHttpDataFactory(16384L);
      final io.netty.handler.codec.http.HttpRequest request = NettyHttpRequestBuilder.toHttpRequest(clientHttpRequest);
      HttpPostRequestEncoder postRequestEncoder = new HttpPostRequestEncoder(
         factory, request, true, CharsetUtil.UTF_8, HttpPostRequestEncoder.EncoderMode.HTML5
      );
      if (bodyValue instanceof MultipartBody.Builder) {
         bodyValue = ((MultipartBody.Builder)bodyValue).build();
      }

      if (bodyValue instanceof MultipartBody) {
         MultipartBody multipartBody = (MultipartBody)bodyValue;
         postRequestEncoder.setBodyHttpDatas(
            multipartBody.getData(
               new MultipartDataFactory<InterfaceHttpData>() {
                  @NonNull
                  public InterfaceHttpData createFileUpload(
                     @NonNull String name,
                     @NonNull String filename,
                     @NonNull MediaType contentType,
                     @Nullable String encoding,
                     @Nullable Charset charset,
                     long length
                  ) {
                     return factory.createFileUpload(request, name, filename, contentType.toString(), encoding, charset, length);
                  }
      
                  @NonNull
                  public InterfaceHttpData createAttribute(@NonNull String name, @NonNull String value) {
                     return factory.createAttribute(request, name, value);
                  }
      
                  public void setContent(InterfaceHttpData fileUploadObject, Object content) throws IOException {
                     if (fileUploadObject instanceof FileUpload) {
                        FileUpload fu = (FileUpload)fileUploadObject;
                        if (content instanceof InputStream) {
                           fu.setContent((InputStream)content);
                        } else if (content instanceof File) {
                           fu.setContent((File)content);
                        } else if (content instanceof byte[]) {
                           ByteBuf buffer = Unpooled.wrappedBuffer((byte[])content);
                           fu.setContent(buffer);
                        }
                     }
      
                  }
               }
            )
         );
         return postRequestEncoder;
      } else {
         throw new MultipartException(String.format("The type %s is not a supported type for a multipart request body", bodyValue.getClass().getName()));
      }
   }

   private void debugRequest(URI requestURI, io.netty.handler.codec.http.HttpRequest nettyRequest) {
      this.log.debug("Sending HTTP {} to {}", nettyRequest.method(), requestURI.toString());
   }

   private void traceRequest(HttpRequest<?> request, io.netty.handler.codec.http.HttpRequest nettyRequest) {
      HttpHeaders headers = nettyRequest.headers();
      this.traceHeaders(headers);
      if (HttpMethod.permitsRequestBody(request.getMethod()) && request.getBody().isPresent() && nettyRequest instanceof FullHttpRequest) {
         FullHttpRequest fullHttpRequest = (FullHttpRequest)nettyRequest;
         ByteBuf content = fullHttpRequest.content();
         if (this.log.isTraceEnabled()) {
            this.traceBody("Request", content);
         }
      }

   }

   private void traceBody(String type, ByteBuf content) {
      this.log.trace(type + " Body");
      this.log.trace("----");
      this.log.trace(content.toString(this.defaultCharset));
      this.log.trace("----");
   }

   private void traceChunk(ByteBuf content) {
      this.log.trace("Sending Chunk");
      this.log.trace("----");
      this.log.trace(content.toString(this.defaultCharset));
      this.log.trace("----");
   }

   private void traceHeaders(HttpHeaders headers) {
      for(String name : headers.names()) {
         List<String> all = headers.getAll(name);
         if (all.size() > 1) {
            for(String value : all) {
               this.log.trace("{}: {}", name, value);
            }
         } else if (!all.isEmpty()) {
            this.log.trace("{}: {}", name, all.get(0));
         }
      }

   }

   private static MediaTypeCodecRegistry createDefaultMediaTypeRegistry() {
      JsonMapper mapper = new JacksonDatabindMapper();
      ApplicationConfiguration configuration = new ApplicationConfiguration();
      return MediaTypeCodecRegistry.of(new JsonMediaTypeCodec(mapper, configuration, null), new JsonStreamMediaTypeCodec(mapper, configuration, null));
   }

   private <I> DefaultHttpClient.NettyRequestWriter prepareRequest(
      HttpRequest<I> request, URI requestURI, FluxSink<HttpResponse<Object>> emitter, boolean closeChannelAfterWrite
   ) throws HttpPostRequestEncoder.ErrorDataEncoderException {
      MediaType requestContentType = (MediaType)request.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
      boolean permitsBody = HttpMethod.permitsRequestBody(request.getMethod());
      if (!(request instanceof MutableHttpRequest)) {
         throw new IllegalArgumentException("A MutableHttpRequest is required");
      } else {
         MutableHttpRequest clientHttpRequest = (MutableHttpRequest)request;
         DefaultHttpClient.NettyRequestWriter requestWriter = this.buildNettyRequest(
            clientHttpRequest, requestURI, requestContentType, permitsBody, null, throwable -> {
               if (!emitter.isCancelled()) {
                  emitter.error(throwable);
               }
   
            }, closeChannelAfterWrite
         );
         io.netty.handler.codec.http.HttpRequest nettyRequest = requestWriter.getNettyRequest();
         this.prepareHttpHeaders(requestURI, request, nettyRequest, permitsBody, true);
         return requestWriter;
      }
   }

   private Disposable buildDisposableChannel(ChannelFuture channelFuture) {
      return new Disposable() {
         private AtomicBoolean disposed = new AtomicBoolean(false);

         @Override
         public void dispose() {
            if (this.disposed.compareAndSet(false, true)) {
               Channel channel = channelFuture.channel();
               if (channel.isOpen()) {
                  DefaultHttpClient.this.closeChannelAsync(channel);
               }
            }

         }

         @Override
         public boolean isDisposed() {
            return this.disposed.get();
         }
      };
   }

   private AbstractChannelPoolHandler newPoolHandler(DefaultHttpClient.RequestKey key) {
      return new AbstractChannelPoolHandler() {
         @Override
         public void channelCreated(Channel ch) {
            ch.pipeline()
               .addLast(
                  "http-client-init",
                  new DefaultHttpClient.HttpClientInitializer(
                     key.isSecure() ? DefaultHttpClient.this.sslContext : null, key.getHost(), key.getPort(), false, false, false, null
                  ) {
                     @Override
                     protected void addFinalHandler(ChannelPipeline pipeline) {
                     }
                  }
               );
            if (DefaultHttpClient.this.connectionTimeAliveMillis != null) {
               ch.pipeline().addLast("connect-ttl", new ConnectTTLHandler(DefaultHttpClient.this.connectionTimeAliveMillis));
            }

         }

         @Override
         public void channelReleased(Channel ch) {
            Duration idleTimeout = (Duration)DefaultHttpClient.this.configuration.getConnectionPoolIdleTimeout().orElse(Duration.ofNanos(0L));
            ChannelPipeline pipeline = ch.pipeline();
            if (ch.isOpen()) {
               ch.config().setAutoRead(true);
               pipeline.addLast(IdlingConnectionHandler.INSTANCE);
               if (idleTimeout.toNanos() > 0L) {
                  pipeline.addLast("idle-state", new IdleStateHandler(idleTimeout.toNanos(), idleTimeout.toNanos(), 0L, TimeUnit.NANOSECONDS));
                  pipeline.addLast(IdleTimeoutHandler.INSTANCE);
               }
            }

            if (DefaultHttpClient.this.connectionTimeAliveMillis != null) {
               boolean shouldCloseOnRelease = Boolean.TRUE.equals(ch.attr(ConnectTTLHandler.RELEASE_CHANNEL).get());
               if (shouldCloseOnRelease && ch.isOpen() && !ch.eventLoop().isShuttingDown()) {
                  ch.close();
               }
            }

            DefaultHttpClient.this.removeReadTimeoutHandler(pipeline);
         }

         @Override
         public void channelAcquired(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            if (pipeline.context(IdlingConnectionHandler.INSTANCE) != null) {
               pipeline.remove(IdlingConnectionHandler.INSTANCE);
            }

            if (pipeline.context("idle-state") != null) {
               pipeline.remove("idle-state");
            }

            if (pipeline.context(IdleTimeoutHandler.INSTANCE) != null) {
               pipeline.remove(IdleTimeoutHandler.INSTANCE);
            }

         }
      };
   }

   private <V, C extends Future<V>> Future<V> addInstrumentedListener(Future<V> channelFuture, GenericFutureListener<C> listener) {
      InvocationInstrumenter instrumenter = this.combineFactories();
      return channelFuture.addListener(f -> {
         try (Instrumentation ignored = instrumenter.newInstrumentation()) {
            listener.operationComplete((C)f);
         }

      });
   }

   @NonNull
   private InvocationInstrumenter combineFactories() {
      return CollectionUtils.isEmpty(this.invocationInstrumenterFactories)
         ? InvocationInstrumenter.NOOP
         : InvocationInstrumenter.combine(
            (Collection<InvocationInstrumenter>)this.invocationInstrumenterFactories
               .stream()
               .map(InvocationInstrumenterFactory::newInvocationInstrumenter)
               .filter(Objects::nonNull)
               .collect(Collectors.toList())
         );
   }

   private static boolean isSecureScheme(String scheme) {
      return "https".equalsIgnoreCase(scheme) || "wss".equalsIgnoreCase(scheme);
   }

   static {
      REDIRECT_HEADER_BLOCKLIST.add(HttpHeaderNames.HOST, "");
      REDIRECT_HEADER_BLOCKLIST.add(HttpHeaderNames.CONTENT_TYPE, "");
      REDIRECT_HEADER_BLOCKLIST.add(HttpHeaderNames.CONTENT_LENGTH, "");
      REDIRECT_HEADER_BLOCKLIST.add(HttpHeaderNames.TRANSFER_ENCODING, "");
      REDIRECT_HEADER_BLOCKLIST.add(HttpHeaderNames.CONNECTION, "");
   }

   private abstract class BaseHttpResponseHandler<R extends io.netty.handler.codec.http.HttpResponse, O> extends SimpleChannelInboundHandlerInstrumented<R> {
      private final Promise<O> responsePromise;
      private final HttpRequest<?> parentRequest;
      private final HttpRequest<?> finalRequest;

      public BaseHttpResponseHandler(Promise<O> responsePromise, HttpRequest<?> parentRequest, HttpRequest<?> finalRequest) {
         super(DefaultHttpClient.this.combineFactories());
         this.responsePromise = responsePromise;
         this.parentRequest = parentRequest;
         this.finalRequest = finalRequest;
      }

      @Override
      public abstract boolean acceptInboundMessage(Object msg);

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
         String message = cause.getMessage();
         if (message == null) {
            message = cause.getClass().getSimpleName();
         }

         if (DefaultHttpClient.this.log.isTraceEnabled()) {
            DefaultHttpClient.this.log
               .trace("HTTP Client exception ({}) occurred for request : {} {}", message, this.finalRequest.getMethodName(), this.finalRequest.getUri());
         }

         HttpClientException result;
         if (cause instanceof TooLongFrameException) {
            result = new ContentLengthExceededException((long)DefaultHttpClient.this.configuration.getMaxContentLength());
         } else if (cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
            result = ReadTimeoutException.TIMEOUT_EXCEPTION;
         } else {
            result = new HttpClientException("Error occurred reading HTTP response: " + message, cause);
         }

         this.responsePromise.tryFailure(result);
      }

      protected void channelReadInstrumented(ChannelHandlerContext ctx, R msg) throws Exception {
         if (!this.responsePromise.isDone()) {
            if (DefaultHttpClient.this.log.isDebugEnabled()) {
               DefaultHttpClient.this.log.debug("Received response {} from {}", msg.status().code(), this.finalRequest.getUri());
            }

            int code = msg.status().code();
            HttpHeaders headers1 = msg.headers();
            if (code > 300 && code < 400 && DefaultHttpClient.this.configuration.isFollowRedirects() && headers1.contains(HttpHeaderNames.LOCATION)) {
               String location = headers1.get(HttpHeaderNames.LOCATION);
               MutableHttpRequest<Object> redirectRequest;
               if (code == 307) {
                  redirectRequest = HttpRequest.create(this.finalRequest.getMethod(), location);
                  this.finalRequest.getBody().ifPresent(redirectRequest::body);
               } else {
                  redirectRequest = HttpRequest.GET(location);
               }

               this.setRedirectHeaders(this.finalRequest, redirectRequest);
               Flux.from(DefaultHttpClient.this.resolveRedirectURI(this.parentRequest, redirectRequest))
                  .<O>flatMap(this.makeRedirectHandler(this.parentRequest, redirectRequest))
                  .subscribe(new NettyPromiseSubscriber<>(this.responsePromise));
            } else {
               HttpResponseStatus status = msg.status();
               int statusCode = status.code();

               HttpStatus httpStatus;
               try {
                  httpStatus = HttpStatus.valueOf(statusCode);
               } catch (IllegalArgumentException var9) {
                  this.responsePromise.tryFailure(var9);
                  return;
               }

               HttpHeaders headers = msg.headers();
               if (DefaultHttpClient.this.log.isTraceEnabled()) {
                  DefaultHttpClient.this.log
                     .trace(
                        "HTTP Client Response Received ({}) for Request: {} {}", msg.status(), this.finalRequest.getMethodName(), this.finalRequest.getUri()
                     );
                  DefaultHttpClient.this.traceHeaders(headers);
               }

               this.buildResponse(this.responsePromise, msg, httpStatus);
            }
         }
      }

      private void setRedirectHeaders(@Nullable HttpRequest<?> request, MutableHttpRequest<Object> redirectRequest) {
         if (request != null) {
            for(Entry<String, List<String>> originalHeader : request.getHeaders()) {
               if (!DefaultHttpClient.REDIRECT_HEADER_BLOCKLIST.contains((String)originalHeader.getKey())) {
                  List<String> originalHeaderValue = (List)originalHeader.getValue();
                  if (originalHeaderValue != null && !originalHeaderValue.isEmpty()) {
                     for(String value : originalHeaderValue) {
                        if (value != null) {
                           redirectRequest.header((CharSequence)originalHeader.getKey(), value);
                        }
                     }
                  }
               }
            }
         }

      }

      protected abstract Function<URI, Publisher<? extends O>> makeRedirectHandler(HttpRequest<?> parentRequest, MutableHttpRequest<Object> redirectRequest);

      protected abstract void buildResponse(Promise<? super O> promise, R msg, HttpStatus httpStatus);
   }

   private static class CurrentEvent {
      byte[] data;
      String id;
      String name;
      Duration retry;

      private CurrentEvent() {
      }
   }

   private class FullHttpResponseHandler<O> extends DefaultHttpClient.BaseHttpResponseHandler<FullHttpResponse, HttpResponse<O>> {
      private final boolean secure;
      private final Argument<O> bodyType;
      private final Argument<?> errorType;
      private final ChannelPool channelPool;
      private boolean keepAlive = true;

      public FullHttpResponseHandler(
         Promise<HttpResponse<O>> responsePromise, ChannelPool channelPool, boolean secure, HttpRequest<?> request, Argument<O> bodyType, Argument<?> errorType
      ) {
         super(responsePromise, request, request);
         this.secure = secure;
         this.bodyType = bodyType;
         this.errorType = errorType;
         this.channelPool = channelPool;
      }

      @Override
      public boolean acceptInboundMessage(Object msg) {
         return msg instanceof FullHttpResponse && (this.secure || !DefaultHttpClient.this.discardH2cStream((HttpMessage)msg));
      }

      @Override
      protected Function<URI, Publisher<? extends HttpResponse<O>>> makeRedirectHandler(
         HttpRequest<?> parentRequest, MutableHttpRequest<Object> redirectRequest
      ) {
         return uri -> DefaultHttpClient.this.exchangeImpl(uri, parentRequest, redirectRequest, this.bodyType, this.errorType);
      }

      protected void channelReadInstrumented(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullResponse) throws Exception {
         try {
            fullResponse.retain();
            super.channelReadInstrumented(channelHandlerContext, fullResponse);
         } finally {
            if (fullResponse.refCnt() > 1) {
               try {
                  ReferenceCountUtil.release(fullResponse);
               } catch (Exception var9) {
                  if (DefaultHttpClient.this.log.isDebugEnabled()) {
                     DefaultHttpClient.this.log.debug("Failed to release response: {}", fullResponse);
                  }
               }
            }

            if (!HttpUtil.isKeepAlive(fullResponse)) {
               this.keepAlive = false;
            }

            channelHandlerContext.pipeline().remove(this);
         }

      }

      protected void buildResponse(Promise<? super HttpResponse<O>> promise, FullHttpResponse msg, HttpStatus httpStatus) {
         try {
            if (DefaultHttpClient.this.log.isTraceEnabled()) {
               DefaultHttpClient.this.traceBody("Response", msg.content());
            }

            if (httpStatus == HttpStatus.NO_CONTENT) {
               msg.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
            }

            boolean convertBodyWithBodyType = httpStatus.getCode() < 400
               || !DefaultHttpClient.this.configuration.isExceptionOnErrorStatus() && this.bodyType.equalsType(this.errorType);
            FullNettyClientHttpResponse<O> response = new FullNettyClientHttpResponse<>(
               msg, httpStatus, DefaultHttpClient.this.mediaTypeCodecRegistry, DefaultHttpClient.this.byteBufferFactory, this.bodyType, convertBodyWithBodyType
            );
            if (convertBodyWithBodyType) {
               promise.trySuccess(response);
               response.onComplete();
            } else {
               try {
                  promise.tryFailure(this.makeErrorFromRequestBody(msg.status(), response));
                  response.onComplete();
               } catch (HttpClientResponseException var7) {
                  promise.tryFailure(var7);
                  response.onComplete();
               } catch (Exception var8) {
                  response.onComplete();
                  promise.tryFailure(this.makeErrorBodyParseError(msg, httpStatus, var8));
               }
            }
         } catch (HttpClientResponseException var9) {
            promise.tryFailure(var9);
         } catch (Exception var10) {
            this.makeNormalBodyParseError(msg, httpStatus, var10, cause -> {
               if (!promise.tryFailure(cause) && DefaultHttpClient.this.log.isWarnEnabled()) {
                  DefaultHttpClient.this.log.warn("Exception fired after handler completed: " + var10.getMessage(), var10);
               }

            });
         }

      }

      private HttpClientResponseException makeErrorFromRequestBody(HttpResponseStatus status, FullNettyClientHttpResponse<?> response) {
         return this.errorType != null && this.errorType != HttpClient.DEFAULT_ERROR_TYPE
            ? new HttpClientResponseException(status.reasonPhrase(), null, response, new HttpClientErrorDecoder() {
               @Override
               public Argument<?> getErrorType(MediaType mediaType) {
                  return FullHttpResponseHandler.this.errorType;
               }
            })
            : new HttpClientResponseException(status.reasonPhrase(), response);
      }

      private HttpClientResponseException makeErrorBodyParseError(FullHttpResponse fullResponse, HttpStatus httpStatus, Throwable t) {
         FullNettyClientHttpResponse<Object> errorResponse = new FullNettyClientHttpResponse<>(
            fullResponse, httpStatus, DefaultHttpClient.this.mediaTypeCodecRegistry, DefaultHttpClient.this.byteBufferFactory, null, false
         );
         errorResponse.onComplete();
         return new HttpClientResponseException("Error decoding HTTP error response body: " + t.getMessage(), t, errorResponse, null);
      }

      private void makeNormalBodyParseError(FullHttpResponse fullResponse, HttpStatus httpStatus, Throwable t, Consumer<HttpClientResponseException> forward) {
         FullNettyClientHttpResponse<Object> response = new FullNettyClientHttpResponse<>(
            fullResponse, httpStatus, DefaultHttpClient.this.mediaTypeCodecRegistry, DefaultHttpClient.this.byteBufferFactory, null, false
         );
         HttpClientResponseException clientResponseError = new HttpClientResponseException(
            "Error decoding HTTP response body: " + t.getMessage(), t, response, new HttpClientErrorDecoder() {
               @Override
               public Argument<?> getErrorType(MediaType mediaType) {
                  return FullHttpResponseHandler.this.errorType;
               }
            }
         );

         try {
            forward.accept(clientResponseError);
         } finally {
            response.onComplete();
         }

      }

      @Override
      public void handlerRemoved(ChannelHandlerContext ctx) {
         if (this.channelPool != null) {
            DefaultHttpClient.this.removeReadTimeoutHandler(ctx.pipeline());
            Channel ch = ctx.channel();
            if (!this.keepAlive) {
               ch.closeFuture().addListener(future -> this.channelPool.release(ch));
            } else {
               this.channelPool.release(ch);
            }
         } else {
            ctx.close();
         }

      }

      @Override
      public void handlerAdded(ChannelHandlerContext ctx) {
         DefaultHttpClient.this.addReadTimeoutHandler(ctx.pipeline());
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
         super.exceptionCaught(ctx, cause);
         this.keepAlive = false;
         ctx.pipeline().remove(this);
      }
   }

   private final class Http2SettingsHandler extends SimpleChannelInboundHandlerInstrumented<Http2Settings> {
      private final ChannelPromise promise;

      Http2SettingsHandler(ChannelPromise promise) {
         super(DefaultHttpClient.this.combineFactories());
         this.promise = promise;
      }

      protected void channelReadInstrumented(ChannelHandlerContext ctx, Http2Settings msg) {
         this.promise.setSuccess();
         ctx.pipeline().remove(this);
      }
   }

   protected class HttpClientInitializer extends ChannelInitializer<SocketChannel> {
      final SslContext sslContext;
      final String host;
      final int port;
      final boolean stream;
      final boolean proxy;
      final boolean acceptsEvents;
      DefaultHttpClient.Http2SettingsHandler settingsHandler;
      private final Consumer<ChannelHandlerContext> contextConsumer;

      protected HttpClientInitializer(
         SslContext sslContext, String host, int port, boolean stream, boolean proxy, boolean acceptsEvents, Consumer<ChannelHandlerContext> contextConsumer
      ) {
         this.sslContext = sslContext;
         this.stream = stream;
         this.host = host;
         this.port = port;
         this.proxy = proxy;
         this.acceptsEvents = acceptsEvents;
         this.contextConsumer = contextConsumer;
      }

      protected void initChannel(SocketChannel ch) {
         ChannelPipeline p = ch.pipeline();
         Proxy proxy = DefaultHttpClient.this.configuration.resolveProxy(this.sslContext != null, this.host, this.port);
         if (!Proxy.NO_PROXY.equals(proxy)) {
            DefaultHttpClient.this.configureProxy(p, proxy);
         }

         if (DefaultHttpClient.this.httpVersion == HttpVersion.HTTP_2_0) {
            Http2Connection connection = new DefaultHttp2Connection(false);
            HttpToHttp2ConnectionHandlerBuilder builder = DefaultHttpClient.this.newHttp2ConnectionHandlerBuilder(
               connection, DefaultHttpClient.this.configuration, this.stream
            );
            DefaultHttpClient.this.configuration.getLogLevel().ifPresent(logLevel -> {
               try {
                  LogLevel nettyLevel = LogLevel.valueOf(logLevel.name());
                  builder.frameLogger(new Http2FrameLogger(nettyLevel, DefaultHttpClient.class));
               } catch (IllegalArgumentException var3x) {
                  throw new HttpClientException("Unsupported log level: " + logLevel);
               }
            });
            HttpToHttp2ConnectionHandler connectionHandler = builder.build();
            if (this.sslContext != null) {
               DefaultHttpClient.this.configureHttp2Ssl(this, ch, this.sslContext, this.host, this.port, connectionHandler);
            } else {
               DefaultHttpClient.this.configureHttp2ClearText(this, ch, connectionHandler);
            }
         } else {
            if (this.stream) {
               ch.config().setAutoRead(false);
            }

            DefaultHttpClient.this.configuration.getLogLevel().ifPresent(logLevel -> {
               try {
                  LogLevel nettyLevel = LogLevel.valueOf(logLevel.name());
                  p.addLast(new LoggingHandler(DefaultHttpClient.class, nettyLevel));
               } catch (IllegalArgumentException var3x) {
                  throw new HttpClientException("Unsupported log level: " + logLevel);
               }
            });
            if (this.sslContext != null) {
               SslHandler sslHandler = this.sslContext.newHandler(ch.alloc(), this.host, this.port);
               sslHandler.setHandshakeTimeoutMillis(DefaultHttpClient.this.configuration.getSslConfiguration().getHandshakeTimeout().toMillis());
               p.addLast("ssl", sslHandler);
            }

            if (DefaultHttpClient.this.poolMap == null && this.stream) {
               Optional<Duration> readIdleTime = DefaultHttpClient.this.configuration.getReadIdleTimeout();
               if (readIdleTime.isPresent()) {
                  Duration duration = (Duration)readIdleTime.get();
                  if (!duration.isNegative()) {
                     p.addLast("idle-state", new IdleStateHandler(duration.toMillis(), duration.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS));
                  }
               }
            }

            this.addHttp1Handlers(p);
         }

      }

      private void addHttp1Handlers(ChannelPipeline p) {
         p.addLast("http-client-codec", new HttpClientCodec());
         p.addLast("http-decoder", new HttpContentDecompressor());
         int maxContentLength = DefaultHttpClient.this.configuration.getMaxContentLength();
         if (!this.stream) {
            p.addLast("http-aggregator", new HttpObjectAggregator(maxContentLength) {
               @Override
               protected void finishAggregation(FullHttpMessage aggregated) throws Exception {
                  if (!HttpUtil.isContentLengthSet(aggregated) && aggregated.content().readableBytes() > 0) {
                     super.finishAggregation(aggregated);
                  }

               }
            });
         }

         this.addEventStreamHandlerIfNecessary(p);
         this.addFinalHandler(p);

         for(ChannelPipelineListener pipelineListener : DefaultHttpClient.this.pipelineListeners) {
            pipelineListener.onConnect(p);
         }

      }

      private void addEventStreamHandlerIfNecessary(ChannelPipeline p) {
         if (this.acceptsEventStream() && !this.proxy) {
            p.addLast("micronaut-sse-event-stream", new LineBasedFrameDecoder(DefaultHttpClient.this.configuration.getMaxContentLength(), true, true) {
               @Override
               public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                  if (msg instanceof HttpContent) {
                     if (msg instanceof LastHttpContent) {
                        super.channelRead(ctx, msg);
                     } else {
                        Attribute<Http2Stream> streamKey = ctx.channel().attr(DefaultHttpClient.STREAM_KEY);
                        if (msg instanceof Http2Content) {
                           streamKey.set(((Http2Content)msg).stream());
                        }

                        try {
                           super.channelRead(ctx, ((HttpContent)msg).content());
                        } finally {
                           streamKey.set(null);
                        }
                     }
                  } else {
                     super.channelRead(ctx, msg);
                  }

               }
            });
            p.addLast("micronaut-sse-content", new SimpleChannelInboundHandlerInstrumented<ByteBuf>(DefaultHttpClient.this.combineFactories(), false) {
               @Override
               public boolean acceptInboundMessage(Object msg) {
                  return msg instanceof ByteBuf;
               }

               protected void channelReadInstrumented(ChannelHandlerContext ctx, ByteBuf msg) {
                  try {
                     Attribute<Http2Stream> streamKey = ctx.channel().attr(DefaultHttpClient.STREAM_KEY);
                     Http2Stream http2Stream = streamKey.get();
                     if (http2Stream != null) {
                        ctx.fireChannelRead(new DefaultHttp2Content(msg.copy(), http2Stream));
                     } else {
                        ctx.fireChannelRead(new DefaultHttpContent(msg.copy()));
                     }
                  } finally {
                     msg.release();
                  }

               }
            });
         }

      }

      protected void addFinalHandler(ChannelPipeline pipeline) {
         pipeline.addLast(
            "http-streams-codec",
            new HttpStreamsClientHandler() {
               @Override
               public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                  if (evt instanceof IdleStateEvent) {
                     ctx.close();
                  }
   
                  super.userEventTriggered(ctx, evt);
               }
   
               @Override
               protected boolean isValidInMessage(Object msg) {
                  return super.isValidInMessage(msg)
                     && (HttpClientInitializer.this.sslContext != null || !DefaultHttpClient.this.discardH2cStream((HttpMessage)msg));
               }
            }
         );
      }

      private boolean acceptsEventStream() {
         return this.acceptsEvents;
      }
   }

   protected class NettyRequestWriter {
      private final io.netty.handler.codec.http.HttpRequest nettyRequest;
      private final HttpPostRequestEncoder encoder;
      private final String scheme;
      private final boolean closeChannelAfterWrite;

      NettyRequestWriter(String scheme, io.netty.handler.codec.http.HttpRequest nettyRequest, HttpPostRequestEncoder encoder, boolean closeChannelAfterWrite) {
         this.nettyRequest = nettyRequest;
         this.encoder = encoder;
         this.scheme = scheme;
         this.closeChannelAfterWrite = closeChannelAfterWrite;
      }

      protected void writeAndClose(Channel channel, ChannelPool channelPool, FluxSink<?> emitter) {
         ChannelPipeline pipeline = channel.pipeline();
         if (DefaultHttpClient.this.httpVersion == HttpVersion.HTTP_2_0) {
            boolean isSecure = DefaultHttpClient.this.sslContext != null && DefaultHttpClient.isSecureScheme(this.scheme);
            if (isSecure) {
               this.nettyRequest.headers().add(AbstractNettyHttpRequest.HTTP2_SCHEME, HttpScheme.HTTPS);
            } else {
               this.nettyRequest.headers().add(AbstractNettyHttpRequest.HTTP2_SCHEME, HttpScheme.HTTP);
            }

            DefaultHttpClient.UpgradeRequestHandler upgradeRequestHandler = (DefaultHttpClient.UpgradeRequestHandler)pipeline.get("http2-upgrade-request");
            DefaultHttpClient.Http2SettingsHandler settingsHandler;
            if (upgradeRequestHandler != null) {
               settingsHandler = upgradeRequestHandler.getSettingsHandler();
            } else {
               settingsHandler = (DefaultHttpClient.Http2SettingsHandler)pipeline.get("http2-settings");
            }

            if (settingsHandler != null) {
               DefaultHttpClient.this.addInstrumentedListener(settingsHandler.promise, future -> {
                  if (future.isSuccess()) {
                     this.processRequestWrite(channel, channelPool, emitter, pipeline);
                  } else {
                     throw new HttpClientException("HTTP/2 clear text upgrade failed to complete", future.cause());
                  }
               });
               return;
            }
         }

         this.processRequestWrite(channel, channelPool, emitter, pipeline);
      }

      private void processRequestWrite(Channel channel, ChannelPool channelPool, FluxSink<?> emitter, ChannelPipeline pipeline) {
         ChannelFuture channelFuture;
         if (this.encoder != null && this.encoder.isChunked()) {
            channel.attr(AttributeKey.valueOf("chunk-writer")).set(true);
            pipeline.addAfter("http-streams-codec", "chunk-writer", new ChunkedWriteHandler());
            channel.write(this.nettyRequest);
            channelFuture = channel.writeAndFlush(this.encoder);
         } else {
            channelFuture = channel.writeAndFlush(this.nettyRequest);
         }

         if (channelPool != null) {
            this.closeChannelIfNecessary(channel, emitter, channelFuture, false);
         } else {
            this.closeChannelIfNecessary(channel, emitter, channelFuture, this.closeChannelAfterWrite);
         }

      }

      private void closeChannelIfNecessary(Channel channel, FluxSink<?> emitter, ChannelFuture channelFuture, boolean closeChannelAfterWrite) {
         DefaultHttpClient.this.addInstrumentedListener(channelFuture, f -> {
            try {
               if (!f.isSuccess()) {
                  if (!emitter.isCancelled()) {
                     emitter.error(f.cause());
                  }
               } else {
                  channel.read();
               }
            } finally {
               if (this.encoder != null) {
                  this.encoder.cleanFiles();
               }

               channel.attr(AttributeKey.valueOf("chunk-writer")).set(null);
               if (closeChannelAfterWrite) {
                  DefaultHttpClient.this.closeChannelAsync(channel);
               }

            }

         });
      }

      io.netty.handler.codec.http.HttpRequest getNettyRequest() {
         return this.nettyRequest;
      }
   }

   private static final class RequestKey {
      private final String host;
      private final int port;
      private final boolean secure;

      public RequestKey(URI requestURI) {
         this.secure = DefaultHttpClient.isSecureScheme(requestURI.getScheme());
         String host = requestURI.getHost();
         int port;
         if (host == null) {
            host = requestURI.getAuthority();
            if (host == null) {
               throw new NoHostException("URI specifies no host to connect to");
            }

            int i = host.indexOf(58);
            if (i > -1) {
               String portStr = host.substring(i + 1);
               host = host.substring(0, i);

               try {
                  port = Integer.parseInt(portStr);
               } catch (NumberFormatException var7) {
                  throw new HttpClientException("URI specifies an invalid port: " + portStr);
               }
            } else {
               port = requestURI.getPort() > -1 ? requestURI.getPort() : (this.secure ? 443 : 80);
            }
         } else {
            port = requestURI.getPort() > -1 ? requestURI.getPort() : (this.secure ? 443 : 80);
         }

         this.host = host;
         this.port = port;
      }

      public InetSocketAddress getRemoteAddress() {
         return InetSocketAddress.createUnresolved(this.host, this.port);
      }

      public boolean isSecure() {
         return this.secure;
      }

      public String getHost() {
         return this.host;
      }

      public int getPort() {
         return this.port;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultHttpClient.RequestKey that = (DefaultHttpClient.RequestKey)o;
            return this.port == that.port && this.secure == that.secure && Objects.equals(this.host, that.host);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.host, this.port, this.secure});
      }
   }

   private class StreamFullHttpResponseHandler extends DefaultHttpClient.BaseHttpResponseHandler<FullHttpResponse, HttpResponse<?>> {
      public StreamFullHttpResponseHandler(Promise<HttpResponse<?>> responsePromise, HttpRequest<?> parentRequest, HttpRequest<?> finalRequest) {
         super(responsePromise, parentRequest, finalRequest);
      }

      @Override
      public boolean acceptInboundMessage(Object msg) {
         return msg instanceof FullHttpResponse;
      }

      protected void buildResponse(Promise<? super HttpResponse<?>> promise, FullHttpResponse msg, HttpStatus httpStatus) {
         Publisher<HttpContent> bodyPublisher;
         if (msg.content() instanceof EmptyByteBuf) {
            bodyPublisher = Publishers.empty();
         } else {
            bodyPublisher = Publishers.just(new DefaultLastHttpContent(msg.content()));
         }

         DefaultStreamedHttpResponse nettyResponse = new DefaultStreamedHttpResponse(msg.protocolVersion(), msg.status(), msg.headers(), bodyPublisher);
         promise.trySuccess(new NettyStreamedHttpResponse(nettyResponse, httpStatus));
      }

      @Override
      public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
         super.handlerAdded(ctx);
         DefaultHttpClient.this.addReadTimeoutHandler(ctx.pipeline());
      }

      @Override
      public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
         super.handlerRemoved(ctx);
         DefaultHttpClient.this.removeReadTimeoutHandler(ctx.pipeline());
      }

      @Override
      protected Function<URI, Publisher<? extends HttpResponse<?>>> makeRedirectHandler(
         HttpRequest<?> parentRequest, MutableHttpRequest<Object> redirectRequest
      ) {
         return uri -> DefaultHttpClient.this.buildStreamExchange(parentRequest, redirectRequest, uri, null);
      }
   }

   private class StreamStreamHttpResponseHandler extends DefaultHttpClient.BaseHttpResponseHandler<StreamedHttpResponse, HttpResponse<?>> {
      public StreamStreamHttpResponseHandler(Promise<HttpResponse<?>> responsePromise, HttpRequest<?> parentRequest, HttpRequest<?> finalRequest) {
         super(responsePromise, parentRequest, finalRequest);
      }

      @Override
      public boolean acceptInboundMessage(Object msg) {
         return msg instanceof StreamedHttpResponse;
      }

      protected void buildResponse(Promise<? super HttpResponse<?>> promise, StreamedHttpResponse msg, HttpStatus httpStatus) {
         promise.trySuccess(new NettyStreamedHttpResponse(msg, httpStatus));
      }

      @Override
      protected Function<URI, Publisher<? extends HttpResponse<?>>> makeRedirectHandler(
         HttpRequest<?> parentRequest, MutableHttpRequest<Object> redirectRequest
      ) {
         return uri -> DefaultHttpClient.this.buildStreamExchange(parentRequest, redirectRequest, uri, null);
      }
   }

   @FunctionalInterface
   interface ThrowingBiConsumer<T1, T2> {
      void accept(T1 t1, T2 t2) throws Exception;
   }

   private class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {
      private final DefaultHttpClient.HttpClientInitializer initializer;
      private final DefaultHttpClient.Http2SettingsHandler settingsHandler;

      public UpgradeRequestHandler(DefaultHttpClient.HttpClientInitializer initializer) {
         this.initializer = initializer;
         this.settingsHandler = initializer.settingsHandler;
      }

      public DefaultHttpClient.Http2SettingsHandler getSettingsHandler() {
         return this.settingsHandler;
      }

      @Override
      public void channelActive(ChannelHandlerContext ctx) {
         ChannelPipeline pipeline = ctx.pipeline();
         pipeline.addLast("http2-settings", this.initializer.settingsHandler);
         DefaultFullHttpRequest upgradeRequest = new DefaultFullHttpRequest(
            io.netty.handler.codec.http.HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpMethod.GET, "/", Unpooled.EMPTY_BUFFER
         );
         InetSocketAddress remote = (InetSocketAddress)ctx.channel().remoteAddress();
         String hostString = remote.getHostString();
         if (hostString == null) {
            hostString = remote.getAddress().getHostAddress();
         }

         upgradeRequest.headers().set(HttpHeaderNames.HOST, hostString + ':' + remote.getPort());
         ctx.writeAndFlush(upgradeRequest);
         ctx.fireChannelActive();
         pipeline.remove(this);
         this.initializer.addFinalHandler(pipeline);
      }
   }
}
