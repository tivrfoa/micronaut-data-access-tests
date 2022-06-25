package io.micronaut.http.netty.websocket;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Internal
public class NettyWebSocketSession implements WebSocketSession {
   public static final AttributeKey<NettyWebSocketSession> WEB_SOCKET_SESSION_KEY = AttributeKey.newInstance("micronaut.websocket.session");
   private final String id;
   private final Channel channel;
   private final HttpRequest<?> request;
   private final String protocolVersion;
   private final boolean isSecure;
   private final MediaTypeCodecRegistry codecRegistry;
   private final MutableConvertibleValues<Object> attributes;
   private final WebSocketMessageEncoder messageEncoder;

   protected NettyWebSocketSession(
      String id, Channel channel, HttpRequest<?> request, MediaTypeCodecRegistry codecRegistry, String protocolVersion, boolean isSecure
   ) {
      this.id = id;
      this.channel = channel;
      this.request = request;
      this.protocolVersion = protocolVersion;
      this.isSecure = isSecure;
      this.channel.attr(WEB_SOCKET_SESSION_KEY).set(this);
      this.codecRegistry = codecRegistry;
      this.messageEncoder = new WebSocketMessageEncoder(this.codecRegistry);
      this.attributes = (MutableConvertibleValues)request.getAttribute("micronaut.SESSION", MutableConvertibleValues.class)
         .orElseGet(MutableConvertibleValuesMap::new);
   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      return this.attributes;
   }

   @Override
   public boolean isOpen() {
      return this.channel.isOpen() && this.channel.isActive();
   }

   @Override
   public boolean isWritable() {
      return this.channel.isWritable();
   }

   @Override
   public boolean isSecure() {
      return this.isSecure;
   }

   @Override
   public Set<? extends WebSocketSession> getOpenSessions() {
      return Collections.emptySet();
   }

   @Override
   public URI getRequestURI() {
      return this.request.getUri();
   }

   @Override
   public ConvertibleMultiValues<String> getRequestParameters() {
      return this.request.getParameters();
   }

   @Override
   public String getProtocolVersion() {
      return this.protocolVersion;
   }

   @Override
   public <T> CompletableFuture<T> sendAsync(T message, MediaType mediaType) {
      if (this.isOpen()) {
         if (message != null) {
            CompletableFuture<T> future = new CompletableFuture();
            WebSocketFrame frame;
            if (message instanceof WebSocketFrame) {
               frame = (WebSocketFrame)message;
            } else {
               frame = this.messageEncoder.encodeMessage(message, mediaType);
            }

            this.channel.writeAndFlush(frame).addListener(f -> {
               if (f.isSuccess()) {
                  future.complete(message);
               } else {
                  future.completeExceptionally(new WebSocketSessionException("Send Failure: " + f.cause().getMessage(), f.cause()));
               }

            });
            return future;
         } else {
            return CompletableFuture.completedFuture(null);
         }
      } else {
         throw new WebSocketSessionException("Session closed");
      }
   }

   @Override
   public void sendSync(Object message, MediaType mediaType) {
      if (this.isOpen()) {
         if (message != null) {
            try {
               WebSocketFrame frame;
               if (message instanceof WebSocketFrame) {
                  frame = (WebSocketFrame)message;
               } else {
                  frame = this.messageEncoder.encodeMessage(message, mediaType);
               }

               this.channel.writeAndFlush(frame).sync().get();
            } catch (InterruptedException var4) {
               throw new WebSocketSessionException("Send interrupt: " + var4.getMessage(), var4);
            } catch (ExecutionException var5) {
               throw new WebSocketSessionException("Send Failure: " + var5.getMessage(), var5);
            }
         }

      } else {
         throw new WebSocketSessionException("Session closed");
      }
   }

   public <T> Flux<T> send(T message, MediaType mediaType) {
      return message == null ? Flux.empty() : Flux.create(emitter -> {
         if (!this.isOpen()) {
            emitter.error(new WebSocketSessionException("Session closed"));
         } else {
            WebSocketFrame frame;
            if (message instanceof WebSocketFrame) {
               frame = (WebSocketFrame)message;
            } else {
               frame = this.messageEncoder.encodeMessage(message, mediaType);
            }

            ChannelFuture channelFuture = this.channel.writeAndFlush(frame);
            channelFuture.addListener(future -> {
               if (future.isSuccess()) {
                  emitter.next(message);
                  emitter.complete();
               } else {
                  emitter.error(new WebSocketSessionException("Send Failure: " + future.cause().getMessage(), future.cause()));
               }

            });
         }

      }, FluxSink.OverflowStrategy.ERROR);
   }

   @NonNull
   @Override
   public CompletableFuture<?> sendPingAsync(@NonNull byte[] content) {
      if (this.isOpen()) {
         ByteBuf messageBuffer = this.channel.alloc().buffer(content.length);
         messageBuffer.writeBytes(content);
         PingWebSocketFrame frame = new PingWebSocketFrame(messageBuffer);
         CompletableFuture<Object> future = new CompletableFuture();
         this.channel.writeAndFlush(frame).addListener(f -> {
            if (f.isSuccess()) {
               future.complete(null);
            } else {
               future.completeExceptionally(new WebSocketSessionException("Send Failure: " + f.cause().getMessage(), f.cause()));
            }

         });
         return future;
      } else {
         throw new WebSocketSessionException("Session closed");
      }
   }

   @Override
   public void close() {
      this.close(CloseReason.NORMAL);
   }

   @Override
   public void close(CloseReason closeReason) {
      if (this.channel.isOpen()) {
         this.channel.writeAndFlush(new CloseWebSocketFrame(closeReason.getCode(), closeReason.getReason())).addListener(future -> this.channel.close());
      }

   }

   public String toString() {
      return "WebSocket Session: " + this.getId();
   }

   @Override
   public MutableConvertibleValues<Object> put(CharSequence key, @Nullable Object value) {
      return this.attributes.put(key, value);
   }

   @Override
   public MutableConvertibleValues<Object> remove(CharSequence key) {
      return this.attributes.remove(key);
   }

   @Override
   public MutableConvertibleValues<Object> clear() {
      return this.attributes.clear();
   }

   @Override
   public Set<String> names() {
      return this.attributes.names();
   }

   @Override
   public Collection<Object> values() {
      return this.attributes.values();
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      return this.attributes.get(name, conversionContext);
   }
}
