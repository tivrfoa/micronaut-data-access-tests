package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentEncoder;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import java.util.List;

@Internal
public class SmartHttpContentCompressor extends HttpContentCompressor {
   private final HttpCompressionStrategy httpCompressionStrategy;
   private boolean skipEncoding = false;

   SmartHttpContentCompressor(HttpCompressionStrategy httpCompressionStrategy) {
      super(httpCompressionStrategy.getCompressionLevel());
      this.httpCompressionStrategy = httpCompressionStrategy;
   }

   public boolean shouldSkip(HttpResponse response) {
      return !this.httpCompressionStrategy.shouldCompress(response);
   }

   @Override
   protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
      if (msg instanceof HttpResponse) {
         HttpResponse res = (HttpResponse)msg;
         this.skipEncoding = this.shouldSkip(res);
      }

      super.encode(ctx, msg, out);
   }

   @Override
   protected HttpContentEncoder.Result beginEncode(HttpResponse headers, String acceptEncoding) throws Exception {
      return this.skipEncoding ? null : super.beginEncode(headers, acceptEncoding);
   }
}
