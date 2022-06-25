package io.micronaut.http.client.netty;

import io.micronaut.scheduling.instrument.Instrumentation;
import io.micronaut.scheduling.instrument.InvocationInstrumenter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

abstract class SimpleChannelInboundHandlerInstrumented<I> extends SimpleChannelInboundHandler<I> {
   private final InvocationInstrumenter instrumenter;

   SimpleChannelInboundHandlerInstrumented(InvocationInstrumenter instrumenter) {
      this.instrumenter = instrumenter;
   }

   SimpleChannelInboundHandlerInstrumented(InvocationInstrumenter instrumenter, boolean autoRelease) {
      super(autoRelease);
      this.instrumenter = instrumenter;
   }

   protected abstract void channelReadInstrumented(ChannelHandlerContext ctx, I msg) throws Exception;

   @Override
   protected final void channelRead0(ChannelHandlerContext ctx, I msg) throws Exception {
      try (Instrumentation ignored = this.instrumenter.newInstrumentation()) {
         this.channelReadInstrumented(ctx, msg);
      }

   }
}
