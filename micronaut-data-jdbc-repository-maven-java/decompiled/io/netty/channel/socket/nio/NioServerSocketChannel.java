package io.netty.channel.socket.nio;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.nio.AbstractNioMessageChannel;
import io.netty.channel.socket.DefaultServerSocketChannelConfig;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannelConfig;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.Map;

public class NioServerSocketChannel extends AbstractNioMessageChannel implements ServerSocketChannel {
   private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
   private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioServerSocketChannel.class);
   private static final Method OPEN_SERVER_SOCKET_CHANNEL_WITH_FAMILY = SelectorProviderUtil.findOpenMethod("openServerSocketChannel");
   private final ServerSocketChannelConfig config = new NioServerSocketChannel.NioServerSocketChannelConfig(this, this.javaChannel().socket());

   private static java.nio.channels.ServerSocketChannel newChannel(SelectorProvider provider, InternetProtocolFamily family) {
      try {
         java.nio.channels.ServerSocketChannel channel = SelectorProviderUtil.newChannel(OPEN_SERVER_SOCKET_CHANNEL_WITH_FAMILY, provider, family);
         return channel == null ? provider.openServerSocketChannel() : channel;
      } catch (IOException var3) {
         throw new ChannelException("Failed to open a socket.", var3);
      }
   }

   public NioServerSocketChannel() {
      this(DEFAULT_SELECTOR_PROVIDER);
   }

   public NioServerSocketChannel(SelectorProvider provider) {
      this(provider, null);
   }

   public NioServerSocketChannel(SelectorProvider provider, InternetProtocolFamily family) {
      this(newChannel(provider, family));
   }

   public NioServerSocketChannel(java.nio.channels.ServerSocketChannel channel) {
      super(null, channel, 16);
   }

   @Override
   public InetSocketAddress localAddress() {
      return (InetSocketAddress)super.localAddress();
   }

   @Override
   public ChannelMetadata metadata() {
      return METADATA;
   }

   @Override
   public ServerSocketChannelConfig config() {
      return this.config;
   }

   @Override
   public boolean isActive() {
      return this.isOpen() && this.javaChannel().socket().isBound();
   }

   @Override
   public InetSocketAddress remoteAddress() {
      return null;
   }

   protected java.nio.channels.ServerSocketChannel javaChannel() {
      return (java.nio.channels.ServerSocketChannel)super.javaChannel();
   }

   @Override
   protected SocketAddress localAddress0() {
      return SocketUtils.localSocketAddress(this.javaChannel().socket());
   }

   @SuppressJava6Requirement(
      reason = "Usage guarded by java version check"
   )
   @Override
   protected void doBind(SocketAddress localAddress) throws Exception {
      if (PlatformDependent.javaVersion() >= 7) {
         this.javaChannel().bind(localAddress, this.config.getBacklog());
      } else {
         this.javaChannel().socket().bind(localAddress, this.config.getBacklog());
      }

   }

   @Override
   protected void doClose() throws Exception {
      this.javaChannel().close();
   }

   @Override
   protected int doReadMessages(List<Object> buf) throws Exception {
      SocketChannel ch = SocketUtils.accept(this.javaChannel());

      try {
         if (ch != null) {
            buf.add(new NioSocketChannel(this, ch));
            return 1;
         }
      } catch (Throwable var6) {
         logger.warn("Failed to create a new channel from an accepted socket.", var6);

         try {
            ch.close();
         } catch (Throwable var5) {
            logger.warn("Failed to close a socket.", var5);
         }
      }

      return 0;
   }

   @Override
   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   protected void doFinishConnect() throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   protected SocketAddress remoteAddress0() {
      return null;
   }

   @Override
   protected void doDisconnect() throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   protected final Object filterOutboundMessage(Object msg) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   protected boolean closeOnReadError(Throwable cause) {
      return super.closeOnReadError(cause);
   }

   private final class NioServerSocketChannelConfig extends DefaultServerSocketChannelConfig {
      private NioServerSocketChannelConfig(NioServerSocketChannel channel, ServerSocket javaSocket) {
         super(channel, javaSocket);
      }

      @Override
      protected void autoReadCleared() {
         NioServerSocketChannel.this.clearReadPending();
      }

      @Override
      public <T> boolean setOption(ChannelOption<T> option, T value) {
         return PlatformDependent.javaVersion() >= 7 && option instanceof NioChannelOption
            ? NioChannelOption.setOption(this.jdkChannel(), (NioChannelOption<T>)option, value)
            : super.setOption(option, value);
      }

      @Override
      public <T> T getOption(ChannelOption<T> option) {
         return (T)(PlatformDependent.javaVersion() >= 7 && option instanceof NioChannelOption
            ? NioChannelOption.getOption(this.jdkChannel(), (NioChannelOption<T>)option)
            : super.getOption(option));
      }

      @Override
      public Map<ChannelOption<?>, Object> getOptions() {
         return PlatformDependent.javaVersion() >= 7 ? this.getOptions(super.getOptions(), NioChannelOption.getOptions(this.jdkChannel())) : super.getOptions();
      }

      private java.nio.channels.ServerSocketChannel jdkChannel() {
         return ((NioServerSocketChannel)this.channel).javaChannel();
      }
   }
}
