package io.micronaut.http.client.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConnectTTLHandler extends ChannelDuplexHandler {
   public static final AttributeKey<Boolean> RELEASE_CHANNEL = AttributeKey.newInstance("release_channel");
   private final Long connectionTtlMillis;
   private ScheduledFuture<?> channelKiller;

   public ConnectTTLHandler(Long connectionTtlMillis) {
      if (connectionTtlMillis <= 0L) {
         throw new IllegalArgumentException("connectTTL must be positive");
      } else {
         this.connectionTtlMillis = connectionTtlMillis;
      }
   }

   @Override
   public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      super.handlerAdded(ctx);
      this.channelKiller = ctx.channel().eventLoop().schedule(() -> this.closeChannel(ctx), this.connectionTtlMillis, TimeUnit.MILLISECONDS);
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) {
      this.channelKiller.cancel(false);
   }

   private void closeChannel(ChannelHandlerContext ctx) {
      if (ctx.channel().isOpen()) {
         ctx.channel().attr(RELEASE_CHANNEL).set(true);
      }

   }
}
