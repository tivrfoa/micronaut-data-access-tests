package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.util.internal.ObjectUtil;
import java.util.List;

public abstract class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {
   public static final ByteToMessageDecoder.Cumulator MERGE_CUMULATOR = new ByteToMessageDecoder.Cumulator() {
      @Override
      public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
         if (!cumulation.isReadable() && in.isContiguous()) {
            cumulation.release();
            return in;
         } else {
            ByteBuf var5;
            try {
               int required = in.readableBytes();
               if (required <= cumulation.maxWritableBytes()
                  && (required <= cumulation.maxFastWritableBytes() || cumulation.refCnt() <= 1)
                  && !cumulation.isReadOnly()) {
                  cumulation.writeBytes(in, in.readerIndex(), required);
                  in.readerIndex(in.writerIndex());
                  return cumulation;
               }

               var5 = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in);
            } finally {
               in.release();
            }

            return var5;
         }
      }
   };
   public static final ByteToMessageDecoder.Cumulator COMPOSITE_CUMULATOR = new ByteToMessageDecoder.Cumulator() {
      @Override
      public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
         if (!cumulation.isReadable()) {
            cumulation.release();
            return in;
         } else {
            CompositeByteBuf composite = null;

            CompositeByteBuf var5;
            try {
               if (cumulation instanceof CompositeByteBuf && cumulation.refCnt() == 1) {
                  composite = (CompositeByteBuf)cumulation;
                  if (composite.writerIndex() != composite.capacity()) {
                     composite.capacity(composite.writerIndex());
                  }
               } else {
                  composite = alloc.compositeBuffer(Integer.MAX_VALUE).addFlattenedComponents(true, cumulation);
               }

               composite.addFlattenedComponents(true, in);
               in = null;
               var5 = composite;
            } finally {
               if (in != null) {
                  in.release();
                  if (composite != null && composite != cumulation) {
                     composite.release();
                  }
               }

            }

            return var5;
         }
      }
   };
   private static final byte STATE_INIT = 0;
   private static final byte STATE_CALLING_CHILD_DECODE = 1;
   private static final byte STATE_HANDLER_REMOVED_PENDING = 2;
   ByteBuf cumulation;
   private ByteToMessageDecoder.Cumulator cumulator = MERGE_CUMULATOR;
   private boolean singleDecode;
   private boolean first;
   private boolean firedChannelRead;
   private boolean selfFiredChannelRead;
   private byte decodeState = 0;
   private int discardAfterReads = 16;
   private int numReads;

   protected ByteToMessageDecoder() {
      this.ensureNotSharable();
   }

   public void setSingleDecode(boolean singleDecode) {
      this.singleDecode = singleDecode;
   }

   public boolean isSingleDecode() {
      return this.singleDecode;
   }

   public void setCumulator(ByteToMessageDecoder.Cumulator cumulator) {
      this.cumulator = ObjectUtil.checkNotNull(cumulator, "cumulator");
   }

   public void setDiscardAfterReads(int discardAfterReads) {
      ObjectUtil.checkPositive(discardAfterReads, "discardAfterReads");
      this.discardAfterReads = discardAfterReads;
   }

   protected int actualReadableBytes() {
      return this.internalBuffer().readableBytes();
   }

   protected ByteBuf internalBuffer() {
      return this.cumulation != null ? this.cumulation : Unpooled.EMPTY_BUFFER;
   }

   @Override
   public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      if (this.decodeState == 1) {
         this.decodeState = 2;
      } else {
         ByteBuf buf = this.cumulation;
         if (buf != null) {
            this.cumulation = null;
            this.numReads = 0;
            int readable = buf.readableBytes();
            if (readable > 0) {
               ctx.fireChannelRead(buf);
               ctx.fireChannelReadComplete();
            } else {
               buf.release();
            }
         }

         this.handlerRemoved0(ctx);
      }
   }

   protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
   }

   // $FF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (msg instanceof ByteBuf) {
         this.selfFiredChannelRead = true;
         CodecOutputList out = CodecOutputList.newInstance();
         boolean var24 = false;

         try {
            var24 = true;
            this.first = this.cumulation == null;
            this.cumulation = this.cumulator.cumulate(ctx.alloc(), this.first ? Unpooled.EMPTY_BUFFER : this.cumulation, (ByteBuf)msg);
            this.callDecode(ctx, this.cumulation, out);
            var24 = false;
         } catch (DecoderException var25) {
            throw var25;
         } catch (Exception var26) {
            throw new DecoderException(var26);
         } finally {
            if (var24) {
               try {
                  if (this.cumulation != null && !this.cumulation.isReadable()) {
                     this.numReads = 0;
                     this.cumulation.release();
                     this.cumulation = null;
                  } else if (++this.numReads >= this.discardAfterReads) {
                     this.numReads = 0;
                     this.discardSomeReadBytes();
                  }

                  int size = out.size();
                  this.firedChannelRead |= out.insertSinceRecycled();
                  fireChannelRead(ctx, out, size);
               } finally {
                  out.recycle();
               }
            }
         }

         try {
            if (this.cumulation != null && !this.cumulation.isReadable()) {
               this.numReads = 0;
               this.cumulation.release();
               this.cumulation = null;
            } else if (++this.numReads >= this.discardAfterReads) {
               this.numReads = 0;
               this.discardSomeReadBytes();
            }

            int size = out.size();
            this.firedChannelRead |= out.insertSinceRecycled();
            fireChannelRead(ctx, out, size);
         } finally {
            out.recycle();
         }
      } else {
         ctx.fireChannelRead(msg);
      }

   }

   static void fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements) {
      if (msgs instanceof CodecOutputList) {
         fireChannelRead(ctx, (CodecOutputList)msgs, numElements);
      } else {
         for(int i = 0; i < numElements; ++i) {
            ctx.fireChannelRead(msgs.get(i));
         }
      }

   }

   static void fireChannelRead(ChannelHandlerContext ctx, CodecOutputList msgs, int numElements) {
      for(int i = 0; i < numElements; ++i) {
         ctx.fireChannelRead(msgs.getUnsafe(i));
      }

   }

   @Override
   public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      this.numReads = 0;
      this.discardSomeReadBytes();
      if (this.selfFiredChannelRead && !this.firedChannelRead && !ctx.channel().config().isAutoRead()) {
         ctx.read();
      }

      this.firedChannelRead = false;
      ctx.fireChannelReadComplete();
   }

   protected final void discardSomeReadBytes() {
      if (this.cumulation != null && !this.first && this.cumulation.refCnt() == 1) {
         this.cumulation.discardSomeReadBytes();
      }

   }

   @Override
   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      this.channelInputClosed(ctx, true);
   }

   @Override
   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof ChannelInputShutdownEvent) {
         this.channelInputClosed(ctx, false);
      }

      super.userEventTriggered(ctx, evt);
   }

   // $FF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void channelInputClosed(ChannelHandlerContext ctx, boolean callChannelInactive) {
      CodecOutputList out = CodecOutputList.newInstance();
      boolean var24 = false;

      try {
         var24 = true;
         this.channelInputClosed(ctx, out);
         var24 = false;
      } catch (DecoderException var25) {
         throw var25;
      } catch (Exception var26) {
         throw new DecoderException(var26);
      } finally {
         if (var24) {
            try {
               if (this.cumulation != null) {
                  this.cumulation.release();
                  this.cumulation = null;
               }

               int size = out.size();
               fireChannelRead(ctx, out, size);
               if (size > 0) {
                  ctx.fireChannelReadComplete();
               }

               if (callChannelInactive) {
                  ctx.fireChannelInactive();
               }
            } finally {
               out.recycle();
            }

         }
      }

      try {
         if (this.cumulation != null) {
            this.cumulation.release();
            this.cumulation = null;
         }

         int size = out.size();
         fireChannelRead(ctx, out, size);
         if (size > 0) {
            ctx.fireChannelReadComplete();
         }

         if (callChannelInactive) {
            ctx.fireChannelInactive();
         }
      } finally {
         out.recycle();
      }

   }

   void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception {
      if (this.cumulation != null) {
         this.callDecode(ctx, this.cumulation, out);
         if (!ctx.isRemoved()) {
            ByteBuf buffer = this.cumulation == null ? Unpooled.EMPTY_BUFFER : this.cumulation;
            this.decodeLast(ctx, buffer, out);
         }
      } else {
         this.decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
      }

   }

   protected void callDecode(ChannelHandlerContext param1, ByteBuf param2, List<Object> param3) {
      // $FF: Couldn't be decompiled
      // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.util.ConcurrentModificationException
      //   at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1013)
      //   at java.base/java.util.ArrayList$Itr.next(ArrayList.java:967)
      //   at org.jetbrains.java.decompiler.modules.decompiler.MergeHelper.condenseInfiniteLoopsWithReturnRec(MergeHelper.java:942)
      //   at org.jetbrains.java.decompiler.modules.decompiler.MergeHelper.condenseInfiniteLoopsWithReturnRec(MergeHelper.java:943)
      //   at org.jetbrains.java.decompiler.modules.decompiler.MergeHelper.condenseInfiniteLoopsWithReturn(MergeHelper.java:922)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessorRunnable.codeToJava(MethodProcessorRunnable.java:293)
      //
      // Bytecode:
      // 00: aload 2
      // 01: invokevirtual io/netty/buffer/ByteBuf.isReadable ()Z
      // 04: ifeq 92
      // 07: aload 3
      // 08: invokeinterface java/util/List.size ()I 1
      // 0d: istore 4
      // 0f: iload 4
      // 11: ifle 2d
      // 14: aload 1
      // 15: aload 3
      // 16: iload 4
      // 18: invokestatic io/netty/handler/codec/ByteToMessageDecoder.fireChannelRead (Lio/netty/channel/ChannelHandlerContext;Ljava/util/List;I)V
      // 1b: aload 3
      // 1c: invokeinterface java/util/List.clear ()V 1
      // 21: aload 1
      // 22: invokeinterface io/netty/channel/ChannelHandlerContext.isRemoved ()Z 1
      // 27: ifeq 2d
      // 2a: goto 92
      // 2d: aload 2
      // 2e: invokevirtual io/netty/buffer/ByteBuf.readableBytes ()I
      // 31: istore 5
      // 33: aload 0
      // 34: aload 1
      // 35: aload 2
      // 36: aload 3
      // 37: invokevirtual io/netty/handler/codec/ByteToMessageDecoder.decodeRemovalReentryProtection (Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V
      // 3a: aload 1
      // 3b: invokeinterface io/netty/channel/ChannelHandlerContext.isRemoved ()Z 1
      // 40: ifeq 46
      // 43: goto 92
      // 46: aload 3
      // 47: invokeinterface java/util/List.isEmpty ()Z 1
      // 4c: ifeq 5b
      // 4f: iload 5
      // 51: aload 2
      // 52: invokevirtual io/netty/buffer/ByteBuf.readableBytes ()I
      // 55: if_icmpne 00
      // 58: goto 92
      // 5b: iload 5
      // 5d: aload 2
      // 5e: invokevirtual io/netty/buffer/ByteBuf.readableBytes ()I
      // 61: if_icmpne 85
      // 64: new io/netty/handler/codec/DecoderException
      // 67: dup
      // 68: new java/lang/StringBuilder
      // 6b: dup
      // 6c: invokespecial java/lang/StringBuilder.<init> ()V
      // 6f: aload 0
      // 70: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 73: invokestatic io/netty/util/internal/StringUtil.simpleClassName (Ljava/lang/Class;)Ljava/lang/String;
      // 76: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 79: ldc ".decode() did not read anything but decoded a message."
      // 7b: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 7e: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 81: invokespecial io/netty/handler/codec/DecoderException.<init> (Ljava/lang/String;)V
      // 84: athrow
      // 85: aload 0
      // 86: invokevirtual io/netty/handler/codec/ByteToMessageDecoder.isSingleDecode ()Z
      // 89: ifeq 8f
      // 8c: goto 92
      // 8f: goto 00
      // 92: goto a6
      // 95: astore 4
      // 97: aload 4
      // 99: athrow
      // 9a: astore 4
      // 9c: new io/netty/handler/codec/DecoderException
      // 9f: dup
      // a0: aload 4
      // a2: invokespecial io/netty/handler/codec/DecoderException.<init> (Ljava/lang/Throwable;)V
      // a5: athrow
      // a6: return
   }

   protected abstract void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws Exception;

   // $FF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
   final void decodeRemovalReentryProtection(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      this.decodeState = 1;
      boolean var8 = false;

      try {
         var8 = true;
         this.decode(ctx, in, out);
         var8 = false;
      } finally {
         if (var8) {
            boolean removePending = this.decodeState == 2;
            this.decodeState = 0;
            if (removePending) {
               fireChannelRead(ctx, out, out.size());
               out.clear();
               this.handlerRemoved(ctx);
            }

         }
      }

      boolean removePending = this.decodeState == 2;
      this.decodeState = 0;
      if (removePending) {
         fireChannelRead(ctx, out, out.size());
         out.clear();
         this.handlerRemoved(ctx);
      }

   }

   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      if (in.isReadable()) {
         this.decodeRemovalReentryProtection(ctx, in, out);
      }

   }

   static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf oldCumulation, ByteBuf in) {
      int oldBytes = oldCumulation.readableBytes();
      int newBytes = in.readableBytes();
      int totalBytes = oldBytes + newBytes;
      ByteBuf newCumulation = alloc.buffer(alloc.calculateNewCapacity(totalBytes, Integer.MAX_VALUE));
      ByteBuf toRelease = newCumulation;

      ByteBuf var8;
      try {
         newCumulation.setBytes(0, oldCumulation, oldCumulation.readerIndex(), oldBytes)
            .setBytes(oldBytes, in, in.readerIndex(), newBytes)
            .writerIndex(totalBytes);
         in.readerIndex(in.writerIndex());
         toRelease = oldCumulation;
         var8 = newCumulation;
      } finally {
         toRelease.release();
      }

      return var8;
   }

   public interface Cumulator {
      ByteBuf cumulate(ByteBufAllocator var1, ByteBuf var2, ByteBuf var3);
   }
}
