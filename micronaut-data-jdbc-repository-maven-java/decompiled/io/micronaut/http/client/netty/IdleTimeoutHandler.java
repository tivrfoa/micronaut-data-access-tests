package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
@Internal
final class IdleTimeoutHandler extends ChannelDuplexHandler {
   static final ChannelInboundHandler INSTANCE = new IdleTimeoutHandler();

   private IdleTimeoutHandler() {
   }

   @Override
   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
         IdleStateEvent e = (IdleStateEvent)evt;
         if (e.state() == IdleState.READER_IDLE || e.state() == IdleState.WRITER_IDLE) {
            ctx.close();
         }
      }

   }
}
