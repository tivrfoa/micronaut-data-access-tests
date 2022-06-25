package io.micronaut.http.server.netty;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.netty.channel.EventLoopGroupConfiguration;
import io.micronaut.http.netty.channel.EventLoopGroupFactory;
import io.micronaut.http.netty.channel.EventLoopGroupRegistry;
import io.micronaut.http.netty.channel.converters.ChannelOptionFactory;
import io.micronaut.http.netty.channel.converters.DefaultChannelOptionFactory;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.server.netty.ssl.CertificateProvidedSslBuilder;
import io.micronaut.http.server.netty.ssl.SelfSignedSslBuilder;
import io.micronaut.http.server.netty.ssl.ServerSslBuilder;
import io.micronaut.http.server.netty.types.DefaultCustomizableResponseTypeHandlerRegistry;
import io.micronaut.http.server.netty.types.NettyCustomizableResponseTypeHandler;
import io.micronaut.http.server.netty.types.files.FileTypeHandler;
import io.micronaut.http.ssl.ServerSslConfiguration;
import io.micronaut.scheduling.executor.ExecutorSelector;
import io.micronaut.web.router.resource.StaticResourceResolver;
import io.micronaut.websocket.context.WebSocketBeanRegistry;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.ServerSocketChannel;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

@Factory
@Internal
@Bean(
   typed = {NettyEmbeddedServerFactory.class, DefaultNettyEmbeddedServerFactory.class}
)
public class DefaultNettyEmbeddedServerFactory implements NettyEmbeddedServerFactory, NettyEmbeddedServices {
   private final ApplicationContext applicationContext;
   private final RequestArgumentSatisfier requestArgumentSatisfier;
   private final RouteExecutor routeExecutor;
   private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
   private final StaticResourceResolver staticResourceResolver;
   private final ExecutorSelector executorSelector;
   private final ThreadFactory nettyThreadFactory;
   private final HttpCompressionStrategy httpCompressionStrategy;
   private final WebSocketBeanRegistry websocketBeanRegistry;
   private final EventLoopGroupFactory eventLoopGroupFactory;
   private final EventLoopGroupRegistry eventLoopGroupRegistry;
   private final Map<Class<?>, ApplicationEventPublisher<?>> cachedEventPublishers = new ConcurrentHashMap(5);
   @Nullable
   private ServerSslBuilder serverSslBuilder;
   @Nullable
   private ChannelOptionFactory channelOptionFactory;
   private List<ChannelOutboundHandler> outboundHandlers = Collections.emptyList();

   protected DefaultNettyEmbeddedServerFactory(
      ApplicationContext applicationContext,
      RouteExecutor routeExecutor,
      MediaTypeCodecRegistry mediaTypeCodecRegistry,
      StaticResourceResolver staticResourceResolver,
      @Named("netty") ThreadFactory nettyThreadFactory,
      HttpCompressionStrategy httpCompressionStrategy,
      EventLoopGroupFactory eventLoopGroupFactory,
      EventLoopGroupRegistry eventLoopGroupRegistry
   ) {
      this.applicationContext = applicationContext;
      this.requestArgumentSatisfier = routeExecutor.getRequestArgumentSatisfier();
      this.routeExecutor = routeExecutor;
      this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
      this.staticResourceResolver = staticResourceResolver;
      this.executorSelector = routeExecutor.getExecutorSelector();
      this.nettyThreadFactory = nettyThreadFactory;
      this.httpCompressionStrategy = httpCompressionStrategy;
      this.websocketBeanRegistry = WebSocketBeanRegistry.forServer(applicationContext);
      this.eventLoopGroupFactory = eventLoopGroupFactory;
      this.eventLoopGroupRegistry = eventLoopGroupRegistry;
   }

   @NonNull
   @Override
   public NettyEmbeddedServer build(@NonNull NettyHttpServerConfiguration configuration) {
      return this.buildInternal(configuration, false, null);
   }

   @NonNull
   @Override
   public NettyEmbeddedServer build(@NonNull NettyHttpServerConfiguration configuration, @Nullable ServerSslConfiguration sslConfiguration) {
      return this.buildInternal(configuration, false, sslConfiguration);
   }

   @Singleton
   @Primary
   @NonNull
   protected NettyEmbeddedServer buildDefaultServer(@NonNull NettyHttpServerConfiguration configuration) {
      return this.buildInternal(configuration, true, null);
   }

   @NonNull
   private NettyEmbeddedServer buildInternal(
      @NonNull NettyHttpServerConfiguration configuration, boolean isDefaultServer, @Nullable ServerSslConfiguration sslConfiguration
   ) {
      Objects.requireNonNull(configuration, "Netty HTTP server configuration cannot be null");
      List<NettyCustomizableResponseTypeHandler<?>> handlers = Arrays.asList(
         new FileTypeHandler(configuration.getFileTypeHandlerConfiguration()), new StreamTypeHandler()
      );
      if (isDefaultServer) {
         return new NettyHttpServer(
            configuration,
            this,
            new DefaultCustomizableResponseTypeHandlerRegistry(
               (NettyCustomizableResponseTypeHandler[])handlers.toArray(new NettyCustomizableResponseTypeHandler[0])
            ),
            true
         );
      } else {
         NettyEmbeddedServices embeddedServices = this.resolveNettyEmbeddedServices(configuration, sslConfiguration);
         return new NettyHttpServer(
            configuration,
            embeddedServices,
            new DefaultCustomizableResponseTypeHandlerRegistry(
               (NettyCustomizableResponseTypeHandler[])handlers.toArray(new NettyCustomizableResponseTypeHandler[0])
            ),
            false
         );
      }
   }

