package io.micronaut.http.server.netty;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.netty.channel.EventLoopGroupConfiguration;
import io.micronaut.http.netty.channel.EventLoopGroupRegistry;
import io.micronaut.http.netty.channel.converters.ChannelOptionFactory;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.netty.ssl.ServerSslBuilder;
import io.micronaut.web.router.resource.StaticResourceResolver;
import io.micronaut.websocket.context.WebSocketBeanRegistry;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Internal
interface DelegateNettyEmbeddedServices extends NettyEmbeddedServices {
   @NonNull
   NettyEmbeddedServices getDelegate();

   @Override
   default List<ChannelOutboundHandler> getOutboundHandlers() {
      return this.getDelegate().getOutboundHandlers();
   }

   @Override
   default ApplicationContext getApplicationContext() {
      return this.getDelegate().getApplicationContext();
   }

   @Override
   default RouteExecutor getRouteExecutor() {
      return this.getDelegate().getRouteExecutor();
   }

   @Override
   default MediaTypeCodecRegistry getMediaTypeCodecRegistry() {
      return this.getDelegate().getMediaTypeCodecRegistry();
   }

   @Override
   default StaticResourceResolver getStaticResourceResolver() {
      return this.getDelegate().getStaticResourceResolver();
   }

   @Override
   default ServerSslBuilder getServerSslBuilder() {
      return this.getDelegate().getServerSslBuilder();
   }

   @Override
   default ChannelOptionFactory getChannelOptionFactory() {
      return this.getDelegate().getChannelOptionFactory();
   }

   @Override
   default HttpCompressionStrategy getHttpCompressionStrategy() {
      return this.getDelegate().getHttpCompressionStrategy();
   }

   @Override
   default WebSocketBeanRegistry getWebSocketBeanRegistry() {
      return this.getDelegate().getWebSocketBeanRegistry();
   }

   @Override
   default EventLoopGroupRegistry getEventLoopGroupRegistry() {
      return this.getDelegate().getEventLoopGroupRegistry();
   }

   @Override
   default EventLoopGroup createEventLoopGroup(EventLoopGroupConfiguration config) {
      return this.getDelegate().createEventLoopGroup(config);
   }

   @Override
   default EventLoopGroup createEventLoopGroup(int numThreads, ExecutorService executorService, Integer ioRatio) {
      return this.getDelegate().createEventLoopGroup(numThreads, executorService, ioRatio);
   }

   @Override
   default ServerSocketChannel getServerSocketChannelInstance(EventLoopGroupConfiguration workerConfig) {
      return this.getDelegate().getServerSocketChannelInstance(workerConfig);
   }

   @Override
   default <E> ApplicationEventPublisher<E> getEventPublisher(Class<E> eventClass) {
      return this.getDelegate().getEventPublisher(eventClass);
   }
}
