package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.TypeHint;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.ThreadFactory;

@Singleton
@Factory
@TypeHint(
   value = {NioServerSocketChannel.class, NioSocketChannel.class},
   typeNames = {"sun.security.ssl.SSLContextImpl$TLSContext"},
   accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_DECLARED_FIELDS, TypeHint.AccessType.ALL_PUBLIC_CONSTRUCTORS}
)
@BootstrapContextCompatible
public class NettyThreadFactory {
   public static final String NAME = "netty";
   public static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(
      1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2)
   );

   @Singleton
   @Named("netty")
   @BootstrapContextCompatible
   protected ThreadFactory nettyThreadFactory() {
      return new DefaultThreadFactory("default-" + DefaultThreadFactory.toPoolName(NioEventLoopGroup.class));
   }
}
