package io.micronaut.http.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public interface WebSocketSessionRepository {
   void addChannel(Channel channel);

   void removeChannel(Channel channel);

   ChannelGroup getChannelGroup();
}
