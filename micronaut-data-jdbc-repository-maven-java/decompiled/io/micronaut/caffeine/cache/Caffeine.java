package io.micronaut.caffeine.cache;

import com.google.errorprone.annotations.FormatMethod;
import io.micronaut.caffeine.cache.stats.ConcurrentStatsCounter;
import io.micronaut.caffeine.cache.stats.StatsCounter;
import java.io.Serializable;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Caffeine<K, V> {
   static final Logger logger = Logger.getLogger(Caffeine.class.getName());
   static final Supplier<StatsCounter> ENABLED_STATS_COUNTER_SUPPLIER = ConcurrentStatsCounter::new;
   static final int UNSET_INT = -1;
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final int DEFAULT_EXPIRATION_NANOS = 0;
   static final int DEFAULT_REFRESH_NANOS = 0;
   boolean strictParsing = true;
   long maximumSize = -1L;
   long maximumWeight = -1L;
   int initialCapacity = -1;
   long expireAfterWriteNanos = -1L;
   long expireAfterAccessNanos = -1L;
   long refreshAfterWriteNanos = -1L;
   @Nullable
   RemovalListener<? super K, ? super V> evictionListener;
   @Nullable
   RemovalListener<? super K, ? super V> removalListener;
   @Nullable
   Supplier<StatsCounter> statsCounterSupplier;
   @Nullable
   CacheWriter<? super K, ? super V> writer;
   @Nullable
   Weigher<? super K, ? super V> weigher;
   @Nullable
   Expiry<? super K, ? super V> expiry;
   @Nullable
   Scheduler scheduler;
   @Nullable
   Executor executor;
   @Nullable
   Ticker ticker;
   @Nullable
   Caffeine.Strength keyStrength;
   @Nullable
   Caffeine.Strength valueStrength;

   private Caffeine() {
   }

   @FormatMethod
   static void requireArgument(boolean expression, String template, Object... args) {
      if (!expression) {
         throw new IllegalArgumentException(String.format(template, args));
      }
   }

   static void requireArgument(boolean expression) {
      if (!expression) {
         throw new IllegalArgumentException();
      }
   }

   static void requireState(boolean expression) {
      if (!expression) {
         throw new IllegalStateException();
      }
   }

   @FormatMethod
   static void requireState(boolean expression, String template, Object... args) {
      if (!expression) {
         throw new IllegalStateException(String.format(template, args));
      }
   }

   static int ceilingPowerOfTwo(int x) {
      return 1 << -Integer.numberOfLeadingZeros(x - 1);
   }

   static long ceilingPowerOfTwo(long x) {
      return 1L << -Long.numberOfLeadingZeros(x - 1L);
   }

   @NonNull
   public static Caffeine<Object, Object> newBuilder() {
      return new Caffeine<>();
   }

   @NonNull
   public static Caffeine<Object, Object> from(CaffeineSpec spec) {
      Caffeine<Object, Object> builder = spec.toBuilder();
      builder.strictParsing = false;
      return builder;
   }

   @NonNull
   public static Caffeine<Object, Object> from(String spec) {
      return from(CaffeineSpec.parse(spec));
   }

   @NonNull
   public Caffeine<K, V> initialCapacity(@NonNegative int initialCapacity) {
      requireState(this.initialCapacity == -1, "initial capacity was already set to %s", this.initialCapacity);
      requireArgument(initialCapacity >= 0);
      this.initialCapacity = initialCapacity;
      return this;
   }

   boolean hasInitialCapacity() {
      return this.initialCapacity != -1;
   }

   int getInitialCapacity() {
      return this.hasInitialCapacity() ? this.initialCapacity : 16;
   }

   @NonNull
   public Caffeine<K, V> executor(@NonNull Executor executor) {
      requireState(this.executor == null, "executor was already set to %s", this.executor);
      this.executor = (Executor)Objects.requireNonNull(executor);
      return this;
   }

   @NonNull
   Executor getExecutor() {
      return (Executor)(this.executor == null ? ForkJoinPool.commonPool() : this.executor);
   }

   @NonNull
   public Caffeine<K, V> scheduler(@NonNull Scheduler scheduler) {
      requireState(this.scheduler == null, "scheduler was already set to %s", this.scheduler);
      this.scheduler = (Scheduler)Objects.requireNonNull(scheduler);
      return this;
   }

   @NonNull
   Scheduler getScheduler() {
      if (this.scheduler == null || this.scheduler == Scheduler.disabledScheduler()) {
         return Scheduler.disabledScheduler();
      } else {
         return this.scheduler == Scheduler.systemScheduler() ? this.scheduler : Scheduler.guardedScheduler(this.scheduler);
      }
   }

   @NonNull
   public Caffeine<K, V> maximumSize(@NonNegative long maximumSize) {
      requireState(this.maximumSize == -1L, "maximum size was already set to %s", this.maximumSize);
      requireState(this.maximumWeight == -1L, "maximum weight was already set to %s", this.maximumWeight);
      requireState(this.weigher == null, "maximum size can not be combined with weigher");
      requireArgument(maximumSize >= 0L, "maximum size must not be negative");
      this.maximumSize = maximumSize;
      return this;
   }

   @NonNull
   public Caffeine<K, V> maximumWeight(@NonNegative long maximumWeight) {
      requireState(this.maximumWeight == -1L, "maximum weight was already set to %s", this.maximumWeight);
      requireState(this.maximumSize == -1L, "maximum size was already set to %s", this.maximumSize);
      requireArgument(maximumWeight >= 0L, "maximum weight must not be negative");
      this.maximumWeight = maximumWeight;
      return this;
   }

   @NonNull
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> weigher(@NonNull Weigher<? super K1, ? super V1> weigher) {
      Objects.requireNonNull(weigher);
      requireState(this.weigher == null, "weigher was already set to %s", this.weigher);
      requireState(!this.strictParsing || this.maximumSize == -1L, "weigher can not be combined with maximum size");
      this.weigher = weigher;
      return this;
   }

   boolean evicts() {
      return this.getMaximum() != -1L;
   }

   boolean isWeighted() {
      return this.weigher != null;
   }

   long getMaximum() {
      return this.isWeighted() ? this.maximumWeight : this.maximumSize;
   }

   @NonNull
   <K1 extends K, V1 extends V> Weigher<K1, V1> getWeigher(boolean isAsync) {
      Weigher<K1, V1> delegate = this.weigher != null && this.weigher != Weigher.singletonWeigher()
         ? Weigher.boundedWeigher(this.weigher)
         : Weigher.singletonWeigher();
      return (Weigher<K1, V1>)(isAsync ? new Async.AsyncWeigher(delegate) : delegate);
   }

   @NonNull
   public Caffeine<K, V> weakKeys() {
      requireState(this.keyStrength == null, "Key strength was already set to %s", this.keyStrength);
      requireState(this.writer == null, "Weak keys may not be used with CacheWriter");
      this.keyStrength = Caffeine.Strength.WEAK;
      return this;
   }

   boolean isStrongKeys() {
      return this.keyStrength == null;
   }

   @NonNull
   public Caffeine<K, V> weakValues() {
      requireState(this.valueStrength == null, "Value strength was already set to %s", this.valueStrength);
      this.valueStrength = Caffeine.Strength.WEAK;
      return this;
   }

   boolean isStrongValues() {
      return this.valueStrength == null;
   }

   boolean isWeakValues() {
      return this.valueStrength == Caffeine.Strength.WEAK;
   }

   @NonNull
   public Caffeine<K, V> softValues() {
      requireState(this.valueStrength == null, "Value strength was already set to %s", this.valueStrength);
      this.valueStrength = Caffeine.Strength.SOFT;
      return this;
   }

   @NonNull
   public Caffeine<K, V> expireAfterWrite(@NonNull Duration duration) {
      return this.expireAfterWrite(saturatedToNanos(duration), TimeUnit.NANOSECONDS);
   }

   @NonNull
   public Caffeine<K, V> expireAfterWrite(@NonNegative long duration, @NonNull TimeUnit unit) {
      requireState(this.expireAfterWriteNanos == -1L, "expireAfterWrite was already set to %s ns", this.expireAfterWriteNanos);
      requireState(this.expiry == null, "expireAfterWrite may not be used with variable expiration");
      requireArgument(duration >= 0L, "duration cannot be negative: %s %s", duration, unit);
      this.expireAfterWriteNanos = unit.toNanos(duration);
      return this;
   }

   long getExpiresAfterWriteNanos() {
      return this.expiresAfterWrite() ? this.expireAfterWriteNanos : 0L;
   }

   boolean expiresAfterWrite() {
      return this.expireAfterWriteNanos != -1L;
   }

   @NonNull
   public Caffeine<K, V> expireAfterAccess(@NonNull Duration duration) {
      return this.expireAfterAccess(saturatedToNanos(duration), TimeUnit.NANOSECONDS);
   }

   @NonNull
   public Caffeine<K, V> expireAfterAccess(@NonNegative long duration, @NonNull TimeUnit unit) {
      requireState(this.expireAfterAccessNanos == -1L, "expireAfterAccess was already set to %s ns", this.expireAfterAccessNanos);
      requireState(this.expiry == null, "expireAfterAccess may not be used with variable expiration");
      requireArgument(duration >= 0L, "duration cannot be negative: %s %s", duration, unit);
      this.expireAfterAccessNanos = unit.toNanos(duration);
      return this;
   }

   long getExpiresAfterAccessNanos() {
      return this.expiresAfterAccess() ? this.expireAfterAccessNanos : 0L;
   }

   boolean expiresAfterAccess() {
      return this.expireAfterAccessNanos != -1L;
   }

   @NonNull
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> expireAfter(@NonNull Expiry<? super K1, ? super V1> expiry) {
      Objects.requireNonNull(expiry);
      requireState(this.expiry == null, "Expiry was already set to %s", this.expiry);
      requireState(this.expireAfterAccessNanos == -1L, "Expiry may not be used with expiresAfterAccess");
      requireState(this.expireAfterWriteNanos == -1L, "Expiry may not be used with expiresAfterWrite");
      this.expiry = expiry;
      return this;
   }

   boolean expiresVariable() {
      return this.expiry != null;
   }

   @Nullable
   Expiry<K, V> getExpiry(boolean isAsync) {
      return (Expiry<K, V>)(isAsync && this.expiry != null ? new Async.AsyncExpiry<>((Expiry<K, ? super V>)this.expiry) : this.expiry);
   }

   @NonNull
   public Caffeine<K, V> refreshAfterWrite(@NonNull Duration duration) {
      return this.refreshAfterWrite(saturatedToNanos(duration), TimeUnit.NANOSECONDS);
   }

   @NonNull
   public Caffeine<K, V> refreshAfterWrite(@NonNegative long duration, @NonNull TimeUnit unit) {
      Objects.requireNonNull(unit);
      requireState(this.refreshAfterWriteNanos == -1L, "refreshAfterWriteNanos was already set to %s ns", this.refreshAfterWriteNanos);
      requireArgument(duration > 0L, "duration must be positive: %s %s", duration, unit);
      this.refreshAfterWriteNanos = unit.toNanos(duration);
      return this;
   }

   long getRefreshAfterWriteNanos() {
      return this.refreshAfterWrite() ? this.refreshAfterWriteNanos : 0L;
   }

   boolean refreshAfterWrite() {
      return this.refreshAfterWriteNanos != -1L;
   }

   @NonNull
   public Caffeine<K, V> ticker(@NonNull Ticker ticker) {
      requireState(this.ticker == null, "Ticker was already set to %s", this.ticker);
      this.ticker = (Ticker)Objects.requireNonNull(ticker);
      return this;
   }

   @NonNull
   Ticker getTicker() {
      boolean useTicker = this.expiresVariable()
         || this.expiresAfterAccess()
         || this.expiresAfterWrite()
         || this.refreshAfterWrite()
         || this.isRecordingStats();
      return useTicker ? (this.ticker == null ? Ticker.systemTicker() : this.ticker) : Ticker.disabledTicker();
   }

   @NonNull
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> evictionListener(@NonNull RemovalListener<? super K1, ? super V1> evictionListener) {
      requireState(this.evictionListener == null, "eviction listener was already set to %s", this.evictionListener);
      this.evictionListener = (RemovalListener)Objects.requireNonNull(evictionListener);
      return this;
   }

   @NonNull
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> removalListener(@NonNull RemovalListener<? super K1, ? super V1> removalListener) {
      requireState(this.removalListener == null, "removal listener was already set to %s", this.removalListener);
      this.removalListener = (RemovalListener)Objects.requireNonNull(removalListener);
      return this;
   }

   @Nullable
   <K1 extends K, V1 extends V> RemovalListener<K1, V1> getRemovalListener(boolean async) {
      RemovalListener<K1, V1> castedListener = this.removalListener;
      return (RemovalListener<K1, V1>)(async && castedListener != null ? new Async.AsyncRemovalListener(castedListener, this.getExecutor()) : castedListener);
   }

   @Deprecated
   @NonNull
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> writer(@NonNull CacheWriter<? super K1, ? super V1> writer) {
      requireState(this.writer == null, "Writer was already set to %s", this.writer);
      requireState(this.keyStrength == null, "Weak keys may not be used with CacheWriter");
      requireState(this.evictionListener == null, "Eviction listener may not be used with CacheWriter");
      this.writer = (CacheWriter)Objects.requireNonNull(writer);
      return this;
   }

   <K1 extends K, V1 extends V> CacheWriter<K1, V1> getCacheWriter(boolean async) {
      CacheWriter<K1, V1> castedWriter;
      if (this.evictionListener == null) {
         castedWriter = this.writer;
      } else {
         castedWriter = new Caffeine.CacheWriterAdapter<>(this.evictionListener, async);
      }

      return castedWriter == null ? CacheWriter.disabledWriter() : castedWriter;
   }

   @NonNull
   public Caffeine<K, V> recordStats() {
      requireState(this.statsCounterSupplier == null, "Statistics recording was already set");
      this.statsCounterSupplier = ENABLED_STATS_COUNTER_SUPPLIER;
      return this;
   }

   @NonNull
   public Caffeine<K, V> recordStats(@NonNull Supplier<? extends StatsCounter> statsCounterSupplier) {
      requireState(this.statsCounterSupplier == null, "Statistics recording was already set");
      this.statsCounterSupplier = () -> StatsCounter.guardedStatsCounter((StatsCounter)statsCounterSupplier.get());
      return this;
   }

   boolean isRecordingStats() {
      return this.statsCounterSupplier != null;
   }

   @NonNull
   Supplier<StatsCounter> getStatsCounterSupplier() {
      return this.statsCounterSupplier == null ? StatsCounter::disabledStatsCounter : this.statsCounterSupplier;
   }

   boolean isBounded() {
      return this.maximumSize != -1L
         || this.maximumWeight != -1L
         || this.expireAfterAccessNanos != -1L
         || this.expireAfterWriteNanos != -1L
         || this.expiry != null
         || this.keyStrength != null
         || this.valueStrength != null;
   }

   @NonNull
   public <K1 extends K, V1 extends V> Cache<K1, V1> build() {
      this.requireWeightWithWeigher();
      this.requireNonLoadingCache();
      return (Cache<K1, V1>)(this.isBounded()
         ? new BoundedLocalCache.BoundedLocalManualCache<>(this)
         : new UnboundedLocalCache.UnboundedLocalManualCache<>(this));
   }

   @NonNull
   public <K1 extends K, V1 extends V> LoadingCache<K1, V1> build(@NonNull CacheLoader<? super K1, V1> loader) {
      this.requireWeightWithWeigher();
      return (LoadingCache<K1, V1>)(!this.isBounded() && !this.refreshAfterWrite()
         ? new UnboundedLocalCache.UnboundedLocalLoadingCache<>(this, loader)
         : new BoundedLocalCache.BoundedLocalLoadingCache<>(this, loader));
   }

   @NonNull
   public <K1 extends K, V1 extends V> AsyncCache<K1, V1> buildAsync() {
      requireState(this.valueStrength == null, "Weak or soft values can not be combined with AsyncCache");
      requireState(this.writer == null, "CacheWriter can not be combined with AsyncCache");
      requireState(this.isStrongKeys() || this.evictionListener == null, "Weak keys cannot be combined eviction listener and with AsyncLoadingCache");
      this.requireWeightWithWeigher();
      this.requireNonLoadingCache();
      return (AsyncCache<K1, V1>)(this.isBounded()
         ? new BoundedLocalCache.BoundedLocalAsyncCache<>(this)
         : new UnboundedLocalCache.UnboundedLocalAsyncCache<>(this));
   }

   @NonNull
   public <K1 extends K, V1 extends V> AsyncLoadingCache<K1, V1> buildAsync(@NonNull CacheLoader<? super K1, V1> loader) {
      return this.buildAsync(loader);
   }

   @NonNull
   public <K1 extends K, V1 extends V> AsyncLoadingCache<K1, V1> buildAsync(@NonNull AsyncCacheLoader<? super K1, V1> loader) {
      requireState(this.isStrongValues(), "Weak or soft values cannot be combined with AsyncLoadingCache");
      requireState(this.writer == null, "CacheWriter cannot be combined with AsyncLoadingCache");
      requireState(this.isStrongKeys() || this.evictionListener == null, "Weak keys cannot be combined eviction listener and with AsyncLoadingCache");
      this.requireWeightWithWeigher();
      Objects.requireNonNull(loader);
      return (AsyncLoadingCache<K1, V1>)(!this.isBounded() && !this.refreshAfterWrite()
         ? new UnboundedLocalCache.UnboundedLocalAsyncLoadingCache<>(this, loader)
         : new BoundedLocalCache.BoundedLocalAsyncLoadingCache<>(this, loader));
   }

   void requireNonLoadingCache() {
      requireState(this.refreshAfterWriteNanos == -1L, "refreshAfterWrite requires a LoadingCache");
   }

   void requireWeightWithWeigher() {
      if (this.weigher == null) {
         requireState(this.maximumWeight == -1L, "maximumWeight requires weigher");
      } else if (this.strictParsing) {
         requireState(this.maximumWeight != -1L, "weigher requires maximumWeight");
      } else if (this.maximumWeight == -1L) {
         logger.log(Level.WARNING, "ignoring weigher specified without maximumWeight");
      }

   }

   private static long saturatedToNanos(Duration duration) {
      try {
         return duration.toNanos();
      } catch (ArithmeticException var2) {
         return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
      }
   }

   public String toString() {
      StringBuilder s = new StringBuilder(75);
      s.append(this.getClass().getSimpleName()).append('{');
      int baseLength = s.length();
      if (this.initialCapacity != -1) {
         s.append("initialCapacity=").append(this.initialCapacity).append(", ");
      }

      if (this.maximumSize != -1L) {
         s.append("maximumSize=").append(this.maximumSize).append(", ");
      }

      if (this.maximumWeight != -1L) {
         s.append("maximumWeight=").append(this.maximumWeight).append(", ");
      }

      if (this.expireAfterWriteNanos != -1L) {
         s.append("expireAfterWrite=").append(this.expireAfterWriteNanos).append("ns, ");
      }

      if (this.expireAfterAccessNanos != -1L) {
         s.append("expireAfterAccess=").append(this.expireAfterAccessNanos).append("ns, ");
      }

      if (this.expiry != null) {
         s.append("expiry, ");
      }

      if (this.refreshAfterWriteNanos != -1L) {
         s.append("refreshAfterWriteNanos=").append(this.refreshAfterWriteNanos).append("ns, ");
      }

      if (this.keyStrength != null) {
         s.append("keyStrength=").append(this.keyStrength.toString().toLowerCase(Locale.US)).append(", ");
      }

      if (this.valueStrength != null) {
         s.append("valueStrength=").append(this.valueStrength.toString().toLowerCase(Locale.US)).append(", ");
      }

      if (this.evictionListener != null) {
         s.append("evictionListener, ");
      }

      if (this.removalListener != null) {
         s.append("removalListener, ");
      }

      if (this.writer != null) {
         s.append("writer, ");
      }

      if (s.length() > baseLength) {
         s.deleteCharAt(s.length() - 2);
      }

      return s.append('}').toString();
   }

   static final class CacheWriterAdapter<K, V> implements CacheWriter<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final RemovalListener<? super K, ? super V> delegate;
      final boolean isAsync;

      CacheWriterAdapter(RemovalListener<? super K, ? super V> delegate, boolean isAsync) {
         this.delegate = delegate;
         this.isAsync = isAsync;
      }

      @Override
      public void write(K key, V value) {
      }

      @Override
      public void delete(K key, @Nullable V value, RemovalCause cause) {
         if (cause.wasEvicted()) {
            try {
               if (this.isAsync && value != null) {
                  CompletableFuture<V> future = (CompletableFuture)value;
                  value = Async.getIfReady(future);
               }

               this.delegate.onRemoval(key, value, cause);
            } catch (Throwable var5) {
               Caffeine.logger.log(Level.WARNING, "Exception thrown by eviction listener", var5);
            }
         }

      }
   }

   static enum Strength {
      WEAK,
      SOFT;
   }
}
