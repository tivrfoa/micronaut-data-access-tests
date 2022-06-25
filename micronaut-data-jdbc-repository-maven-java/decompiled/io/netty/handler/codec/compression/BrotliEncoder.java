package io.netty.handler.codec.compression;

import com.aayushatharva.brotli4j.encoder.Encoders;
import com.aayushatharva.brotli4j.encoder.Encoder.Parameters;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;

@ChannelHandler.Sharable
public final class BrotliEncoder extends MessageToByteEncoder<ByteBuf> {
   private final Parameters parameters;

   public BrotliEncoder() {
      this(BrotliOptions.DEFAULT);
   }

   public BrotliEncoder(Parameters parameters) {
      this.parameters = ObjectUtil.checkNotNull(parameters, "Parameters");
   }

   public BrotliEncoder(BrotliOptions brotliOptions) {
      this(brotliOptions.parameters());
   }

   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
   }

   protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception {
      if (!msg.isReadable()) {
         return Unpooled.EMPTY_BUFFER;
      } else {
         try {
            ByteBuf out;
            if (preferDirect) {
               out = ctx.alloc().ioBuffer();
            } else {
               out = ctx.alloc().buffer();
            }

            Encoders.compress(msg, out, this.parameters);
            return out;
         } catch (Exception var5) {
            ReferenceCountUtil.release(msg);
            throw var5;
         }
      }
   }
}
