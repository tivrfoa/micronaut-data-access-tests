package io.netty.handler.codec.http;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.Brotli;
import io.netty.handler.codec.compression.BrotliDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;

public class HttpContentDecompressor extends HttpContentDecoder {
   private final boolean strict;

   public HttpContentDecompressor() {
      this(false);
   }

   public HttpContentDecompressor(boolean strict) {
      this.strict = strict;
   }

   @Override
   protected EmbeddedChannel newContentDecoder(String contentEncoding) throws Exception {
      if (HttpHeaderValues.GZIP.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_GZIP.contentEqualsIgnoreCase(contentEncoding)) {
         return new EmbeddedChannel(
            this.ctx.channel().id(),
            this.ctx.channel().metadata().hasDisconnect(),
            this.ctx.channel().config(),
            ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP)
         );
      } else if (HttpHeaderValues.DEFLATE.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_DEFLATE.contentEqualsIgnoreCase(contentEncoding)) {
         ZlibWrapper wrapper = this.strict ? ZlibWrapper.ZLIB : ZlibWrapper.ZLIB_OR_NONE;
         return new EmbeddedChannel(
            this.ctx.channel().id(), this.ctx.channel().metadata().hasDisconnect(), this.ctx.channel().config(), ZlibCodecFactory.newZlibDecoder(wrapper)
         );
      } else {
         return Brotli.isAvailable() && HttpHeaderValues.BR.contentEqualsIgnoreCase(contentEncoding)
            ? new EmbeddedChannel(this.ctx.channel().id(), this.ctx.channel().metadata().hasDisconnect(), this.ctx.channel().config(), new BrotliDecoder())
            : null;
      }
   }
}
