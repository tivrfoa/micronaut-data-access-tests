package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
@Internal
final class IdlingConnectionHandler extends ChannelInboundHandlerAdapter {
   static final ChannelInboundHandler INSTANCE = new IdlingConnectionHandler();

   private IdlingConnectionHandler() {
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      ReferenceCountUtil.release(msg);
      ctx.close();
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      ctx.close();
   }
}
