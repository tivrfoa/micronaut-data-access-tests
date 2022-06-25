package io.micronaut.http.server.netty;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.discovery.EmbeddedServerInstance;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.discovery.event.ServiceStoppedEvent;
import io.micronaut.http.context.event.HttpRequestTerminatedEvent;
import io.micronaut.http.netty.channel.ChannelPipelineListener;
import io.micronaut.http.netty.channel.DefaultEventLoopGroupConfiguration;
import io.micronaut.http.netty.channel.EventLoopGroupConfiguration;
import io.micronaut.http.netty.channel.converters.ChannelOptionFactory;
import io.micronaut.http.netty.stream.StreamingInboundHttp2ToHttpAdapter;
import io.micronaut.http.netty.websocket.WebSocketSessionRepository;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.server.netty.ssl.ServerSslBuilder;
import io.micronaut.http.server.netty.types.NettyCustomizableResponseTypeHandlerRegistry;
import io.micronaut.http.server.util.DefaultHttpHostResolver;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.http.ssl.ServerSslConfiguration;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.web.router.Router;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.io.File;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
@TypeHint(
   value = {ChannelOption.class},
   accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_DECLARED_FIELDS}
)
public class NettyHttpServer implements NettyEmbeddedServer {
   public static final String OUTBOUND_KEY = "-outbound-";
   private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServer.class);
   private final NettyEmbeddedServices nettyEmbeddedServices;
   private final NettyHttpServerConfiguration serverConfiguration;
   private final ServerSslConfiguration sslConfiguration;
   private final Environment environment;
   private final RoutingInBoundHandler routingHandler;
   private final HttpContentProcessorResolver httpContentProcessorResolver;
   private final boolean isDefault;
   private final ApplicationContext applicationContext;
   private final AtomicBoolean running = new AtomicBoolean(false);
   private final ChannelGroup webSocketSessions = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
   private final HttpHostResolver hostResolver;
   private boolean shutdownWorker = false;
   private boolean shutdownParent = false;
   private EventLoopGroup workerGroup;
   private EventLoopGroup parentGroup;
   private EmbeddedServerInstance serviceInstance;
   private final Collection<ChannelPipelineListener> pipelineListeners = new ArrayList(2);
   @Nullable
   private volatile List<NettyHttpServer.Listener> activeListeners = null;
   private final List<NettyHttpServerConfiguration.NettyListenerConfiguration> listenerConfigurations;

   public NettyHttpServer(
      NettyHttpServerConfiguration serverConfiguration,
      NettyEmbeddedServices nettyEmbeddedServices,
      NettyCustomizableResponseTypeHandlerRegistry handlerRegistry,
      boolean isDefault
   ) {
      this.isDefault = isDefault;
      this.serverConfiguration = serverConfiguration;
      this.nettyEmbeddedServices = nettyEmbeddedServices;
      Optional<File> location = this.serverConfiguration.getMultipart().getLocation();
      location.ifPresent(dir -> DiskFileUpload.baseDirectory = dir.getAbsolutePath());
      this.applicationContext = nettyEmbeddedServices.getApplicationContext();
      this.environment = this.applicationContext.getEnvironment();
      ServerSslBuilder serverSslBuilder = nettyEmbeddedServices.getServerSslBuilder();
      if (serverSslBuilder != null) {
         this.sslConfiguration = serverSslBuilder.getSslConfiguration();
      } else {
         this.sslConfiguration = null;
      }

      ApplicationEventPublisher<HttpRequestTerminatedEvent> httpRequestTerminatedEventPublisher = nettyEmbeddedServices.getEventPublisher(
         HttpRequestTerminatedEvent.class
      );
      Supplier<ExecutorService> ioExecutor = SupplierUtil.memoized(() -> (ExecutorService)nettyEmbeddedServices.getExecutorSelector().select("io").orElse(null));
      this.httpContentProcessorResolver = new DefaultHttpContentProcessorResolver(nettyEmbeddedServices.getApplicationContext(), () -> serverConfiguration);
      this.routingHandler = new RoutingInBoundHandler(
         serverConfiguration, handlerRegistry, nettyEmbeddedServices, ioExecutor, this.httpContentProcessorResolver, httpRequestTerminatedEventPublisher
      );
      this.hostResolver = new DefaultHttpHostResolver(serverConfiguration, () -> this);
      this.listenerConfigurations = this.buildListenerConfigurations();
   }

   private List<NettyHttpServerConfiguration.NettyListenerConfiguration> buildListenerConfigurations() {
      List<NettyHttpServerConfiguration.NettyListenerConfiguration> explicit = this.serverConfiguration.getListeners();
      if (explicit != null) {
         if (explicit.isEmpty()) {
            throw new IllegalArgumentException("When configuring listeners explicitly, must specify at least one");
         } else {
            return explicit;
         }
      } else {
         String configuredHost = (String)this.serverConfiguration.getHost().orElse(null);
         List<NettyHttpServerConfiguration.NettyListenerConfiguration> implicit = new ArrayList(2);
         ServerSslBuilder serverSslBuilder = this.nettyEmbeddedServices.getServerSslBuilder();
         if (serverSslBuilder != null && this.sslConfiguration.isEnabled()) {
            implicit.add(NettyHttpServerConfiguration.NettyListenerConfiguration.createTcp(configuredHost, this.sslConfiguration.getPort(), true));
         } else {
            implicit.add(NettyHttpServerConfiguration.NettyListenerConfiguration.createTcp(configuredHost, this.getHttpPort(this.serverConfiguration), false));
         }

         if (this.isDefault) {
            if (this.serverConfiguration.isDualProtocol()) {
               implicit.add(
                  NettyHttpServerConfiguration.NettyListenerConfiguration.createTcp(configuredHost, this.getHttpPort(this.serverConfiguration), false)
               );
            }

            Router router = this.nettyEmbeddedServices.getRouter();

            for(int exposedPort : router.getExposedPorts()) {
               if (exposedPort == -1 || exposedPort == 0 || implicit.stream().noneMatch(cfg -> cfg.getPort() == exposedPort)) {
                  NettyHttpServerConfiguration.NettyListenerConfiguration mgmt = NettyHttpServerConfiguration.NettyListenerConfiguration.createTcp(
                     configuredHost, exposedPort, false
                  );
                  mgmt.setExposeDefaultRoutes(false);
                  implicit.add(mgmt);
               }
            }
         }

         return implicit;
      }
   }

   private int getHttpPort(NettyHttpServerConfiguration serverConfiguration) {
      Integer configPort = (Integer)serverConfiguration.getPort().orElse(null);
      return this.getHttpPort(configPort);
   }

   private int getHttpPort(Integer configPort) {
      if (configPort != null) {
         return configPort;
      } else {
         return this.environment.getActiveNames().contains("test") ? -1 : 8080;
      }
   }

   @Override
   public boolean isKeepAlive() {
      return false;
   }

   public NettyHttpServerConfiguration getServerConfiguration() {
      return this.serverConfiguration;
   }

   @Override
   public boolean isRunning() {
      return this.running.get();
   }

   @Override
   public synchronized NettyEmbeddedServer start() {
      if (!this.isRunning()) {
         EventLoopGroupConfiguration workerConfig = this.resolveWorkerConfiguration();
         this.workerGroup = this.createWorkerEventLoopGroup(workerConfig);
         this.parentGroup = this.createParentEventLoopGroup();
         ServerBootstrap serverBootstrap = this.createServerBootstrap();
         this.processOptions(this.serverConfiguration.getOptions(), serverBootstrap::option);
         this.processOptions(this.serverConfiguration.getChildOptions(), serverBootstrap::childOption);
         serverBootstrap = serverBootstrap.group(this.parentGroup, this.workerGroup);
         List<NettyHttpServer.Listener> listeners = new ArrayList();

         for(NettyHttpServerConfiguration.NettyListenerConfiguration listenerConfiguration : this.listenerConfigurations) {
            NettyHttpServer.Listener listener = this.bind(serverBootstrap, listenerConfiguration, workerConfig);
            listeners.add(listener);
         }

         this.activeListeners = Collections.unmodifiableList(listeners);
         if (this.isDefault) {
            Router router = this.nettyEmbeddedServices.getRouter();
            Set<Integer> exposedPorts = router.getExposedPorts();
            if (CollectionUtils.isNotEmpty(exposedPorts)) {
               router.applyDefaultPorts(
                  (List<Integer>)listeners.stream()
                     .filter(l -> l.config.isExposeDefaultRoutes())
                     .map(l -> l.serverChannel.localAddress())
                     .filter(InetSocketAddress.class::isInstance)
                     .map(addr -> ((InetSocketAddress)addr).getPort())
                     .collect(Collectors.toList())
               );
            }
         }

         this.fireStartupEvents();
         this.running.set(true);
      }

      return this;
   }

   private EventLoopGroupConfiguration resolveWorkerConfiguration() {
      EventLoopGroupConfiguration workerConfig = this.serverConfiguration.getWorker();
      if (workerConfig == null) {
         workerConfig = (EventLoopGroupConfiguration)this.nettyEmbeddedServices
            .getEventLoopGroupRegistry()
            .getEventLoopGroupConfiguration("default")
            .orElse(null);
      } else {
         String eventLoopGroupName = workerConfig.getName();
         if (!"default".equals(eventLoopGroupName)) {
            workerConfig = (EventLoopGroupConfiguration)this.nettyEmbeddedServices
               .getEventLoopGroupRegistry()
               .getEventLoopGroupConfiguration(eventLoopGroupName)
               .orElse(workerConfig);
         }
      }

      return workerConfig;
   }

   @Override
   public synchronized NettyEmbeddedServer stop() {
      if (this.isRunning() && this.workerGroup != null && this.running.compareAndSet(true, false)) {
         this.stopInternal();
      }

      return this;
   }

   @Override
   public int getPort() {
      List<NettyHttpServer.Listener> listenersLocal = this.activeListeners;
      boolean hasRandom = false;
      boolean hasUnix = false;
      if (listenersLocal == null) {
         for(NettyHttpServerConfiguration.NettyListenerConfiguration listenerCfg : this.listenerConfigurations) {
            switch(listenerCfg.getFamily()) {
               case TCP:
                  if (listenerCfg.getPort() != -1) {
                     return listenerCfg.getPort();
                  }

                  hasRandom = true;
                  break;
               case UNIX:
                  hasUnix = true;
            }
         }
      } else {
         for(NettyHttpServer.Listener listener : listenersLocal) {
            SocketAddress localAddress = listener.serverChannel.localAddress();
            if (localAddress instanceof InetSocketAddress) {
               return ((InetSocketAddress)localAddress).getPort();
            }

            hasUnix = true;
         }
      }

      if (hasRandom) {
         throw new UnsupportedOperationException("Retrieving the port from the server before it has started is not supported when binding to a random port");
      } else if (hasUnix) {
         throw new UnsupportedOperationException("Retrieving the port from the server is not supported for unix domain sockets");
      } else {
         throw new UnsupportedOperationException("Could not retrieve server port");
      }
   }

   @Override
   public String getHost() {
      return (String)this.serverConfiguration.getHost().orElseGet(() -> (String)Optional.ofNullable(CachedEnvironment.getenv("HOSTNAME")).orElse("localhost"));
   }

   @Override
   public String getScheme() {
      return this.sslConfiguration != null && this.sslConfiguration.isEnabled() ? "https" : "http";
   }

   @Override
   public URL getURL() {
      try {
         return new URL(this.getScheme() + "://" + this.getHost() + ':' + this.getPort());
      } catch (MalformedURLException var2) {
         throw new ConfigurationException("Invalid server URL: " + var2.getMessage(), var2);
      }
   }

   @Override
   public URI getURI() {
      try {
         return new URI(this.getScheme() + "://" + this.getHost() + ':' + this.getPort());
      } catch (URISyntaxException var2) {
         throw new ConfigurationException("Invalid server URL: " + var2.getMessage(), var2);
      }
   }

   @Override
   public ApplicationContext getApplicationContext() {
      return this.applicationContext;
   }

   @Override
   public ApplicationConfiguration getApplicationConfiguration() {
      return this.serverConfiguration.getApplicationConfiguration();
   }

   @Override
   public final Set<Integer> getBoundPorts() {
      List<NettyHttpServer.Listener> listeners = this.activeListeners;
      return listeners == null
         ? Collections.emptySet()
         : Collections.unmodifiableSet(
            (Set)listeners.stream()
               .map(l -> l.serverChannel.localAddress())
               .filter(InetSocketAddress.class::isInstance)
               .map(addr -> ((InetSocketAddress)addr).getPort())
               .collect(Collectors.toCollection(LinkedHashSet::new))
         );
   }

   protected EventLoopGroup createParentEventLoopGroup() {
      NettyHttpServerConfiguration.Parent parent = this.serverConfiguration.getParent();
      return (EventLoopGroup)this.nettyEmbeddedServices
         .getEventLoopGroupRegistry()
         .getEventLoopGroup(parent != null ? parent.getName() : "parent")
         .orElseGet(() -> {
            EventLoopGroup newGroup = this.newEventLoopGroup(parent);
            this.shutdownParent = true;
            return newGroup;
         });
   }

   protected EventLoopGroup createWorkerEventLoopGroup(@Nullable EventLoopGroupConfiguration workerConfig) {
      String configName = workerConfig != null ? workerConfig.getName() : "default";
      return (EventLoopGroup)this.nettyEmbeddedServices
         .getEventLoopGroupRegistry()
         .getEventLoopGroup(configName)
         .orElseGet(
            () -> {
               LOG.warn(
                  "The configuration for 'micronaut.server.netty.worker.{}' is deprecated. Use 'micronaut.netty.event-loops.default' configuration instead.",
                  configName
               );
               EventLoopGroup newGroup = this.newEventLoopGroup(workerConfig);
               this.shutdownWorker = true;
               return newGroup;
            }
         );
   }

   protected ServerBootstrap createServerBootstrap() {
      return new ServerBootstrap();
   }

   private NettyHttpServer.Listener bind(
      ServerBootstrap bootstrap, NettyHttpServerConfiguration.NettyListenerConfiguration cfg, EventLoopGroupConfiguration workerConfig
   ) {
      this.logBind(cfg);

      try {
         NettyHttpServer.Listener listener = new NettyHttpServer.Listener(cfg);
         ServerBootstrap listenerBootstrap = bootstrap.clone();
         listenerBootstrap.childHandler(listener);
         ChannelFuture future;
         switch(cfg.getFamily()) {
            case TCP:
               listenerBootstrap.channelFactory(() -> this.nettyEmbeddedServices.getServerSocketChannelInstance(workerConfig));
               int port = cfg.getPort();
               if (port == -1) {
                  port = 0;
               }

               if (cfg.getHost() == null) {
                  future = listenerBootstrap.bind(port);
               } else {
                  future = listenerBootstrap.bind(cfg.getHost(), port);
               }
               break;
            case UNIX:
               listenerBootstrap.channelFactory(() -> this.nettyEmbeddedServices.getDomainServerChannelInstance(workerConfig));
               future = listenerBootstrap.bind(NettyHttpServer.DomainSocketHolder.makeDomainSocketAddress(cfg.getPath()));
               break;
            default:
               throw new UnsupportedOperationException("Unsupported family: " + cfg.getFamily());
         }

         listener.serverChannel = future.channel();
         future.syncUninterruptibly();
         return listener;
      } catch (Exception var8) {
         boolean isBindError = var8 instanceof BindException;
         if (LOG.isErrorEnabled()) {
            if (isBindError) {
               LOG.error("Unable to start server. Port {} already in use.", displayAddress(cfg));
            } else {
               LOG.error("Error starting Micronaut server: " + var8.getMessage(), var8);
            }
         }

         this.stopInternal();
         throw new ServerStartupException("Unable to start Micronaut server on " + displayAddress(cfg), var8);
      }
   }

   private void logBind(NettyHttpServerConfiguration.NettyListenerConfiguration cfg) {
      Optional<String> applicationName = this.serverConfiguration.getApplicationConfiguration().getName();
      if (applicationName.isPresent()) {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Binding {} server to {}", applicationName.get(), displayAddress(cfg));
         }
      } else if (LOG.isTraceEnabled()) {
         LOG.trace("Binding server to {}", displayAddress(cfg));
      }

   }

   private static String displayAddress(NettyHttpServerConfiguration.NettyListenerConfiguration cfg) {
      switch(cfg.getFamily()) {
         case TCP:
            if (cfg.getHost() == null) {
               return "*:" + cfg.getPort();
            }

            return cfg.getHost() + ":" + cfg.getPort();
         case UNIX:
            if (cfg.getPath().startsWith("\u0000")) {
               return "unix:@" + cfg.getPath().substring(1);
            }

            return "unix:" + cfg.getPath();
         default:
            throw new UnsupportedOperationException("Unsupported family: " + cfg.getFamily());
      }
   }

   private void fireStartupEvents() {
      Optional<String> applicationName = this.serverConfiguration.getApplicationConfiguration().getName();
      this.applicationContext.getEventPublisher(ServerStartupEvent.class).publishEvent(new ServerStartupEvent(this));
      applicationName.ifPresent(id -> {
         this.serviceInstance = this.applicationContext.createBean(NettyEmbeddedServerInstance.class, new Object[]{id, this});
         this.applicationContext.getEventPublisher(ServiceReadyEvent.class).publishEvent(new ServiceReadyEvent(this.serviceInstance));
      });
   }

   private void logShutdownErrorIfNecessary(Future<?> future) {
      if (!future.isSuccess() && LOG.isWarnEnabled()) {
         Throwable e = future.cause();
         LOG.warn("Error stopping Micronaut server: " + e.getMessage(), e);
      }

   }

   private void stopInternal() {
      try {
         if (this.shutdownParent) {
            EventLoopGroupConfiguration parent = this.serverConfiguration.getParent();
            if (parent != null) {
               long quietPeriod = parent.getShutdownQuietPeriod().toMillis();
               long timeout = parent.getShutdownTimeout().toMillis();
               this.parentGroup.shutdownGracefully(quietPeriod, timeout, TimeUnit.MILLISECONDS).addListener(this::logShutdownErrorIfNecessary);
            } else {
               this.parentGroup.shutdownGracefully().addListener(this::logShutdownErrorIfNecessary);
            }
         }

         if (this.shutdownWorker) {
            this.workerGroup.shutdownGracefully().addListener(this::logShutdownErrorIfNecessary);
         }

         this.webSocketSessions.close();
         this.applicationContext.getEventPublisher(ServerShutdownEvent.class).publishEvent(new ServerShutdownEvent(this));
         if (this.serviceInstance != null) {
            this.applicationContext.getEventPublisher(ServiceStoppedEvent.class).publishEvent(new ServiceStoppedEvent(this.serviceInstance));
         }

         if (this.isDefault && this.applicationContext.isRunning()) {
            this.applicationContext.stop();
         }

         this.serverConfiguration.getMultipart().getLocation().ifPresent(dir -> DiskFileUpload.baseDirectory = null);
         this.activeListeners = null;
      } catch (Throwable var6) {
         if (LOG.isErrorEnabled()) {
            LOG.error("Error stopping Micronaut server: " + var6.getMessage(), var6);
         }
      }

   }

   private EventLoopGroup newEventLoopGroup(EventLoopGroupConfiguration config) {
      if (config != null) {
         ExecutorService executorService = (ExecutorService)config.getExecutorName()
            .flatMap(name -> this.applicationContext.findBean(ExecutorService.class, Qualifiers.byName(name)))
            .orElse(null);
         return executorService != null
            ? this.nettyEmbeddedServices.createEventLoopGroup(config.getNumThreads(), executorService, (Integer)config.getIoRatio().orElse(null))
            : this.nettyEmbeddedServices.createEventLoopGroup(config);
      } else {
         return this.nettyEmbeddedServices.createEventLoopGroup(new DefaultEventLoopGroupConfiguration());
      }
   }

   private void processOptions(Map<ChannelOption, Object> options, BiConsumer<ChannelOption, Object> biConsumer) {
      ChannelOptionFactory channelOptionFactory = this.nettyEmbeddedServices.getChannelOptionFactory();
      options.forEach((option, value) -> biConsumer.accept(option, channelOptionFactory.convertValue(option, value, this.environment)));
   }

   @Override
   public void addChannel(Channel channel) {
      this.webSocketSessions.add(channel);
   }

   @Override
   public void removeChannel(Channel channel) {
      this.webSocketSessions.remove(channel);
   }

   @Override
   public ChannelGroup getChannelGroup() {
      return this.webSocketSessions;
   }

   public WebSocketSessionRepository getWebSocketSessionRepository() {
      return this;
   }

   private HttpToHttp2ConnectionHandler newHttpToHttp2ConnectionHandler() {
      Http2Connection connection = new DefaultHttp2Connection(true);
      Http2FrameListener http2ToHttpAdapter = new StreamingInboundHttp2ToHttpAdapter(
         connection, (int)this.serverConfiguration.getMaxRequestSize(), this.serverConfiguration.isValidateHeaders(), true
      );
      HttpToHttp2ConnectionHandlerBuilder builder = new HttpToHttp2ConnectionHandlerBuilder()
         .frameListener(http2ToHttpAdapter)
         .validateHeaders(this.serverConfiguration.isValidateHeaders())
         .initialSettings(this.serverConfiguration.getHttp2().http2Settings());
      this.serverConfiguration.getLogLevel().ifPresent(logLevel -> builder.frameLogger(new Http2FrameLogger(logLevel, NettyHttpServer.class)));
      return builder.connection(connection).build();
   }

   @Override
   public boolean isClientChannel() {
      return false;
   }

   @Override
   public void doOnConnect(@NonNull ChannelPipelineListener listener) {
      this.pipelineListeners.add(Objects.requireNonNull(listener, "The listener cannot be null"));
   }

   @Override
   public Set<String> getObservedConfigurationPrefixes() {
      return Collections.singleton("micronaut.server");
   }

   public void onApplicationEvent(RefreshEvent event) {
      List<NettyHttpServer.Listener> listeners = this.activeListeners;
      if (listeners != null) {
         for(NettyHttpServer.Listener listener : listeners) {
            listener.refresh();
         }
      }

   }

   final void triggerPipelineListeners(ChannelPipeline pipeline) {
      for(ChannelPipelineListener pipelineListener : this.pipelineListeners) {
         pipelineListener.onConnect(pipeline);
      }

   }

   private HttpPipelineBuilder createPipelineBuilder() {
      return new HttpPipelineBuilder(this, this.nettyEmbeddedServices, this.sslConfiguration, this.routingHandler, this.hostResolver);
   }

   @Internal
   public EmbeddedChannel buildEmbeddedChannel(boolean ssl) {
      EmbeddedChannel embeddedChannel = new EmbeddedChannel();
      this.createPipelineBuilder().new ConnectionPipeline(embeddedChannel, ssl).initChannel();
      return embeddedChannel;
   }

   static Predicate<String> inclusionPredicate(NettyHttpServerConfiguration.AccessLogger config) {
      List<String> exclusions = config.getExclusions();
      if (CollectionUtils.isEmpty(exclusions)) {
         return null;
      } else {
         List<Pattern> patterns = (List)exclusions.stream().map(Pattern::compile).collect(Collectors.toList());
         return uri -> patterns.stream().noneMatch(pattern -> pattern.matcher(uri).matches());
      }
   }

   private static class DomainSocketHolder {
      @NonNull
      private static SocketAddress makeDomainSocketAddress(String path) {
         try {
            return new DomainSocketAddress(path);
         } catch (NoClassDefFoundError var2) {
            throw new UnsupportedOperationException("Netty domain socket support not on classpath", var2);
         }
      }
   }

   private class Listener extends ChannelInitializer<Channel> {
      Channel serverChannel;
      NettyHttpServerConfiguration.NettyListenerConfiguration config;
      private volatile HttpPipelineBuilder httpPipelineBuilder;

      Listener(NettyHttpServerConfiguration.NettyListenerConfiguration config) {
         this.config = config;
         this.refresh();
      }

      void refresh() {
         this.httpPipelineBuilder = NettyHttpServer.this.createPipelineBuilder();
         if (this.config.isSsl() && !this.httpPipelineBuilder.supportsSsl()) {
            throw new IllegalStateException("Listener configured for SSL, but no SSL context available");
         }
      }

      @Override
      protected void initChannel(@NonNull Channel ch) throws Exception {
         this.httpPipelineBuilder.new ConnectionPipeline(ch, this.config.isSsl()).initChannel();
      }
   }
}
