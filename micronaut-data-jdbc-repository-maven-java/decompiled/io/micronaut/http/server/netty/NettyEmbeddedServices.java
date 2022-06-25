package io.micronaut.http.server.netty;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.netty.channel.EventLoopGroupConfiguration;
import io.micronaut.http.netty.channel.EventLoopGroupRegistry;
import io.micronaut.http.netty.channel.converters.ChannelOptionFactory;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.netty.ssl.ServerSslBuilder;
import io.micronaut.scheduling.executor.ExecutorSelector;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.resource.StaticResourceResolver;
import io.micronaut.websocket.context.WebSocketBeanRegistry;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.ServerSocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Internal
public interface NettyEmbeddedServices {
   @NonNull
   List<ChannelOutboundHandler> getOutboundHandlers();

   @NonNull
   ApplicationContext getApplicationContext();

   @NonNull
   default RequestArgumentSatisfier getRequestArgumentSatisfier() {
      return this.getRouteExecutor().getRequestArgumentSatisfier();
   }

   @NonNull
   RouteExecutor getRouteExecutor();

   @NonNull
   MediaTypeCodecRegistry getMediaTypeCodecRegistry();

   @NonNull
   StaticResourceResolver getStaticResourceResolver();

   @NonNull
   default ExecutorSelector getExecutorSelector() {
      return this.getRouteExecutor().getExecutorSelector();
   }

   @Nullable
   ServerSslBuilder getServerSslBuilder();

   @NonNull
   ChannelOptionFactory getChannelOptionFactory();

   @NonNull
   HttpCompressionStrategy getHttpCompressionStrategy();

   @NonNull
   WebSocketBeanRegistry getWebSocketBeanRegistry();

   @NonNull
   EventLoopGroupRegistry getEventLoopGroupRegistry();

   @NonNull
   default Router getRouter() {
      return this.getRouteExecutor().getRouter();
   }

   @NonNull
   EventLoopGroup createEventLoopGroup(@NonNull EventLoopGroupConfiguration config);

   @NonNull
   EventLoopGroup createEventLoopGroup(int numThreads, @NonNull ExecutorService executorService, @Nullable Integer ioRatio);

   @NonNull
   ServerSocketChannel getServerSocketChannelInstance(@NonNull EventLoopGroupConfiguration workerConfig);

   @NonNull
   default ServerChannel getDomainServerChannelInstance(@NonNull EventLoopGroupConfiguration workerConfig) {
      throw new UnsupportedOperationException("Domain sockets not supported");
   }

   @NonNull
   <E> ApplicationEventPublisher<E> getEventPublisher(@NonNull Class<E> eventClass);
}
