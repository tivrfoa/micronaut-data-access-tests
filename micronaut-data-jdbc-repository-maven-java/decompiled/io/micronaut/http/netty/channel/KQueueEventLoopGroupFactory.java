package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

@Singleton
@Internal
@Requires(
   classes = {KQueue.class},
   condition = KQueueAvailabilityCondition.class
)
@Named("native")
@BootstrapContextCompatible
public class KQueueEventLoopGroupFactory implements EventLoopGroupFactory {
   @Override
   public EventLoopGroup createEventLoopGroup(int threads, ThreadFactory threadFactory, @Nullable Integer ioRatio) {
      return withIoRatio(new KQueueEventLoopGroup(threads, threadFactory), ioRatio);
   }

   @Override
   public EventLoopGroup createEventLoopGroup(int threads, Executor executor, @Nullable Integer ioRatio) {
      return withIoRatio(new KQueueEventLoopGroup(threads, executor), ioRatio);
   }

   @Override
   public boolean isNative() {
      return true;
   }

   @Override
   public Class<? extends ServerSocketChannel> serverSocketChannelClass() {
      return KQueueServerSocketChannel.class;
   }

   @Override
   public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass() throws UnsupportedOperationException {
      try {
         return KQueueServerDomainSocketChannel.class;
      } catch (NoClassDefFoundError var2) {
         throw new UnsupportedOperationException(var2);
      }
   }

   public KQueueServerSocketChannel serverSocketChannelInstance(EventLoopGroupConfiguration configuration) {
      return new KQueueServerSocketChannel();
   }

   @Override
   public ServerChannel domainServerSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      try {
         return new KQueueServerDomainSocketChannel();
      } catch (NoClassDefFoundError var3) {
         throw new UnsupportedOperationException(var3);
      }
   }

   @NonNull
   @Override
   public Class<? extends SocketChannel> clientSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
      return KQueueSocketChannel.class;
   }

   @Override
   public SocketChannel clientSocketChannelInstance(EventLoopGroupConfiguration configuration) {
      return new KQueueSocketChannel();
   }

   private static KQueueEventLoopGroup withIoRatio(KQueueEventLoopGroup group, @Nullable Integer ioRatio) {
      if (ioRatio != null) {
         group.setIoRatio(ioRatio);
      }

      return group;
   }
}