   private NettyEmbeddedServices resolveNettyEmbeddedServices(
      @NonNull NettyHttpServerConfiguration configuration, @Nullable ServerSslConfiguration sslConfiguration
   ) {
      if (sslConfiguration != null && sslConfiguration.isEnabled()) {
         ResourceResolver resourceResolver = this.applicationContext.getBean(ResourceResolver.class);
         final ServerSslBuilder serverSslBuilder;
         if (sslConfiguration.buildSelfSigned()) {
            serverSslBuilder = new SelfSignedSslBuilder(configuration, sslConfiguration, resourceResolver);
         } else {
            serverSslBuilder = new CertificateProvidedSslBuilder(configuration, sslConfiguration, resourceResolver);
         }

         return new DelegateNettyEmbeddedServices() {
            @Override
            public NettyEmbeddedServices getDelegate() {
               return DefaultNettyEmbeddedServerFactory.this;
            }

            @Override
            public ServerSslBuilder getServerSslBuilder() {
               return serverSslBuilder;
            }
         };
      } else {
         return this;
      }
   }

   @Override
   public List<ChannelOutboundHandler> getOutboundHandlers() {
      return this.outboundHandlers;
   }

   @Override
   public ApplicationContext getApplicationContext() {
      return this.applicationContext;
   }

   @Override
   public RequestArgumentSatisfier getRequestArgumentSatisfier() {
      return this.requestArgumentSatisfier;
   }

   @Override
   public RouteExecutor getRouteExecutor() {
      return this.routeExecutor;
   }

   @Override
   public MediaTypeCodecRegistry getMediaTypeCodecRegistry() {
      return this.mediaTypeCodecRegistry;
   }

   @Override
   public StaticResourceResolver getStaticResourceResolver() {
      return this.staticResourceResolver;
   }

   @Override
   public ExecutorSelector getExecutorSelector() {
      return this.executorSelector;
   }

   @Override
   public ServerSslBuilder getServerSslBuilder() {
      return this.serverSslBuilder;
   }

   @Override
   public ChannelOptionFactory getChannelOptionFactory() {
      if (this.channelOptionFactory == null) {
         this.channelOptionFactory = new DefaultChannelOptionFactory();
      }

      return this.channelOptionFactory;
   }

   @Override
   public HttpCompressionStrategy getHttpCompressionStrategy() {
      return this.httpCompressionStrategy;
   }

   @Override
   public WebSocketBeanRegistry getWebSocketBeanRegistry() {
      return this.websocketBeanRegistry;
   }

   @Override
   public EventLoopGroupRegistry getEventLoopGroupRegistry() {
      return this.eventLoopGroupRegistry;
   }

   @Override
   public EventLoopGroup createEventLoopGroup(EventLoopGroupConfiguration config) {
      return this.eventLoopGroupFactory.createEventLoopGroup(config, this.nettyThreadFactory);
   }

   @Override
   public ServerSocketChannel getServerSocketChannelInstance(EventLoopGroupConfiguration workerConfig) {
      return this.eventLoopGroupFactory.serverSocketChannelInstance(workerConfig);
   }

   @Override
   public ServerChannel getDomainServerChannelInstance(EventLoopGroupConfiguration workerConfig) {
      return this.eventLoopGroupFactory.domainServerSocketChannelInstance(workerConfig);
   }

   @Override
   public <E> ApplicationEventPublisher<E> getEventPublisher(Class<E> eventClass) {
      Objects.requireNonNull(eventClass, "Event class cannot be null");
      return (ApplicationEventPublisher<E>)this.cachedEventPublishers.computeIfAbsent(eventClass, this.applicationContext::getEventPublisher);
   }

   @NonNull
   @Override
   public EventLoopGroup createEventLoopGroup(int numThreads, @NonNull ExecutorService executorService, Integer ioRatio) {
      return this.eventLoopGroupFactory.createEventLoopGroup(numThreads, executorService, ioRatio);
   }

   @Inject
   protected void setChannelOptionFactory(@Nullable ChannelOptionFactory channelOptionFactory) {
      this.channelOptionFactory = channelOptionFactory;
   }

   @Inject
   protected void setServerSslBuilder(@Nullable ServerSslBuilder serverSslBuilder) {
      this.serverSslBuilder = serverSslBuilder;
   }

   @Inject
   protected void setOutboundHandlers(List<ChannelOutboundHandler> outboundHandlers) {
      if (CollectionUtils.isNotEmpty(outboundHandlers)) {
         OrderUtil.sort(outboundHandlers);
         this.outboundHandlers = Collections.unmodifiableList(outboundHandlers);
      }

   }
}
