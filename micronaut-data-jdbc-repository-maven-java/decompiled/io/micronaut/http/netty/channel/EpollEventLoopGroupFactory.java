package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

@Singleton
@Requires(
   classes = {Epoll.class},
   condition = EpollAvailabilityCondition.class
)
@Internal
@Named("native")
@BootstrapContextCompatible
public class EpollEventLoopGroupFactory implements EventLoopGroupFactory {
   @Override
   public EventLoopGroup createEventLoopGroup(int threads, ThreadFactory threadFactory, @Nullable Integer ioRatio) {
      return new EpollEventLoopGroup(threads, threadFactory);
   }

   @Override
   public EventLoopGroup createEventLoopGroup(int threads, Executor executor, @Nullable Integer ioRatio) {
      return new EpollEventLoopGroup(threads, executor);
   }

   @Override
   public Class<? extends ServerSocketChannel> serverSocketChannelClass() {
      return EpollServerSocketChannel.class;
   }

   @Override
   public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass() throws UnsupportedOperationException {
      try {
         return EpollServerDomainSocketChannel.class;
      } catch (NoClassDefFoundError var2) {
         throw new UnsupportedOperationException(var2);
      }
   }

   @NonNull
   public EpollServerSocketChannel serverSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      return new EpollServerSocketChannel();
   }

   @Override
   public ServerChannel domainServerSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      try {
         return new EpollServerDomainSocketChannel();
      } catch (NoClassDefFoundError var3) {
         throw new UnsupportedOperationException(var3);
      }
   }

   @NonNull
   @Override
   public Class<? extends SocketChannel> clientSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
      return EpollSocketChannel.class;
   }

   @Override
   public SocketChannel clientSocketChannelInstance(EventLoopGroupConfiguration configuration) {
      return new EpollSocketChannel();
   }

   @Override
   public boolean isNative() {
      return true;
   }
}
