package io.micronaut.http.client.netty;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.FilterMatcher;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.HttpClientRegistry;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.LoadBalancerResolver;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.client.ProxyHttpClientRegistry;
import io.micronaut.http.client.StreamingHttpClient;
import io.micronaut.http.client.StreamingHttpClientRegistry;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.filter.ClientFilterResolutionContext;
import io.micronaut.http.client.netty.ssl.NettyClientSslBuilder;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.client.sse.SseClientRegistry;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.filter.HttpClientFilterResolver;
import io.micronaut.http.netty.channel.ChannelPipelineCustomizer;
import io.micronaut.http.netty.channel.ChannelPipelineListener;
import io.micronaut.http.netty.channel.DefaultEventLoopGroupConfiguration;
import io.micronaut.http.netty.channel.EventLoopGroupConfiguration;
import io.micronaut.http.netty.channel.EventLoopGroupFactory;
import io.micronaut.http.netty.channel.EventLoopGroupRegistry;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.json.JsonFeatures;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.codec.MapperMediaTypeCodec;
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory;
import io.micronaut.websocket.WebSocketClient;
import io.micronaut.websocket.WebSocketClientRegistry;
import io.micronaut.websocket.context.WebSocketBeanRegistry;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
@BootstrapContextCompatible
@Internal
public class DefaultNettyHttpClientRegistry
   implements AutoCloseable,
   HttpClientRegistry<HttpClient>,
   SseClientRegistry<SseClient>,
   StreamingHttpClientRegistry<StreamingHttpClient>,
   WebSocketClientRegistry<WebSocketClient>,
   ProxyHttpClientRegistry<ProxyHttpClient>,
   ChannelPipelineCustomizer {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultNettyHttpClientRegistry.class);
   private final Map<DefaultNettyHttpClientRegistry.ClientKey, DefaultHttpClient> clients = new ConcurrentHashMap(10);
   private final LoadBalancerResolver loadBalancerResolver;
   private final NettyClientSslBuilder nettyClientSslBuilder;
   private final ThreadFactory threadFactory;
   private final MediaTypeCodecRegistry codecRegistry;
   private final BeanContext beanContext;
   private final HttpClientConfiguration defaultHttpClientConfiguration;
   private final EventLoopGroupRegistry eventLoopGroupRegistry;
   private final List<InvocationInstrumenterFactory> invocationInstrumenterFactories;
   private final EventLoopGroupFactory eventLoopGroupFactory;
   private final HttpClientFilterResolver<ClientFilterResolutionContext> clientFilterResolver;
   private final JsonMapper jsonMapper;
   private final Collection<ChannelPipelineListener> pipelineListeners = new CopyOnWriteArrayList();

   public DefaultNettyHttpClientRegistry(
      HttpClientConfiguration defaultHttpClientConfiguration,
      HttpClientFilterResolver httpClientFilterResolver,
      LoadBalancerResolver loadBalancerResolver,
      NettyClientSslBuilder nettyClientSslBuilder,
      ThreadFactory threadFactory,
      MediaTypeCodecRegistry codecRegistry,
      EventLoopGroupRegistry eventLoopGroupRegistry,
      EventLoopGroupFactory eventLoopGroupFactory,
      BeanContext beanContext,
      List<InvocationInstrumenterFactory> invocationInstrumenterFactories,
      JsonMapper jsonMapper
   ) {
      this.clientFilterResolver = httpClientFilterResolver;
      this.defaultHttpClientConfiguration = defaultHttpClientConfiguration;
      this.loadBalancerResolver = loadBalancerResolver;
      this.nettyClientSslBuilder = nettyClientSslBuilder;
      this.threadFactory = threadFactory;
      this.codecRegistry = codecRegistry;
      this.beanContext = beanContext;
      this.eventLoopGroupFactory = eventLoopGroupFactory;
      this.eventLoopGroupRegistry = eventLoopGroupRegistry;
      this.invocationInstrumenterFactories = invocationInstrumenterFactories;
      this.jsonMapper = jsonMapper;
   }

   @NonNull
   @Override
   public HttpClient getClient(HttpVersion httpVersion, @NonNull String clientId, @Nullable String path) {
      DefaultNettyHttpClientRegistry.ClientKey key = new DefaultNettyHttpClientRegistry.ClientKey(httpVersion, clientId, null, path, null, null);
      return this.getClient(key, this.beanContext, AnnotationMetadata.EMPTY_METADATA);
   }

   @NonNull
   public DefaultHttpClient getClient(@NonNull AnnotationMetadata metadata) {
      DefaultNettyHttpClientRegistry.ClientKey key = this.getClientKey(metadata);
      return this.getClient(key, this.beanContext, metadata);
   }

   @NonNull
   public DefaultHttpClient getSseClient(@NonNull AnnotationMetadata metadata) {
      return this.getClient(metadata);
   }

   @NonNull
   public DefaultHttpClient getStreamingHttpClient(@NonNull AnnotationMetadata metadata) {
      return this.getClient(metadata);
   }

   @NonNull
   public DefaultHttpClient getProxyHttpClient(@NonNull AnnotationMetadata metadata) {
      return this.getClient(metadata);
   }

   @NonNull
   public DefaultHttpClient getWebSocketClient(@NonNull AnnotationMetadata metadata) {
      return this.getClient(metadata);
   }

   @PreDestroy
   public void close() {
      for(HttpClient httpClient : this.clients.values()) {
         try {
            httpClient.close();
         } catch (Throwable var4) {
            if (LOG.isWarnEnabled()) {
               LOG.warn("Error shutting down HTTP client: " + var4.getMessage(), var4);
            }
         }
      }

      this.clients.clear();
   }

   @Override
   public void disposeClient(AnnotationMetadata annotationMetadata) {
      DefaultNettyHttpClientRegistry.ClientKey key = this.getClientKey(annotationMetadata);
      StreamingHttpClient streamingHttpClient = (StreamingHttpClient)this.clients.get(key);
      if (streamingHttpClient != null && streamingHttpClient.isRunning()) {
         streamingHttpClient.close();
         this.clients.remove(key);
      }

   }

   @Bean
   @BootstrapContextCompatible
   @Primary
   protected DefaultHttpClient httpClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Parameter @Nullable LoadBalancer loadBalancer,
      @Parameter @Nullable HttpClientConfiguration configuration,
      BeanContext beanContext
   ) {
      return this.resolveDefaultHttpClient(injectionPoint, loadBalancer, configuration, beanContext);
   }

   @NonNull
   @Override
   public HttpClient resolveClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   ) {
      return this.resolveDefaultHttpClient(injectionPoint, loadBalancer, configuration, beanContext);
   }

   @NonNull
   @Override
   public ProxyHttpClient resolveProxyHttpClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   ) {
      return this.resolveDefaultHttpClient(injectionPoint, loadBalancer, configuration, beanContext);
   }

   @NonNull
   @Override
   public SseClient resolveSseClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   ) {
      return this.resolveDefaultHttpClient(injectionPoint, loadBalancer, configuration, beanContext);
   }

   @NonNull
   @Override
   public StreamingHttpClient resolveStreamingHttpClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   ) {
      return this.resolveDefaultHttpClient(injectionPoint, loadBalancer, configuration, beanContext);
   }

   @NonNull
   @Override
   public WebSocketClient resolveWebSocketClient(
      @Nullable InjectionPoint<?> injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   ) {
      return this.resolveDefaultHttpClient(injectionPoint, loadBalancer, configuration, beanContext);
   }

   @Override
   public boolean isClientChannel() {
      return true;
   }

   @Override
   public void doOnConnect(@NonNull ChannelPipelineListener listener) {
      Objects.requireNonNull(listener, "listener");
      this.pipelineListeners.add(listener);
   }

   private DefaultHttpClient getClient(DefaultNettyHttpClientRegistry.ClientKey key, BeanContext beanContext, AnnotationMetadata annotationMetadata) {
      return (DefaultHttpClient)this.clients
         .computeIfAbsent(
            key,
            clientKey -> {
               DefaultHttpClient clientBean = null;
               String clientId = clientKey.clientId;
               Class<?> configurationClass = clientKey.configurationClass;
               if (clientId != null) {
                  clientBean = (DefaultHttpClient)this.beanContext.findBean(HttpClient.class, Qualifiers.byName(clientId)).orElse(null);
               }
      
               if (configurationClass != null && !HttpClientConfiguration.class.isAssignableFrom(configurationClass)) {
                  throw new IllegalStateException(
                     "Referenced HTTP client configuration class must be an instance of HttpClientConfiguration for injection point: " + configurationClass
                  );
               } else {
                  List<String> filterAnnotations = clientKey.filterAnnotations;
                  String path = clientKey.path;
                  if (clientBean != null && path == null && configurationClass == null && filterAnnotations.isEmpty()) {
                     return clientBean;
                  } else {
                     LoadBalancer loadBalancer = null;
                     List<String> clientIdentifiers = null;
                     HttpClientConfiguration configuration;
                     if (configurationClass != null) {
                        configuration = this.beanContext.getBean(configurationClass);
                     } else if (clientId != null) {
                        configuration = (HttpClientConfiguration)this.beanContext
                           .findBean(HttpClientConfiguration.class, Qualifiers.byName(clientId))
                           .orElse(this.defaultHttpClientConfiguration);
                     } else {
                        configuration = this.defaultHttpClientConfiguration;
                     }
      
                     if (clientId != null) {
                        loadBalancer = (LoadBalancer)this.loadBalancerResolver
                           .resolve(clientId)
                           .orElseThrow(() -> new HttpClientException("Invalid service reference [" + clientId + "] specified to @Client"));
                        clientIdentifiers = Collections.singletonList(clientId);
                     }
      
                     String contextPath = null;
                     if (StringUtils.isNotEmpty(path)) {
                        contextPath = path;
                     } else if (StringUtils.isNotEmpty(clientId) && clientId.startsWith("/")) {
                        contextPath = clientId;
                     } else if (loadBalancer != null) {
                        contextPath = (String)loadBalancer.getContextPath().orElse(null);
                     }
      
                     DefaultHttpClient client = this.buildClient(
                        loadBalancer, clientKey.httpVersion, configuration, clientIdentifiers, contextPath, beanContext, annotationMetadata
                     );
                     JsonFeatures jsonFeatures = clientKey.jsonFeatures;
                     if (jsonFeatures != null) {
                        List<MediaTypeCodec> codecs = new ArrayList(2);
                        MediaTypeCodecRegistry codecRegistry = client.getMediaTypeCodecRegistry();
      
                        for(MediaTypeCodec codec : codecRegistry.getCodecs()) {
                           if (codec instanceof MapperMediaTypeCodec) {
                              codecs.add(((MapperMediaTypeCodec)codec).cloneWithFeatures(jsonFeatures));
                           } else {
                              codecs.add(codec);
                           }
                        }
      
                        if (!codecRegistry.findCodec(MediaType.APPLICATION_JSON_TYPE).isPresent()) {
                           codecs.add(createNewJsonCodec(this.beanContext, jsonFeatures));
                        }
      
                        client.setMediaTypeCodecRegistry(MediaTypeCodecRegistry.of(codecs));
                     }
      
                     return client;
                  }
               }
            }
         );
   }

   private DefaultHttpClient buildClient(
      LoadBalancer loadBalancer,
      HttpVersion httpVersion,
      HttpClientConfiguration configuration,
      List<String> clientIdentifiers,
      String contextPath,
      BeanContext beanContext,
      AnnotationMetadata annotationMetadata
   ) {
      EventLoopGroup eventLoopGroup = this.resolveEventLoopGroup(configuration, beanContext);
      return new DefaultHttpClient(
         loadBalancer,
         httpVersion,
         configuration,
         contextPath,
         this.clientFilterResolver,
         this.clientFilterResolver.resolveFilterEntries(new ClientFilterResolutionContext(clientIdentifiers, annotationMetadata)),
         this.threadFactory,
         this.nettyClientSslBuilder,
         this.codecRegistry,
         WebSocketBeanRegistry.forClient(beanContext),
         (RequestBinderRegistry)beanContext.findBean(RequestBinderRegistry.class).orElseGet(() -> new DefaultRequestBinderRegistry(ConversionService.SHARED)),
         eventLoopGroup,
         this.resolveSocketChannelFactory(configuration, beanContext),
         this.pipelineListeners,
         this.invocationInstrumenterFactories
      );
   }

   private EventLoopGroup resolveEventLoopGroup(HttpClientConfiguration configuration, BeanContext beanContext) {
      String eventLoopGroupName = configuration.getEventLoopGroup();
      EventLoopGroup eventLoopGroup;
      if ("default".equals(eventLoopGroupName)) {
         eventLoopGroup = this.eventLoopGroupRegistry.getDefaultEventLoopGroup();
      } else {
         eventLoopGroup = (EventLoopGroup)beanContext.findBean(EventLoopGroup.class, Qualifiers.byName(eventLoopGroupName))
            .orElseThrow(() -> new HttpClientException("Specified event loop group is not defined: " + eventLoopGroupName));
      }

      return eventLoopGroup;
   }

   private DefaultHttpClient resolveDefaultHttpClient(
      @Nullable InjectionPoint injectionPoint,
      @Nullable LoadBalancer loadBalancer,
      @Nullable HttpClientConfiguration configuration,
      @NonNull BeanContext beanContext
   ) {
      if (loadBalancer != null) {
         if (configuration == null) {
            configuration = this.defaultHttpClientConfiguration;
         }

         return this.buildClient(
            loadBalancer, null, configuration, null, (String)loadBalancer.getContextPath().orElse(null), beanContext, AnnotationMetadata.EMPTY_METADATA
         );
      } else {
         return this.getClient(injectionPoint != null ? injectionPoint.getAnnotationMetadata() : AnnotationMetadata.EMPTY_METADATA);
      }
   }

   private ChannelFactory resolveSocketChannelFactory(HttpClientConfiguration configuration, BeanContext beanContext) {
      String eventLoopGroup = configuration.getEventLoopGroup();
      EventLoopGroupConfiguration eventLoopGroupConfiguration = (EventLoopGroupConfiguration)beanContext.findBean(
            EventLoopGroupConfiguration.class, Qualifiers.byName(eventLoopGroup)
         )
         .orElseGet(() -> {
            if ("default".equals(eventLoopGroup)) {
               return new DefaultEventLoopGroupConfiguration();
            } else {
               throw new HttpClientException("Specified event loop group is not defined: " + eventLoopGroup);
            }
         });
      return () -> this.eventLoopGroupFactory.clientSocketChannelInstance(eventLoopGroupConfiguration);
   }

   private DefaultNettyHttpClientRegistry.ClientKey getClientKey(AnnotationMetadata metadata) {
      HttpVersion httpVersion = (HttpVersion)metadata.enumValue(Client.class, "httpVersion", HttpVersion.class).orElse(null);
      String clientId = (String)metadata.stringValue(Client.class).orElse(null);
      String path = (String)metadata.stringValue(Client.class, "path").orElse(null);
      List<String> filterAnnotation = metadata.getAnnotationNamesByStereotype(FilterMatcher.class);
      Class configurationClass = (Class)metadata.classValue(Client.class, "configuration").orElse(null);
      JsonFeatures jsonFeatures = (JsonFeatures)this.jsonMapper.detectFeatures(metadata).orElse(null);
      return new DefaultNettyHttpClientRegistry.ClientKey(httpVersion, clientId, filterAnnotation, path, configurationClass, jsonFeatures);
   }

   private static MediaTypeCodec createNewJsonCodec(BeanContext beanContext, JsonFeatures jsonFeatures) {
      return getJsonCodec(beanContext).cloneWithFeatures(jsonFeatures);
   }

   private static MapperMediaTypeCodec getJsonCodec(BeanContext beanContext) {
      return beanContext.getBean(MapperMediaTypeCodec.class, Qualifiers.byName("json"));
   }

   @Internal
   private static final class ClientKey {
      final HttpVersion httpVersion;
      final String clientId;
      final List<String> filterAnnotations;
      final String path;
      final Class<?> configurationClass;
      final JsonFeatures jsonFeatures;

      ClientKey(HttpVersion httpVersion, String clientId, List<String> filterAnnotations, String path, Class<?> configurationClass, JsonFeatures jsonFeatures) {
         this.httpVersion = httpVersion;
         this.clientId = clientId;
         this.filterAnnotations = filterAnnotations;
         this.path = path;
         this.configurationClass = configurationClass;
         this.jsonFeatures = jsonFeatures;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultNettyHttpClientRegistry.ClientKey clientKey = (DefaultNettyHttpClientRegistry.ClientKey)o;
            return this.httpVersion == clientKey.httpVersion
               && Objects.equals(this.clientId, clientKey.clientId)
               && Objects.equals(this.filterAnnotations, clientKey.filterAnnotations)
               && Objects.equals(this.path, clientKey.path)
               && Objects.equals(this.configurationClass, clientKey.configurationClass)
               && Objects.equals(this.jsonFeatures, clientKey.jsonFeatures);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.httpVersion, this.clientId, this.filterAnnotations, this.path, this.configurationClass, this.jsonFeatures});
      }
   }
}
