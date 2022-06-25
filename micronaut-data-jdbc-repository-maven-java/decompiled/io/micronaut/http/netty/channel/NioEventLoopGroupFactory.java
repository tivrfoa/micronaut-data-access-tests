package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import jakarta.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

@Internal
@Singleton
@BootstrapContextCompatible
public class NioEventLoopGroupFactory implements EventLoopGroupFactory {
   @Override
   public EventLoopGroup createEventLoopGroup(int threads, ThreadFactory threadFactory, @Nullable Integer ioRatio) {
      return withIoRatio(new NioEventLoopGroup(threads, threadFactory), ioRatio);
   }

   @Override
   public EventLoopGroup createEventLoopGroup(int threads, Executor executor, @Nullable Integer ioRatio) {
      return withIoRatio(new NioEventLoopGroup(threads, executor), ioRatio);
   }

   @Override
   public Class<? extends ServerSocketChannel> serverSocketChannelClass() {
      return NioServerSocketChannel.class;
   }

   @Override
   public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("UNIX domain sockets are not supported by the NIO implementation right now, please switch to epoll or kqueue");
   }

   public NioServerSocketChannel serverSocketChannelInstance(EventLoopGroupConfiguration configuration) {
      return new NioServerSocketChannel();
   }

   @NonNull
   @Override
   public Class<? extends SocketChannel> clientSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
      return NioSocketChannel.class;
   }

   @Override
   public SocketChannel clientSocketChannelInstance(EventLoopGroupConfiguration configuration) {
      return new NioSocketChannel();
   }

   private static NioEventLoopGroup withIoRatio(NioEventLoopGroup group, @Nullable Integer ioRatio) {
      if (ioRatio != null) {
         group.setIoRatio(ioRatio);
      }

      return group;
   }
}
