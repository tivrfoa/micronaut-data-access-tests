package io.netty.handler.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.PromiseCombiner;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.TypeParameterMatcher;
import java.util.List;

public abstract class MessageToMessageEncoder<I> extends ChannelOutboundHandlerAdapter {
   private final TypeParameterMatcher matcher;

   protected MessageToMessageEncoder() {
      this.matcher = TypeParameterMatcher.find(this, MessageToMessageEncoder.class, "I");
   }

   protected MessageToMessageEncoder(Class<? extends I> outboundMessageType) {
      this.matcher = TypeParameterMatcher.get(outboundMessageType);
   }

   public boolean acceptOutboundMessage(Object msg) throws Exception {
      return this.matcher.match(msg);
   }

   // $FF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      CodecOutputList out = null;
      boolean var29 = false;

      try {
         var29 = true;
         if (this.acceptOutboundMessage(msg)) {
            out = CodecOutputList.newInstance();
            I cast = (I)msg;

            try {
               this.encode(ctx, cast, out);
            } finally {
               ReferenceCountUtil.release(msg);
            }

            if (out.isEmpty()) {
               throw new EncoderException(StringUtil.simpleClassName(this) + " must produce at least one message.");
            }

            var29 = false;
         } else {
            ctx.write(msg, promise);
            var29 = false;
         }
      } catch (EncoderException var39) {
         throw var39;
      } catch (Throwable var40) {
         throw new EncoderException(var40);
      } finally {
         if (var29) {
            if (out != null) {
               try {
                  int sizeMinusOne = out.size() - 1;
                  if (sizeMinusOne == 0) {
                     ctx.write(out.getUnsafe(0), promise);
                  } else if (sizeMinusOne > 0) {
                     if (promise == ctx.voidPromise()) {
                        writeVoidPromise(ctx, out);
                     } else {
                        writePromiseCombiner(ctx, out, promise);
                     }
                  }
               } finally {
                  out.recycle();
               }
            }

         }
      }

      if (out != null) {
         try {
            int sizeMinusOne = out.size() - 1;
            if (sizeMinusOne == 0) {
               ctx.write(out.getUnsafe(0), promise);
            } else if (sizeMinusOne > 0) {
               if (promise == ctx.voidPromise()) {
                  writeVoidPromise(ctx, out);
               } else {
                  writePromiseCombiner(ctx, out, promise);
               }
            }
         } finally {
            out.recycle();
         }
      }

   }

   private static void writeVoidPromise(ChannelHandlerContext ctx, CodecOutputList out) {
      ChannelPromise voidPromise = ctx.voidPromise();

      for(int i = 0; i < out.size(); ++i) {
         ctx.write(out.getUnsafe(i), voidPromise);
      }

   }

   private static void writePromiseCombiner(ChannelHandlerContext ctx, CodecOutputList out, ChannelPromise promise) {
      PromiseCombiner combiner = new PromiseCombiner(ctx.executor());

      for(int i = 0; i < out.size(); ++i) {
         combiner.add(ctx.write(out.getUnsafe(i)));
      }

      combiner.finish(promise);
   }

   protected abstract void encode(ChannelHandlerContext var1, I var2, List<Object> var3) throws Exception;
}
