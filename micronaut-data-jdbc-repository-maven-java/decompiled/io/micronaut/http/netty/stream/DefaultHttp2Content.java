package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http2.Http2Stream;

@Internal
public final class DefaultHttp2Content extends DefaultHttpContent implements Http2Content {
   private final Http2Stream stream;

   public DefaultHttp2Content(ByteBuf content, Http2Stream stream) {
      super(content);
      this.stream = stream;
   }

   @Override
   public Http2Stream stream() {
      return this.stream;
   }
}
