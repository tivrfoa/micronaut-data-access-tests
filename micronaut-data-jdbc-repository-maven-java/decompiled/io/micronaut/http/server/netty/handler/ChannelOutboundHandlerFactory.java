package io.micronaut.http.server.netty.handler;

import io.netty.channel.ChannelOutboundHandler;

public interface ChannelOutboundHandlerFactory {
   ChannelOutboundHandler build();
}
