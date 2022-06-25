package io.micronaut.http.netty.content;

import io.micronaut.core.annotation.Internal;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import java.nio.charset.StandardCharsets;

@Internal
public class HttpContentUtil {
   public static final byte[] OPEN_BRACKET = "[".getBytes(StandardCharsets.UTF_8);
   public static final byte[] CLOSE_BRACKET = "]".getBytes(StandardCharsets.UTF_8);
   public static final byte[] COMMA = ",".getBytes(StandardCharsets.UTF_8);

   public static HttpContent closeBracket() {
      return new DefaultHttpContent(Unpooled.wrappedBuffer(CLOSE_BRACKET));
   }

   public static HttpContent prefixComma(HttpContent httpContent) {
      CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer(2);
      compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(COMMA));
      compositeByteBuf.addComponent(true, httpContent.content());
      return httpContent.replace(compositeByteBuf);
   }

   public static HttpContent prefixOpenBracket(HttpContent httpContent) {
      CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer(2);
      compositeByteBuf.addComponent(true, Unpooled.wrappedBuffer(OPEN_BRACKET));
      compositeByteBuf.addComponent(true, httpContent.content());
      return httpContent.replace(compositeByteBuf);
   }
}
