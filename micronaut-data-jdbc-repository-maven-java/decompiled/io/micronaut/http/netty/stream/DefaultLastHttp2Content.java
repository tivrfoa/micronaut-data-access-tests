package io.micronaut.http.netty.stream;

import io.micronaut.http.netty.LastHttp2Content;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http2.Http2Stream;

final class DefaultLastHttp2Content extends DefaultLastHttpContent implements LastHttp2Content {
   private final Http2Stream stream;

   public DefaultLastHttp2Content(Http2Stream stream) {
      this.stream = stream;
   }

   public DefaultLastHttp2Content(ByteBuf content, Http2Stream stream) {
      super(content);
      this.stream = stream;
   }

   public DefaultLastHttp2Content(ByteBuf content, boolean validateHeaders, Http2Stream stream) {
      super(content, validateHeaders);
      this.stream = stream;
   }

   @Override
   public Http2Stream stream() {
      return this.stream;
   }
}
