package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.util.ReferenceCountUtil;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public final class HttpClientCodec extends CombinedChannelDuplexHandler<HttpResponseDecoder, HttpRequestEncoder> implements HttpClientUpgradeHandler.SourceCodec {
   public static final boolean DEFAULT_FAIL_ON_MISSING_RESPONSE = false;
   public static final boolean DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST = false;
   private final Queue<HttpMethod> queue = new ArrayDeque();
   private final boolean parseHttpAfterConnectRequest;
   private boolean done;
   private final AtomicLong requestResponseCounter = new AtomicLong();
   private final boolean failOnMissingResponse;

   public HttpClientCodec() {
      this(4096, 8192, 8192, false);
   }

   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
      this(maxInitialLineLength, maxHeaderSize, maxChunkSize, false);
   }

   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse) {
      this(maxInitialLineLength, maxHeaderSize, maxChunkSize, failOnMissingResponse, true);
   }

   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse, boolean validateHeaders) {
      this(maxInitialLineLength, maxHeaderSize, maxChunkSize, failOnMissingResponse, validateHeaders, false);
   }

   public HttpClientCodec(
      int maxInitialLineLength,
      int maxHeaderSize,
      int maxChunkSize,
      boolean failOnMissingResponse,
      boolean validateHeaders,
      boolean parseHttpAfterConnectRequest
   ) {
      this.init(new HttpClientCodec.Decoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders), new HttpClientCodec.Encoder());
      this.failOnMissingResponse = failOnMissingResponse;
      this.parseHttpAfterConnectRequest = parseHttpAfterConnectRequest;
   }

   public HttpClientCodec(
      int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse, boolean validateHeaders, int initialBufferSize
   ) {
      this(maxInitialLineLength, maxHeaderSize, maxChunkSize, failOnMissingResponse, validateHeaders, initialBufferSize, false);
   }

   public HttpClientCodec(
      int maxInitialLineLength,
      int maxHeaderSize,
      int maxChunkSize,
      boolean failOnMissingResponse,
      boolean validateHeaders,
      int initialBufferSize,
      boolean parseHttpAfterConnectRequest
   ) {
      this(maxInitialLineLength, maxHeaderSize, maxChunkSize, failOnMissingResponse, validateHeaders, initialBufferSize, parseHttpAfterConnectRequest, false);
   }

   public HttpClientCodec(
      int maxInitialLineLength,
      int maxHeaderSize,
      int maxChunkSize,
      boolean failOnMissingResponse,
      boolean validateHeaders,
      int initialBufferSize,
      boolean parseHttpAfterConnectRequest,
      boolean allowDuplicateContentLengths
   ) {
      this(
         maxInitialLineLength,
         maxHeaderSize,
         maxChunkSize,
         failOnMissingResponse,
         validateHeaders,
         initialBufferSize,
         parseHttpAfterConnectRequest,
         allowDuplicateContentLengths,
         true
      );
   }

   public HttpClientCodec(
      int maxInitialLineLength,
      int maxHeaderSize,
      int maxChunkSize,
      boolean failOnMissingResponse,
      boolean validateHeaders,
      int initialBufferSize,
      boolean parseHttpAfterConnectRequest,
      boolean allowDuplicateContentLengths,
      boolean allowPartialChunks
   ) {
      this.init(
         new HttpClientCodec.Decoder(
            maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize, allowDuplicateContentLengths, allowPartialChunks
         ),
         new HttpClientCodec.Encoder()
      );
      this.parseHttpAfterConnectRequest = parseHttpAfterConnectRequest;
      this.failOnMissingResponse = failOnMissingResponse;
   }

   @Override
   public void prepareUpgradeFrom(ChannelHandlerContext ctx) {
      ((HttpClientCodec.Encoder)this.outboundHandler()).upgraded = true;
   }

   @Override
   public void upgradeFrom(ChannelHandlerContext ctx) {
      ChannelPipeline p = ctx.pipeline();
      p.remove(this);
   }

   public void setSingleDecode(boolean singleDecode) {
      this.inboundHandler().setSingleDecode(singleDecode);
   }

   public boolean isSingleDecode() {
      return this.inboundHandler().isSingleDecode();
   }

   private final class Decoder extends HttpResponseDecoder {
      Decoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
         super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders);
      }

      Decoder(
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
         if (HttpClientCodec.this.done) {
            int readable = this.actualReadableBytes();
            if (readable == 0) {
               return;
            }

            out.add(buffer.readBytes(readable));
         } else {
            int oldSize = out.size();
            super.decode(ctx, buffer, out);
            if (HttpClientCodec.this.failOnMissingResponse) {
               int size = out.size();

               for(int i = oldSize; i < size; ++i) {
                  this.decrement(out.get(i));
               }
            }
         }

      }

      private void decrement(Object msg) {
         if (msg != null) {
            if (msg instanceof LastHttpContent) {
               HttpClientCodec.this.requestResponseCounter.decrementAndGet();
            }

         }
      }

      @Override
      protected boolean isContentAlwaysEmpty(HttpMessage msg) {
         HttpMethod method = (HttpMethod)HttpClientCodec.this.queue.poll();
         int statusCode = ((HttpResponse)msg).status().code();
         if (statusCode >= 100 && statusCode < 200) {
            return super.isContentAlwaysEmpty(msg);
         } else {
            if (method != null) {
               char firstChar = method.name().charAt(0);
               switch(firstChar) {
                  case 'C':
                     if (statusCode == 200 && HttpMethod.CONNECT.equals(method)) {
                        if (!HttpClientCodec.this.parseHttpAfterConnectRequest) {
                           HttpClientCodec.this.done = true;
                           HttpClientCodec.this.queue.clear();
                        }

                        return true;
                     }
                     break;
                  case 'H':
                     if (HttpMethod.HEAD.equals(method)) {
                        return true;
                     }
               }
            }

            return super.isContentAlwaysEmpty(msg);
         }
      }

      @Override
      public void channelInactive(ChannelHandlerContext ctx) throws Exception {
         super.channelInactive(ctx);
         if (HttpClientCodec.this.failOnMissingResponse) {
            long missingResponses = HttpClientCodec.this.requestResponseCounter.get();
            if (missingResponses > 0L) {
               ctx.fireExceptionCaught(new PrematureChannelClosureException("channel gone inactive with " + missingResponses + " missing response(s)"));
            }
         }

      }
   }

   private final class Encoder extends HttpRequestEncoder {
      boolean upgraded;

      private Encoder() {
      }

      @Override
      protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
         if (this.upgraded) {
            out.add(ReferenceCountUtil.retain(msg));
         } else {
            if (msg instanceof HttpRequest) {
               HttpClientCodec.this.queue.offer(((HttpRequest)msg).method());
            }

            super.encode(ctx, msg, out);
            if (HttpClientCodec.this.failOnMissingResponse && !HttpClientCodec.this.done && msg instanceof LastHttpContent) {
               HttpClientCodec.this.requestResponseCounter.incrementAndGet();
            }

         }
      }
   }
}
