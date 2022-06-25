package io.micronaut.buffer.netty;

import io.micronaut.core.annotation.Nullable;

public interface ByteBufAllocatorConfiguration {
   String DEFAULT_ALLOCATOR = "netty.default.allocator";

   void setNumHeapArenas(@Nullable Integer numHeapArenas);

   void setNumDirectArenas(@Nullable Integer numDirectArenas);

   void setPageSize(@Nullable Integer pageSize);

   void setMaxOrder(@Nullable Integer maxOrder);

   void setChunkSize(@Nullable Integer chunkSize);

   void setSmallCacheSize(@Nullable Integer smallCacheSize);

   void setNormalCacheSize(@Nullable Integer normalCacheSize);

   void setUseCacheForAllThreads(@Nullable Boolean useCacheForAllThreads);

   void setMaxCachedBufferCapacity(@Nullable Integer maxCachedBufferCapacity);

   void setCacheTrimInterval(@Nullable Integer cacheTrimInterval);

   void setMaxCachedByteBuffersPerChunk(@Nullable Integer maxCachedByteBuffersPerChunk);
}
