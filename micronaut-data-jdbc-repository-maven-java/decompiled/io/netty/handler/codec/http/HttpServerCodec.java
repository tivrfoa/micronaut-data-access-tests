package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public final class HttpServerCodec extends CombinedChannelDuplexHandler<HttpRequestDecoder, HttpResponseEncoder> implements HttpServerUpgradeHandler.SourceCodec {
   private final Queue<HttpMethod> queue = new ArrayDeque();

   public HttpServerCodec() {
      this(4096, 8192, 8192);
   }

   public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
      this.init(
         new HttpServerCodec.HttpServerRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize), new HttpServerCodec.HttpServerResponseEncoder()
      );
   }

   public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
      this.init(
         new HttpServerCodec.HttpServerRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders),
         new HttpServerCodec.HttpServerResponseEncoder()
      );
   }

   public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders, int initialBufferSize) {
      this.init(
         new HttpServerCodec.HttpServerRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize),
         new HttpServerCodec.HttpServerResponseEncoder()
      );
   }

   public HttpServerCodec(
      int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders, int initialBufferSize, boolean allowDuplicateContentLengths
   ) {
      this.init(
         new HttpServerCodec.HttpServerRequestDecoder(
            maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize, allowDuplicateContentLengths
         ),
         new HttpServerCodec.HttpServerResponseEncoder()
      );
   }

   public HttpServerCodec(
      int maxInitialLineLength,
      int maxHeaderSize,
      int maxChunkSize,
      boolean validateHeaders,
      int initialBufferSize,
      boolean allowDuplicateContentLengths,
      boolean allowPartialChunks
   ) {
      this.init(
         new HttpServerCodec.HttpServerRequestDecoder(
            maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize, allowDuplicateContentLengths, allowPartialChunks
         ),
         new HttpServerCodec.HttpServerResponseEncoder()
      );
   }

   @Override
   public void upgradeFrom(ChannelHandlerContext ctx) {
      ctx.pipeline().remove(this);
   }

   private final class HttpServerRequestDecoder extends HttpRequestDecoder {
      HttpServerRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
         super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
      }

      HttpServerRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
         super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders);
      }

      HttpServerRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders, int initialBufferSize) {
         super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize);
      }

      HttpServerRequestDecoder(
         int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders, int initialBufferSize, boolean allowDuplicateContentLengths
      ) {
         super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize, allowDuplicateContentLengths);
      }

      HttpServerRequestDecoder(
         int maxInitialLineLength,
         int maxHeaderSize,
         int maxChunkSize,
         boolean validateHeaders,
         int initialBufferSize,
         boolean allowDuplicateContentLengths,
         boolean allowPartialChunks
      ) {
         super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize, allowDuplicateContentLengths, allowPartialChunks);
      }

      @Override
      protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
         int oldSize = out.size();
         super.decode(ctx, buffer, out);
         int size = out.size();

         for(int i = oldSize; i < size; ++i) {
            Object obj = out.get(i);
            if (obj instanceof HttpRequest) {
               HttpServerCodec.this.queue.add(((HttpRequest)obj).method());
            }
         }

      }
   }

   private final class HttpServerResponseEncoder extends HttpResponseEncoder {
      private HttpMethod method;

      private HttpServerResponseEncoder() {
      }

      @Override
      protected void sanitizeHeadersBeforeEncode(HttpResponse msg, boolean isAlwaysEmpty) {
         if (!isAlwaysEmpty && HttpMethod.CONNECT.equals(this.method) && msg.status().codeClass() == HttpStatusClass.SUCCESS) {
            msg.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
         } else {
            super.sanitizeHeadersBeforeEncode(msg, isAlwaysEmpty);
         }
      }

      @Override
      protected boolean isContentAlwaysEmpty(HttpResponse msg) {
         this.method = (HttpMethod)HttpServerCodec.this.queue.poll();
         return HttpMethod.HEAD.equals(this.method) || super.isContentAlwaysEmpty(msg);
      }
   }
}
