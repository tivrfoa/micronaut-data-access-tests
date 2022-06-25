package io.netty.handler.codec.spdy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import java.util.zip.Deflater;

class SpdyHeaderBlockZlibEncoder extends SpdyHeaderBlockRawEncoder {
   private final Deflater compressor;
   private boolean finished;

   SpdyHeaderBlockZlibEncoder(SpdyVersion spdyVersion, int compressionLevel) {
      super(spdyVersion);
      if (compressionLevel >= 0 && compressionLevel <= 9) {
         this.compressor = new Deflater(compressionLevel);
         this.compressor.setDictionary(SpdyCodecUtil.SPDY_DICT);
      } else {
         throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
      }
   }

   private int setInput(ByteBuf decompressed) {
      int len = decompressed.readableBytes();
      if (decompressed.hasArray()) {
         this.compressor.setInput(decompressed.array(), decompressed.arrayOffset() + decompressed.readerIndex(), len);
      } else {
         byte[] in = new byte[len];
         decompressed.getBytes(decompressed.readerIndex(), in);
         this.compressor.setInput(in, 0, in.length);
      }

      return len;
   }

   private ByteBuf encode(ByteBufAllocator alloc, int len) {
      ByteBuf compressed = alloc.heapBuffer(len);
      boolean release = true;

      ByteBuf var5;
      try {
         while(this.compressInto(compressed)) {
            compressed.ensureWritable(compressed.capacity() << 1);
         }

         release = false;
         var5 = compressed;
      } finally {
         if (release) {
            compressed.release();
         }

      }

      return var5;
   }

   @SuppressJava6Requirement(
      reason = "Guarded by java version check"
   )
   private boolean compressInto(ByteBuf compressed) {
      byte[] out = compressed.array();
      int off = compressed.arrayOffset() + compressed.writerIndex();
      int toWrite = compressed.writableBytes();
      int numBytes;
      if (PlatformDependent.javaVersion() >= 7) {
         numBytes = this.compressor.deflate(out, off, toWrite, 2);
      } else {
         numBytes = this.compressor.deflate(out, off, toWrite);
      }

      compressed.writerIndex(compressed.writerIndex() + numBytes);
      return numBytes == toWrite;
   }

   @Override
   public ByteBuf encode(ByteBufAllocator alloc, SpdyHeadersFrame frame) throws Exception {
      ObjectUtil.checkNotNullWithIAE(alloc, "alloc");
      ObjectUtil.checkNotNullWithIAE(frame, "frame");
      if (this.finished) {
         return Unpooled.EMPTY_BUFFER;
      } else {
         ByteBuf decompressed = super.encode(alloc, frame);

         ByteBuf len;
         try {
            if (decompressed.isReadable()) {
               int len = this.setInput(decompressed);
               return this.encode(alloc, len);
            }

            len = Unpooled.EMPTY_BUFFER;
         } finally {
            decompressed.release();
         }

         return len;
      }
   }

   @Override
   public void end() {
      if (!this.finished) {
         this.finished = true;
         this.compressor.end();
         super.end();
      }
   }
}
