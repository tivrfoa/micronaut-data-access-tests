package io.micronaut.buffer.netty;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.Order;

@ConfigurationProperties("netty.default.allocator")
@Requires(
   property = "netty.default.allocator"
)
@Context
@BootstrapContextCompatible
@Internal
@Order(Integer.MIN_VALUE)
final class DefaultByteBufAllocatorConfiguration implements ByteBufAllocatorConfiguration {
   private static final String PROP_PREFIX = "io.netty.allocator.";

   @Override
   public void setNumHeapArenas(@Nullable Integer numHeapArenas) {
      if (numHeapArenas != null) {
         System.setProperty("io.netty.allocator.numHeapArenas", numHeapArenas.toString());
      }

   }

   @Override
   public void setNumDirectArenas(@Nullable Integer numDirectArenas) {
      if (numDirectArenas != null) {
         System.setProperty("io.netty.allocator.numDirectArenas", numDirectArenas.toString());
      }

   }

   @Override
   public void setPageSize(@Nullable Integer pageSize) {
      if (pageSize != null) {
         System.setProperty("io.netty.allocator.pageSize", pageSize.toString());
      }

   }

   @Override
   public void setMaxOrder(@Nullable Integer maxOrder) {
      if (maxOrder != null) {
         System.setProperty("io.netty.allocator.maxOrder", maxOrder.toString());
      }

   }

   @Override
   public void setChunkSize(@Nullable Integer chunkSize) {
      if (chunkSize != null) {
         System.setProperty("io.netty.allocator.chunkSize", chunkSize.toString());
      }

   }

   @Override
   public void setSmallCacheSize(@Nullable Integer smallCacheSize) {
      if (smallCacheSize != null) {
         System.setProperty("io.netty.allocator.smallCacheSize", smallCacheSize.toString());
      }

   }

   @Override
   public void setNormalCacheSize(@Nullable Integer normalCacheSize) {
      if (normalCacheSize != null) {
         System.setProperty("io.netty.allocator.normalCacheSize", normalCacheSize.toString());
      }

   }

   @Override
   public void setUseCacheForAllThreads(@Nullable Boolean useCacheForAllThreads) {
      if (useCacheForAllThreads != null) {
         System.setProperty("io.netty.allocator.useCacheForAllThreads", useCacheForAllThreads.toString());
      }

   }

   @Override
   public void setMaxCachedBufferCapacity(@Nullable Integer maxCachedBufferCapacity) {
      if (maxCachedBufferCapacity != null) {
         System.setProperty("io.netty.allocator.maxCachedBufferCapacity", maxCachedBufferCapacity.toString());
      }

   }

   @Override
   public void setCacheTrimInterval(@Nullable Integer cacheTrimInterval) {
      if (cacheTrimInterval != null) {
         System.setProperty("io.netty.allocator.cacheTrimInterval", cacheTrimInterval.toString());
      }

   }

   @Override
   public void setMaxCachedByteBuffersPerChunk(@Nullable Integer maxCachedByteBuffersPerChunk) {
      if (maxCachedByteBuffersPerChunk != null) {
         System.setProperty("io.netty.allocator.maxCachedByteBuffersPerChunk", maxCachedByteBuffersPerChunk.toString());
      }

   }
}
