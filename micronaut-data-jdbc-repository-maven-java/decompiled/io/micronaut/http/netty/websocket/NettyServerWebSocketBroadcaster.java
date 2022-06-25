package io.micronaut.http.netty.websocket;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroupException;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import jakarta.inject.Singleton;
import java.nio.channels.ClosedChannelException;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Singleton
@Requires(
   beans = {WebSocketSessionRepository.class}
)
public class NettyServerWebSocketBroadcaster implements WebSocketBroadcaster {
   private final WebSocketMessageEncoder webSocketMessageEncoder;
   private final WebSocketSessionRepository webSocketSessionRepository;

   public NettyServerWebSocketBroadcaster(WebSocketMessageEncoder webSocketMessageEncoder, WebSocketSessionRepository webSocketSessionRepository) {
      this.webSocketMessageEncoder = webSocketMessageEncoder;
      this.webSocketSessionRepository = webSocketSessionRepository;
   }

   @Override
   public <T> void broadcastSync(T message, MediaType mediaType, Predicate<WebSocketSession> filter) {
      WebSocketFrame frame = this.webSocketMessageEncoder.encodeMessage(message, mediaType);

      try {
         this.webSocketSessionRepository.getChannelGroup().writeAndFlush(frame, ch -> {
            Attribute<NettyWebSocketSession> attr = ch.attr(NettyWebSocketSession.WEB_SOCKET_SESSION_KEY);
            NettyWebSocketSession s = attr.get();
            return s != null && s.isOpen() && filter.test(s);
         }).sync();
      } catch (InterruptedException var6) {
         throw new WebSocketSessionException("Broadcast Interrupted");
      }
   }

   @Override
   public <T> Publisher<T> broadcast(T message, MediaType mediaType, Predicate<WebSocketSession> filter) {
      return Flux.create(emitter -> {
         try {
            WebSocketFrame frame = this.webSocketMessageEncoder.encodeMessage(message, mediaType);
            this.webSocketSessionRepository.getChannelGroup().writeAndFlush(frame, ch -> {
               Attribute<NettyWebSocketSession> attr = ch.attr(NettyWebSocketSession.WEB_SOCKET_SESSION_KEY);
               NettyWebSocketSession s = attr.get();
               return s != null && s.isOpen() && filter.test(s);
            }).addListener(future -> {
               if (!future.isSuccess()) {
                  Throwable cause = this.extractBroadcastFailure(future.cause());
                  if (cause != null) {
                     emitter.error(new WebSocketSessionException("Broadcast Failure: " + cause.getMessage(), cause));
                     return;
                  }
               }

               emitter.next(message);
               emitter.complete();
            });
         } catch (Throwable var6) {
            emitter.error(new WebSocketSessionException("Broadcast Failure: " + var6.getMessage(), var6));
         }

      }, FluxSink.OverflowStrategy.BUFFER);
   }

   @Nullable
   private Throwable extractBroadcastFailure(Throwable failure) {
      if (failure instanceof ChannelGroupException) {
         Throwable singleCause = null;

         for(Entry<Channel, Throwable> entry : (ChannelGroupException)failure) {
            Throwable entryCause = this.extractBroadcastFailure((Throwable)entry.getValue());
            if (entryCause != null) {
               if (singleCause != null) {
                  return failure;
               }

               singleCause = entryCause;
            }
         }

         return singleCause;
      } else {
         return failure instanceof ClosedChannelException ? null : failure;
      }
   }
}
