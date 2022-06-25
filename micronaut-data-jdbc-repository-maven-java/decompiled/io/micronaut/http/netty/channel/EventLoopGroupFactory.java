package io.micronaut.http.netty.channel;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public interface EventLoopGroupFactory {
   String NATIVE = "native";

   default boolean isNative() {
      return false;
   }

   EventLoopGroup createEventLoopGroup(int threads, Executor executor, @Nullable Integer ioRatio);

   default EventLoopGroup createEventLoopGroup(EventLoopGroupConfiguration configuration, ThreadFactory threadFactory) {
      ArgumentUtils.requireNonNull("configuration", configuration);
      ArgumentUtils.requireNonNull("threadFactory", threadFactory);
      return this.createEventLoopGroup(configuration.getNumThreads(), threadFactory, (Integer)configuration.getIoRatio().orElse(null));
   }

   EventLoopGroup createEventLoopGroup(int threads, @Nullable ThreadFactory threadFactory, @Nullable Integer ioRatio);

   default EventLoopGroup createEventLoopGroup(int threads, @Nullable Integer ioRatio) {
      return this.createEventLoopGroup(threads, (ThreadFactory)null, ioRatio);
   }

   @NonNull
   Class<? extends ServerSocketChannel> serverSocketChannelClass();

   @NonNull
   default Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Domain server socket channels not supported by this transport");
   }

   @NonNull
   default Class<? extends ServerSocketChannel> serverSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
      return this.serverSocketChannelClass();
   }

   @NonNull
   default Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
      return this.domainServerSocketChannelClass();
   }

   @NonNull
   default ServerSocketChannel serverSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      try {
         return (ServerSocketChannel)this.serverSocketChannelClass(configuration).getDeclaredConstructor().newInstance();
      } catch (Exception var3) {
         throw new RuntimeException("Cannot instantiate server socket channel instance");
      }
   }

   @NonNull
   default ServerChannel domainServerSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      try {
         return (ServerChannel)this.domainServerSocketChannelClass(configuration).getDeclaredConstructor().newInstance();
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException("Cannot instantiate server socket channel instance", var3);
      }
   }

   @NonNull
   Class<? extends SocketChannel> clientSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration);

   @NonNull
   default SocketChannel clientSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
      try {
         return (SocketChannel)this.clientSocketChannelClass(configuration).getDeclaredConstructor().newInstance();
      } catch (Exception var3) {
         throw new RuntimeException("Cannot instantiate server socket channel instance");
      }
   }
}
