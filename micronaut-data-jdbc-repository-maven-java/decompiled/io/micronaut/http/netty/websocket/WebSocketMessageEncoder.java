package io.micronaut.http.netty.websocket;

import io.micronaut.buffer.netty.NettyByteBufferFactory;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import jakarta.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.Optional;

@Singleton
public class WebSocketMessageEncoder {
   private final MediaTypeCodecRegistry codecRegistry;

   public WebSocketMessageEncoder(MediaTypeCodecRegistry codecRegistry) {
      this.codecRegistry = codecRegistry;
   }

   public WebSocketFrame encodeMessage(Object message, MediaType mediaType) {
      if (message instanceof byte[]) {
         return new BinaryWebSocketFrame(Unpooled.wrappedBuffer((byte[])message));
      } else if (ClassUtils.isJavaLangType(message.getClass()) || message instanceof CharSequence) {
         String s = message.toString();
         return new TextWebSocketFrame(s);
      } else if (message instanceof ByteBuf) {
         return new BinaryWebSocketFrame(((ByteBuf)message).slice());
      } else if (message instanceof ByteBuffer) {
         return new BinaryWebSocketFrame(Unpooled.wrappedBuffer((ByteBuffer)message));
      } else {
         Optional<MediaTypeCodec> codec = this.codecRegistry.findCodec(mediaType != null ? mediaType : MediaType.APPLICATION_JSON_TYPE);
         if (codec.isPresent()) {
            io.micronaut.core.io.buffer.ByteBuffer encoded = ((MediaTypeCodec)codec.get())
               .encode(message, new NettyByteBufferFactory(UnpooledByteBufAllocator.DEFAULT));
            return new TextWebSocketFrame((ByteBuf)encoded.asNativeBuffer());
         } else {
            throw new WebSocketSessionException("Unable to encode WebSocket message: " + message);
         }
      }
   }
}
