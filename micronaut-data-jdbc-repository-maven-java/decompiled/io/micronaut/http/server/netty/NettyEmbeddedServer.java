package io.micronaut.http.server.netty;

import io.micronaut.http.netty.channel.ChannelPipelineCustomizer;
import io.micronaut.http.netty.websocket.WebSocketSessionRepository;
import io.micronaut.runtime.context.scope.refresh.RefreshEventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import java.util.Collections;
import java.util.Set;

public interface NettyEmbeddedServer extends EmbeddedServer, WebSocketSessionRepository, ChannelPipelineCustomizer, RefreshEventListener {
   default Set<Integer> getBoundPorts() {
      return Collections.singleton(this.getPort());
   }

   default NettyEmbeddedServer start() {
      return (NettyEmbeddedServer)EmbeddedServer.super.start();
   }

   default NettyEmbeddedServer stop() {
      return (NettyEmbeddedServer)EmbeddedServer.super.stop();
   }
}
