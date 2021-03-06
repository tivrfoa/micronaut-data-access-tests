package io.netty.handler.codec.compression;

import io.netty.util.internal.ObjectUtil;

public class ZstdOptions implements CompressionOptions {
   private final int blockSize;
   private final int compressionLevel;
   private final int maxEncodeSize;
   static final ZstdOptions DEFAULT = new ZstdOptions(3, 65536, 33554432);

   ZstdOptions(int compressionLevel, int blockSize, int maxEncodeSize) {
      if (!Zstd.isAvailable()) {
         throw new IllegalStateException("zstd-jni is not available", Zstd.cause());
      } else {
         this.compressionLevel = ObjectUtil.checkInRange(compressionLevel, 0, 22, "compressionLevel");
         this.blockSize = ObjectUtil.checkPositive(blockSize, "blockSize");
         this.maxEncodeSize = ObjectUtil.checkPositive(maxEncodeSize, "maxEncodeSize");
      }
   }

   public int compressionLevel() {
      return this.compressionLevel;
   }

   public int blockSize() {
      return this.blockSize;
   }

   public int maxEncodeSize() {
      return this.maxEncodeSize;
   }
}
