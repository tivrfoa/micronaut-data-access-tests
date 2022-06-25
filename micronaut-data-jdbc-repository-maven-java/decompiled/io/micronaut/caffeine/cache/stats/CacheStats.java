package io.micronaut.caffeine.cache.stats;

import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

@Immutable
public final class CacheStats {
   private static final CacheStats EMPTY_STATS = of(0L, 0L, 0L, 0L, 0L, 0L, 0L);
   private final long hitCount;
   private final long missCount;
   private final long loadSuccessCount;
   private final long loadFailureCount;
   private final long totalLoadTime;
   private final long evictionCount;
   private final long evictionWeight;

   @Deprecated
   public CacheStats(
      @NonNegative long hitCount,
      @NonNegative long missCount,
      @NonNegative long loadSuccessCount,
      @NonNegative long loadFailureCount,
      @NonNegative long totalLoadTime,
      @NonNegative long evictionCount
   ) {
      this(hitCount, missCount, loadSuccessCount, loadFailureCount, totalLoadTime, evictionCount, 0L);
   }

   @Deprecated
   public CacheStats(
      @NonNegative long hitCount,
      @NonNegative long missCount,
      @NonNegative long loadSuccessCount,
      @NonNegative long loadFailureCount,
      @NonNegative long totalLoadTime,
      @NonNegative long evictionCount,
      @NonNegative long evictionWeight
   ) {
      if (hitCount >= 0L
         && missCount >= 0L
         && loadSuccessCount >= 0L
         && loadFailureCount >= 0L
         && totalLoadTime >= 0L
         && evictionCount >= 0L
         && evictionWeight >= 0L) {
         this.hitCount = hitCount;
         this.missCount = missCount;
         this.loadSuccessCount = loadSuccessCount;
         this.loadFailureCount = loadFailureCount;
         this.totalLoadTime = totalLoadTime;
         this.evictionCount = evictionCount;
         this.evictionWeight = evictionWeight;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public static CacheStats of(
      @NonNegative long hitCount,
      @NonNegative long missCount,
      @NonNegative long loadSuccessCount,
      @NonNegative long loadFailureCount,
      @NonNegative long totalLoadTime,
      @NonNegative long evictionCount,
      @NonNegative long evictionWeight
   ) {
      return new CacheStats(hitCount, missCount, loadSuccessCount, loadFailureCount, totalLoadTime, evictionCount, evictionWeight);
   }

   @NonNull
   public static CacheStats empty() {
      return EMPTY_STATS;
   }

   @NonNegative
   public long requestCount() {
      return saturatedAdd(this.hitCount, this.missCount);
   }

   @NonNegative
   public long hitCount() {
      return this.hitCount;
   }

   @NonNegative
   public double hitRate() {
      long requestCount = this.requestCount();
      return requestCount == 0L ? 1.0 : (double)this.hitCount / (double)requestCount;
   }

   @NonNegative
   public long missCount() {
      return this.missCount;
   }

   @NonNegative
   public double missRate() {
      long requestCount = this.requestCount();
      return requestCount == 0L ? 0.0 : (double)this.missCount / (double)requestCount;
   }

   @NonNegative
   public long loadCount() {
      return saturatedAdd(this.loadSuccessCount, this.loadFailureCount);
   }

   @NonNegative
   public long loadSuccessCount() {
      return this.loadSuccessCount;
   }

   @NonNegative
   public long loadFailureCount() {
      return this.loadFailureCount;
   }

   @NonNegative
   public double loadFailureRate() {
      long totalLoadCount = saturatedAdd(this.loadSuccessCount, this.loadFailureCount);
      return totalLoadCount == 0L ? 0.0 : (double)this.loadFailureCount / (double)totalLoadCount;
   }

   @NonNegative
   public long totalLoadTime() {
      return this.totalLoadTime;
   }

   @NonNegative
   public double averageLoadPenalty() {
      long totalLoadCount = saturatedAdd(this.loadSuccessCount, this.loadFailureCount);
      return totalLoadCount == 0L ? 0.0 : (double)this.totalLoadTime / (double)totalLoadCount;
   }

   @NonNegative
   public long evictionCount() {
      return this.evictionCount;
   }

   @NonNegative
   public long evictionWeight() {
      return this.evictionWeight;
   }

   @NonNull
   public CacheStats minus(@NonNull CacheStats other) {
      return of(
         Math.max(0L, saturatedSubtract(this.hitCount, other.hitCount)),
         Math.max(0L, saturatedSubtract(this.missCount, other.missCount)),
         Math.max(0L, saturatedSubtract(this.loadSuccessCount, other.loadSuccessCount)),
         Math.max(0L, saturatedSubtract(this.loadFailureCount, other.loadFailureCount)),
         Math.max(0L, saturatedSubtract(this.totalLoadTime, other.totalLoadTime)),
         Math.max(0L, saturatedSubtract(this.evictionCount, other.evictionCount)),
         Math.max(0L, saturatedSubtract(this.evictionWeight, other.evictionWeight))
      );
   }

   @NonNull
   public CacheStats plus(@NonNull CacheStats other) {
      return of(
         saturatedAdd(this.hitCount, other.hitCount),
         saturatedAdd(this.missCount, other.missCount),
         saturatedAdd(this.loadSuccessCount, other.loadSuccessCount),
         saturatedAdd(this.loadFailureCount, other.loadFailureCount),
         saturatedAdd(this.totalLoadTime, other.totalLoadTime),
         saturatedAdd(this.evictionCount, other.evictionCount),
         saturatedAdd(this.evictionWeight, other.evictionWeight)
      );
   }

   private static long saturatedSubtract(long a, long b) {
      long naiveDifference = a - b;
      return (a ^ b) >= 0L | (a ^ naiveDifference) >= 0L ? naiveDifference : Long.MAX_VALUE + (naiveDifference >>> 63 ^ 1L);
   }

   private static long saturatedAdd(long a, long b) {
      long naiveSum = a + b;
      return (a ^ b) < 0L | (a ^ naiveSum) >= 0L ? naiveSum : Long.MAX_VALUE + (naiveSum >>> 63 ^ 1L);
   }

   public int hashCode() {
      return Objects.hash(
         new Object[]{this.hitCount, this.missCount, this.loadSuccessCount, this.loadFailureCount, this.totalLoadTime, this.evictionCount, this.evictionWeight}
      );
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof CacheStats)) {
         return false;
      } else {
         CacheStats other = (CacheStats)o;
         return this.hitCount == other.hitCount
            && this.missCount == other.missCount
            && this.loadSuccessCount == other.loadSuccessCount
            && this.loadFailureCount == other.loadFailureCount
            && this.totalLoadTime == other.totalLoadTime
            && this.evictionCount == other.evictionCount
            && this.evictionWeight == other.evictionWeight;
      }
   }

   public String toString() {
      return this.getClass().getSimpleName()
         + '{'
         + "hitCount="
         + this.hitCount
         + ", missCount="
         + this.missCount
         + ", loadSuccessCount="
         + this.loadSuccessCount
         + ", loadFailureCount="
         + this.loadFailureCount
         + ", totalLoadTime="
         + this.totalLoadTime
         + ", evictionCount="
         + this.evictionCount
         + ", evictionWeight="
         + this.evictionWeight
         + '}';
   }
}
