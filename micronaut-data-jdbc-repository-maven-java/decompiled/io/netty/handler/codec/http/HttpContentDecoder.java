package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import java.util.List;

public abstract class HttpContentDecoder extends MessageToMessageDecoder<HttpObject> {
   static final String IDENTITY = HttpHeaderValues.IDENTITY.toString();
   protected ChannelHandlerContext ctx;
   private EmbeddedChannel decoder;
   private boolean continueResponse;
   private boolean needRead = true;

   protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
      try {
         if (!(msg instanceof HttpResponse) || ((HttpResponse)msg).status().code() != 100) {
            if (!this.continueResponse) {
               if (msg instanceof HttpMessage) {
                  this.cleanup();
                  HttpMessage message = (HttpMessage)msg;
                  HttpHeaders headers = message.headers();
                  String contentEncoding = headers.get(HttpHeaderNames.CONTENT_ENCODING);
                  if (contentEncoding != null) {
                     contentEncoding = contentEncoding.trim();
                  } else {
                     String transferEncoding = headers.get(HttpHeaderNames.TRANSFER_ENCODING);
                     if (transferEncoding != null) {
                        int idx = transferEncoding.indexOf(",");
                        if (idx != -1) {
                           contentEncoding = transferEncoding.substring(0, idx).trim();
                        } else {
                           contentEncoding = transferEncoding.trim();
                        }
                     } else {
                        contentEncoding = IDENTITY;
                     }
                  }

                  this.decoder = this.newContentDecoder(contentEncoding);
                  if (this.decoder == null) {
                     if (message instanceof HttpContent) {
                        ((HttpContent)message).retain();
                     }

                     out.add(message);
                     return;
                  }

                  if (headers.contains(HttpHeaderNames.CONTENT_LENGTH)) {
                     headers.remove(HttpHeaderNames.CONTENT_LENGTH);
                     headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
                  }

                  CharSequence targetContentEncoding = this.getTargetContentEncoding(contentEncoding);
                  if (HttpHeaderValues.IDENTITY.contentEquals(targetContentEncoding)) {
                     headers.remove(HttpHeaderNames.CONTENT_ENCODING);
                  } else {
                     headers.set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
                  }

                  if (message instanceof HttpContent) {
                     HttpMessage copy;
                     if (message instanceof HttpRequest) {
                        HttpRequest r = (HttpRequest)message;
                        copy = new DefaultHttpRequest(r.protocolVersion(), r.method(), r.uri());
                     } else {
                        if (!(message instanceof HttpResponse)) {
                           throw new CodecException("Object of class " + message.getClass().getName() + " is not an HttpRequest or HttpResponse");
                        }

                        HttpResponse r = (HttpResponse)message;
                        copy = new DefaultHttpResponse(r.protocolVersion(), r.status());
                     }

                     copy.headers().set(message.headers());
                     copy.setDecoderResult(message.decoderResult());
                     out.add(copy);
                  } else {
                     out.add(message);
                  }
               }

               if (msg instanceof HttpContent) {
                  HttpContent c = (HttpContent)msg;
                  if (this.decoder == null) {
                     out.add(c.retain());
                  } else {
                     this.decodeContent(c, out);
                  }

                  return;
               }

               return;
            }

            if (msg instanceof LastHttpContent) {
               this.continueResponse = false;
            }

            out.add(ReferenceCountUtil.retain(msg));
            return;
         }

         if (!(msg instanceof LastHttpContent)) {
            this.continueResponse = true;
         }

         out.add(ReferenceCountUtil.retain(msg));
      } finally {
         this.needRead = out.isEmpty();
      }

   }

   private void decodeContent(HttpContent c, List<Object> out) {
      ByteBuf content = c.content();
      this.decode(content, out);
      if (c instanceof LastHttpContent) {
         this.finishDecode(out);
         LastHttpContent last = (LastHttpContent)c;
         HttpHeaders headers = last.trailingHeaders();
         if (headers.isEmpty()) {
            out.add(LastHttpContent.EMPTY_LAST_CONTENT);
         } else {
            out.add(new ComposedLastHttpContent(headers, DecoderResult.SUCCESS));
         }
      }

   }

   @Override
   public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      boolean needRead = this.needRead;
      this.needRead = true;

      try {
         ctx.fireChannelReadComplete();
      } finally {
         if (needRead && !ctx.channel().config().isAutoRead()) {
            ctx.read();
         }

      }

   }

   protected abstract EmbeddedChannel newContentDecoder(String var1) throws Exception;

   protected String getTargetContentEncoding(String contentEncoding) throws Exception {
      return IDENTITY;
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      this.cleanupSafely(ctx);
      super.handlerRemoved(ctx);
   }

   @Override
   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      this.cleanupSafely(ctx);
      super.channelInactive(ctx);
   }

   @Override
   public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
      this.ctx = ctx;
      super.handlerAdded(ctx);
   }

   private void cleanup() {
      if (this.decoder != null) {
         this.decoder.finishAndReleaseAll();
         this.decoder = null;
      }

   }

   private void cleanupSafely(ChannelHandlerContext ctx) {
      try {
         this.cleanup();
      } catch (Throwable var3) {
         ctx.fireExceptionCaught(var3);
      }

   }

   private void decode(ByteBuf in, List<Object> out) {
      this.decoder.writeInbound(in.retain());
      this.fetchDecoderOutput(out);
   }

   private void finishDecode(List<Object> out) {
      if (this.decoder.finish()) {
         this.fetchDecoderOutput(out);
      }

      this.decoder = null;
   }

   private void fetchDecoderOutput(List<Object> out) {
      while(true) {
         ByteBuf buf = this.decoder.readInbound();
         if (buf == null) {
            return;
         }

         if (!buf.isReadable()) {
            buf.release();
         } else {
            out.add(new DefaultHttpContent(buf));
         }
      }
   }
}
