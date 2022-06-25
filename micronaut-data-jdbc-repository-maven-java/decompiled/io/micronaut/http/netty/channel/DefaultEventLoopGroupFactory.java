package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.netty.configuration.NettyGlobalConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import io.netty.util.ResourceLeakDetector;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

@Primary
@Singleton
@BootstrapContextCompatible
public class DefaultEventLoopGroupFactory implements EventLoopGroupFactory {
   private final EventLoopGroupFactory nativeFactory;
   private final EventLoopGroupFactory defaultFactory;

   public DefaultEventLoopGroupFactory(NioEventLoopGroupFactory nioEventLoopGroupFactory, @Nullable @Named("native") EventLoopGroupFactory nativeFactory) {
      this(nioEventLoopGroupFactory, nativeFactory, null);
   }

   @Inject
   public DefaultEventLoopGroupFactory(
      NioEventLoopGroupFactory nioEventLoopGroupFactory,
      @Nullable @Named("native") EventLoopGroupFactory nativeFactory,
      @Nullable NettyGlobalConfiguration nettyGlobalConfiguration
   ) {
      this.defaultFactory = nioEventLoopGroupFactory;
      this.nativeFactory = nativeFactory != null ? nativeFactory : this.defaultFactory;
      if (nettyGlobalConfiguration != null && nettyGlobalConfiguration.getResourceLeakDetectorLevel() != null) {
         ResourceLeakDetector.setLevel(nettyGlobalConfiguration.getResourceLeakDetectorLevel());
      }

   }

   @Override
   public EventLoopGroup createEventLoopGroup(EventLoopGroupConfiguration configuration, ThreadFactory threadFactory) {
      ArgumentUtils.requireNonNull("configuration", configuration);
      ArgumentUtils.requireNonNull("threadFactory", threadFactory);
      return this.getFactory(configuration).createEventLoopGroup(configuration, threadFactory);
   }

   @Override
   public EventLoopGroup createEventLoopGroup(int threads, Executor executor, @Nullable Integer ioRatio) {
      return this.nativeFactory.createEventLoopGroup(threads, executor, ioRatio);
   }

   @Override
   public EventLoopGroup createEventLoopGroup(int threads, @Nullable ThreadFactory threadFactory, @Nullable Integer ioRatio) {
      return this.nativeFactory.createEventLoopGroup(threads, threadFactory, ioRatio);
   }

   @Override
   public Class<? extends ServerSocketChannel> serverSocketChannelClass() {
      return this.nativeFactory.serverSocketChannelClass();
   }

   @Override
   public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass() throws UnsupportedOperationException {
      return this.nativeFactory.domainServerSocketChannelClass();
   }

   @NonNull
   @Override
   public Class<? extends ServerSocketChannel> serverSocketChannelClass(EventLoopGroupConfiguration configuration) {
      return this.getFactory(configuration).serverSocketChannelClass(configuration);
   }

   @NonNull
   @Override
   public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass(EventLoopGroupConfiguration configuration) {
      return this.getFactory(configuration).domainServerSocketChannelClass(configuration);
   }

   @Override
   public ServerSocketChannel serverSocketChannelInstance(EventLoopGroupConfiguration configuration) {
      return this.getFactory(configuration).serverSocketChannelInstance(configuration);
   }

   @Override
   public ServerChannel domainServerSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      return this.getFactory(configuration).domainServerSocketChannelInstance(configuration);
   }

   @NonNull
   @Override
   public Class<? extends SocketChannel> clientSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
      return this.getFactory(configuration).clientSocketChannelClass(configuration);
   }

   private EventLoopGroupFactory getFactory(@Nullable EventLoopGroupConfiguration configuration) {
      return configuration != null && configuration.isPreferNativeTransport() ? this.nativeFactory : this.defaultFactory;
   }

   @NonNull
   @Override
   public SocketChannel clientSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      return this.getFactory(configuration).clientSocketChannelInstance(configuration);
   }
}
