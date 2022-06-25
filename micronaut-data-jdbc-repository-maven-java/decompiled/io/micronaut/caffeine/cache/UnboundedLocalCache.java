package io.micronaut.caffeine.cache;

import io.micronaut.caffeine.cache.stats.StatsCounter;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;

final class UnboundedLocalCache<K, V> implements LocalCache<K, V> {
   @Nullable
   final RemovalListener<K, V> removalListener;
   final ConcurrentHashMap<K, V> data;
   final StatsCounter statsCounter;
   final boolean isRecordingStats;
   final CacheWriter<K, V> writer;
   final Executor executor;
   final Ticker ticker;
   @Nullable
   transient Set<K> keySet;
   @Nullable
   transient Collection<V> values;
   @Nullable
   transient Set<Entry<K, V>> entrySet;

   UnboundedLocalCache(Caffeine<? super K, ? super V> builder, boolean async) {
      this.data = new ConcurrentHashMap(builder.getInitialCapacity());
      this.statsCounter = (StatsCounter)builder.getStatsCounterSupplier().get();
      this.removalListener = builder.getRemovalListener(async);
      this.isRecordingStats = builder.isRecordingStats();
      this.writer = builder.getCacheWriter(async);
      this.executor = builder.getExecutor();
      this.ticker = builder.getTicker();
   }

   @Override
   public boolean hasWriteTime() {
      return false;
   }

   @Nullable
   @Override
   public V getIfPresent(Object key, boolean recordStats) {
      V value = (V)this.data.get(key);
      if (recordStats) {
         if (value == null) {
            this.statsCounter.recordMisses(1);
         } else {
            this.statsCounter.recordHits(1);
         }
      }

      return value;
   }

   @Nullable
   @Override
   public V getIfPresentQuietly(Object key, long[] writeTime) {
      return (V)this.data.get(key);
   }

   @Override
   public long estimatedSize() {
      return this.data.mappingCount();
   }

   @Override
   public Map<K, V> getAllPresent(Iterable<?> keys) {
      Set<Object> uniqueKeys = new LinkedHashSet();

      for(Object key : keys) {
         uniqueKeys.add(key);
      }

      int misses = 0;
      Map<Object, Object> result = new LinkedHashMap(uniqueKeys.size());

      for(Object key : uniqueKeys) {
         Object value = this.data.get(key);
         if (value == null) {
            ++misses;
         } else {
            result.put(key, value);
         }
      }

      this.statsCounter.recordMisses(misses);
      this.statsCounter.recordHits(result.size());
      return Collections.unmodifiableMap(result);
   }

   @Override
   public void cleanUp() {
   }

   @Override
   public StatsCounter statsCounter() {
      return this.statsCounter;
   }

   @Override
   public boolean hasRemovalListener() {
      return this.removalListener != null;
   }

   @Override
   public RemovalListener<K, V> removalListener() {
      return this.removalListener;
   }

   @Override
   public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
      Objects.requireNonNull(this.removalListener(), "Notification should be guarded with a check");
      this.executor.execute(() -> this.removalListener().onRemoval(key, value, cause));
   }

   @Override
   public boolean isRecordingStats() {
      return this.isRecordingStats;
   }

   @Override
   public Executor executor() {
      return this.executor;
   }

   @Override
   public Ticker expirationTicker() {
      return Ticker.disabledTicker();
   }

   @Override
   public Ticker statsTicker() {
      return this.ticker;
   }

   public void forEach(BiConsumer<? super K, ? super V> action) {
      this.data.forEach(action);
   }

   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      K[] notificationKey = (K[])(new Object[1]);
      V[] notificationValue = (V[])(new Object[1]);
      this.data.replaceAll((key, value) -> {
         if (notificationKey[0] != null) {
            this.notifyRemoval(notificationKey[0], notificationValue[0], RemovalCause.REPLACED);
            notificationValue[0] = null;
            notificationKey[0] = null;
         }

         V newValue = (V)Objects.requireNonNull(function.apply(key, value));
         if (newValue != value) {
            this.writer.write((K)key, newValue);
         }

         if (this.hasRemovalListener() && newValue != value) {
            notificationKey[0] = (K)key;
            notificationValue[0] = (V)value;
         }

         return newValue;
      });
      if (notificationKey[0] != null) {
         this.notifyRemoval(notificationKey[0], notificationValue[0], RemovalCause.REPLACED);
      }

   }

   @Override
   public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, boolean recordStats, boolean recordLoad) {
      Objects.requireNonNull(mappingFunction);
      V value = (V)this.data.get(key);
      if (value != null) {
         if (recordStats) {
            this.statsCounter.recordHits(1);
         }

         return value;
      } else {
         boolean[] missed = new boolean[1];
         value = (V)this.data.computeIfAbsent(key, k -> {
            missed[0] = true;
            return recordStats ? this.statsAware(mappingFunction, recordLoad).apply(key) : mappingFunction.apply(key);
         });
         if (!missed[0] && recordStats) {
            this.statsCounter.recordHits(1);
         }

         return value;
      }
   }

   @Nullable
   public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      if (!this.data.containsKey(key)) {
         return null;
      } else {
         V[] oldValue = (V[])(new Object[1]);
         RemovalCause[] cause = new RemovalCause[1];
         V nv = (V)this.data.computeIfPresent(key, (k, value) -> {
            BiFunction<? super K, ? super V, ? extends V> function = this.statsAware(remappingFunction, false, true, true);
            V newValue = (V)function.apply(k, value);
            cause[0] = newValue == null ? RemovalCause.EXPLICIT : RemovalCause.REPLACED;
            if (this.hasRemovalListener() && newValue != value) {
               oldValue[0] = (V)value;
            }

            return newValue;
         });
         if (oldValue[0] != null) {
            this.notifyRemoval(key, oldValue[0], cause[0]);
         }

         return nv;
      }
   }

   @Override
   public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, boolean recordMiss, boolean recordLoad, boolean recordLoadFailure) {
      Objects.requireNonNull(remappingFunction);
      return this.remap(key, this.statsAware(remappingFunction, recordMiss, recordLoad, recordLoadFailure));
   }

   public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      Objects.requireNonNull(value);
      return this.remap(key, (k, oldValue) -> oldValue == null ? value : this.statsAware(remappingFunction).apply(oldValue, value));
   }

   V remap(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      V[] oldValue = (V[])(new Object[1]);
      RemovalCause[] cause = new RemovalCause[1];
      V nv = (V)this.data.compute(key, (k, value) -> {
         V newValue = (V)remappingFunction.apply(k, value);
         if (value == null && newValue == null) {
            return null;
         } else {
            cause[0] = newValue == null ? RemovalCause.EXPLICIT : RemovalCause.REPLACED;
            if (this.hasRemovalListener() && value != null && newValue != value) {
               oldValue[0] = (V)value;
            }

            return newValue;
         }
      });
      if (oldValue[0] != null) {
         this.notifyRemoval(key, oldValue[0], cause[0]);
      }

      return nv;
   }

   public boolean isEmpty() {
      return this.data.isEmpty();
   }

   public int size() {
      return this.data.size();
   }

   public void clear() {
      if (!this.hasRemovalListener() && this.writer == CacheWriter.disabledWriter()) {
         this.data.clear();
      } else {
         for(K key : this.data.keySet()) {
            this.remove(key);
         }

      }
   }

   public boolean containsKey(Object key) {
      return this.data.containsKey(key);
   }

   public boolean containsValue(Object value) {
      return this.data.containsValue(value);
   }

   @Nullable
   public V get(Object key) {
      return this.getIfPresent(key, false);
   }

   @Nullable
   public V put(K key, V value) {
      return this.put(key, value, true);
   }

   @Nullable
   @Override
   public V put(K key, V value, boolean notifyWriter) {
      V[] oldValue = (V[])(new Object[1]);
      if (this.writer != CacheWriter.disabledWriter() && notifyWriter) {
         this.data.compute(key, (k, v) -> {
            if (value != v) {
               this.writer.write(key, value);
            }

            oldValue[0] = (V)v;
            return value;
         });
      } else {
         oldValue[0] = (V)this.data.put(key, value);
      }

      if (this.hasRemovalListener() && oldValue[0] != null && oldValue[0] != value) {
         this.notifyRemoval(key, oldValue[0], RemovalCause.REPLACED);
      }

      return oldValue[0];
   }

   @Nullable
   public V putIfAbsent(K key, V value) {
      boolean[] wasAbsent = new boolean[1];
      V val = (V)this.data.computeIfAbsent(key, k -> {
         this.writer.write(key, value);
         wasAbsent[0] = true;
         return value;
      });
      return wasAbsent[0] ? null : val;
   }

   public void putAll(Map<? extends K, ? extends V> map) {
      if (!this.hasRemovalListener() && this.writer == CacheWriter.disabledWriter()) {
         this.data.putAll(map);
      } else {
         map.forEach(this::put);
      }
   }

   @Nullable
   public V remove(Object key) {
      V[] oldValue = (V[])(new Object[1]);
      if (this.writer == CacheWriter.disabledWriter()) {
         oldValue[0] = (V)this.data.remove(key);
      } else {
         this.data.computeIfPresent(key, (k, v) -> {
            this.writer.delete((K)key, (V)v, RemovalCause.EXPLICIT);
            oldValue[0] = (V)v;
            return null;
         });
      }

      if (this.hasRemovalListener() && oldValue[0] != null) {
         this.notifyRemoval((K)key, oldValue[0], RemovalCause.EXPLICIT);
      }

      return oldValue[0];
   }

   public boolean remove(Object key, Object value) {
      if (value == null) {
         return false;
      } else {
         V[] oldValue = (V[])(new Object[1]);
         this.data.computeIfPresent(key, (k, v) -> {
            if (v.equals(value)) {
               this.writer.delete((K)key, (V)v, RemovalCause.EXPLICIT);
               oldValue[0] = (V)v;
               return null;
            } else {
               return v;
            }
         });
         boolean removed = oldValue[0] != null;
         if (this.hasRemovalListener() && removed) {
            this.notifyRemoval((K)key, oldValue[0], RemovalCause.EXPLICIT);
         }

         return removed;
      }
   }

   @Nullable
   public V replace(K key, V value) {
      V[] oldValue = (V[])(new Object[1]);
      this.data.computeIfPresent(key, (k, v) -> {
         if (value != v) {
            this.writer.write(key, value);
         }

         oldValue[0] = (V)v;
         return value;
      });
      if (this.hasRemovalListener() && oldValue[0] != null && oldValue[0] != value) {
         this.notifyRemoval(key, value, RemovalCause.REPLACED);
      }

      return oldValue[0];
   }

   public boolean replace(K key, V oldValue, V newValue) {
      Objects.requireNonNull(oldValue);
      V[] prev = (V[])(new Object[1]);
      this.data.computeIfPresent(key, (k, v) -> {
         if (v.equals(oldValue)) {
            if (newValue != v) {
               this.writer.write(key, newValue);
            }

            prev[0] = (V)v;
            return newValue;
         } else {
            return v;
         }
      });
      boolean replaced = prev[0] != null;
      if (this.hasRemovalListener() && replaced && prev[0] != newValue) {
         this.notifyRemoval(key, prev[0], RemovalCause.REPLACED);
      }

      return replaced;
   }

   public boolean equals(Object o) {
      return this.data.equals(o);
   }

   public int hashCode() {
      return this.data.hashCode();
   }

   public String toString() {
      return this.data.toString();
   }

   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      return ks == null ? (this.keySet = new UnboundedLocalCache.KeySetView<K>(this)) : ks;
   }

   public Collection<V> values() {
      Collection<V> vs = this.values;
      return vs == null ? (this.values = new UnboundedLocalCache.ValuesView<K, V>(this)) : vs;
   }

   public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> es = this.entrySet;
      return es == null ? (this.entrySet = new UnboundedLocalCache.EntrySetView<K, V>(this)) : es;
   }

   static final class EntryIterator<K, V> implements Iterator<Entry<K, V>> {
      final UnboundedLocalCache<K, V> cache;
      final Iterator<Entry<K, V>> iterator;
      @Nullable
      Entry<K, V> entry;

      EntryIterator(UnboundedLocalCache<K, V> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
         this.iterator = cache.data.entrySet().iterator();
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public Entry<K, V> next() {
         this.entry = (Entry)this.iterator.next();
         return new WriteThroughEntry<K, V>(this.cache, (K)this.entry.getKey(), (V)this.entry.getValue());
      }

      public void remove() {
         if (this.entry == null) {
            throw new IllegalStateException();
         } else {
            this.cache.remove(this.entry.getKey());
            this.entry = null;
         }
      }
   }

   static final class EntrySetView<K, V> extends AbstractSet<Entry<K, V>> {
      final UnboundedLocalCache<K, V> cache;

      EntrySetView(UnboundedLocalCache<K, V> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public boolean isEmpty() {
         return this.cache.isEmpty();
      }

      public int size() {
         return this.cache.size();
      }

      public void clear() {
         this.cache.clear();
      }

      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> entry = (Entry)o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
               V cachedValue = this.cache.get(key);
               return cachedValue != null && cachedValue.equals(value);
            } else {
               return false;
            }
         }
      }

      public boolean remove(Object obj) {
         if (!(obj instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> entry = (Entry)obj;
            return this.cache.remove(entry.getKey(), entry.getValue());
         }
      }

      public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
         boolean removed = false;

         for(Entry<K, V> entry : this.cache.data.entrySet()) {
            if (filter.test(entry)) {
               removed |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return removed;
      }

      public Iterator<Entry<K, V>> iterator() {
         return new UnboundedLocalCache.EntryIterator<>(this.cache);
      }

      public Spliterator<Entry<K, V>> spliterator() {
         return new UnboundedLocalCache.EntrySpliterator<>(this.cache);
      }
   }

   static final class EntrySpliterator<K, V> implements Spliterator<Entry<K, V>> {
      final Spliterator<Entry<K, V>> spliterator;
      final UnboundedLocalCache<K, V> cache;

      EntrySpliterator(UnboundedLocalCache<K, V> cache) {
         this(cache, cache.data.entrySet().spliterator());
      }

      EntrySpliterator(UnboundedLocalCache<K, V> cache, Spliterator<Entry<K, V>> spliterator) {
         this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
         Objects.requireNonNull(action);
         this.spliterator.forEachRemaining(entry -> {
            Entry<K, V> e = new WriteThroughEntry<K, V>(this.cache, (K)entry.getKey(), (V)entry.getValue());
            action.accept(e);
         });
      }

      public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
         Objects.requireNonNull(action);
         return this.spliterator.tryAdvance(entry -> {
            Entry<K, V> e = new WriteThroughEntry<K, V>(this.cache, (K)entry.getKey(), (V)entry.getValue());
            action.accept(e);
         });
      }

      @Nullable
      public UnboundedLocalCache.EntrySpliterator<K, V> trySplit() {
         Spliterator<Entry<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new UnboundedLocalCache.EntrySpliterator<>(this.cache, split);
      }

      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      public int characteristics() {
         return this.spliterator.characteristics();
      }
   }

   static final class KeyIterator<K> implements Iterator<K> {
      final UnboundedLocalCache<K, ?> cache;
      final Iterator<K> iterator;
      @Nullable
      K current;

      KeyIterator(UnboundedLocalCache<K, ?> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
         this.iterator = cache.data.keySet().iterator();
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public K next() {
         this.current = (K)this.iterator.next();
         return this.current;
      }

      public void remove() {
         if (this.current == null) {
            throw new IllegalStateException();
         } else {
            this.cache.remove(this.current);
            this.current = null;
         }
      }
   }

   static final class KeySetView<K> extends AbstractSet<K> {
      final UnboundedLocalCache<K, ?> cache;

      KeySetView(UnboundedLocalCache<K, ?> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public boolean isEmpty() {
         return this.cache.isEmpty();
      }

      public int size() {
         return this.cache.size();
      }

      public void clear() {
         this.cache.clear();
      }

      public boolean contains(Object o) {
         return this.cache.containsKey(o);
      }

      public boolean remove(Object obj) {
         return this.cache.remove(obj) != null;
      }

      public Iterator<K> iterator() {
         return new UnboundedLocalCache.KeyIterator<>(this.cache);
      }

      public Spliterator<K> spliterator() {
         return this.cache.data.keySet().spliterator();
      }
   }

   static final class UnboundedLocalAsyncCache<K, V> implements LocalAsyncCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, CompletableFuture<V>> cache;
      @Nullable
      ConcurrentMap<K, CompletableFuture<V>> mapView;
      @Nullable
      LocalAsyncCache.CacheView<K, V> cacheView;
      @Nullable
      Policy<K, V> policy;

      UnboundedLocalAsyncCache(Caffeine<K, V> builder) {
         this.cache = new UnboundedLocalCache<>(builder, true);
      }

      public UnboundedLocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      @Override
      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<>(this)) : this.mapView;
      }

      @Override
      public Cache<K, V> synchronous() {
         return this.cacheView == null ? (this.cacheView = new LocalAsyncCache.CacheView<>(this)) : this.cacheView;
      }

      @Override
      public Policy<K, V> policy() {
         UnboundedLocalCache<K, V> castCache = this.cache;
         Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
         return this.policy == null ? (this.policy = new UnboundedLocalCache.UnboundedPolicy<>(castCache, transformer)) : this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<>();
         proxy.isRecordingStats = this.cache.isRecordingStats;
         proxy.removalListener = this.cache.removalListener;
         proxy.ticker = this.cache.ticker;
         proxy.writer = this.cache.writer;
         proxy.async = true;
         return proxy;
      }
   }

   static final class UnboundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<K, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, CompletableFuture<V>> cache;
      @Nullable
      ConcurrentMap<K, CompletableFuture<V>> mapView;
      @Nullable
      Policy<K, V> policy;

      UnboundedLocalAsyncLoadingCache(Caffeine<K, V> builder, AsyncCacheLoader<? super K, V> loader) {
         super(loader);
         this.cache = new UnboundedLocalCache<>(builder, true);
      }

      @Override
      public LocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      @Override
      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<>(this)) : this.mapView;
      }

      @Override
      public Policy<K, V> policy() {
         UnboundedLocalCache<K, V> castCache = this.cache;
         Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
         return this.policy == null ? (this.policy = new UnboundedLocalCache.UnboundedPolicy<>(castCache, transformer)) : this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<>();
         proxy.isRecordingStats = this.cache.isRecordingStats();
         proxy.removalListener = this.cache.removalListener();
         proxy.ticker = this.cache.ticker;
         proxy.writer = this.cache.writer;
         proxy.loader = this.loader;
         proxy.async = true;
         return proxy;
      }
   }

   static final class UnboundedLocalLoadingCache<K, V> extends UnboundedLocalCache.UnboundedLocalManualCache<K, V> implements LocalLoadingCache<K, V> {
      private static final long serialVersionUID = 1L;
      final Function<K, V> mappingFunction;
      final CacheLoader<? super K, V> loader;
      @Nullable
      final Function<Iterable<? extends K>, Map<K, V>> bulkMappingFunction;

      UnboundedLocalLoadingCache(Caffeine<K, V> builder, CacheLoader<? super K, V> loader) {
         super(builder);
         this.loader = loader;
         this.mappingFunction = LocalLoadingCache.newMappingFunction(loader);
         this.bulkMappingFunction = LocalLoadingCache.newBulkMappingFunction(loader);
      }

      @Override
      public CacheLoader<? super K, V> cacheLoader() {
         return this.loader;
      }

      @Override
      public Function<K, V> mappingFunction() {
         return this.mappingFunction;
      }

      @Nullable
      @Override
      public Function<Iterable<? extends K>, Map<K, V>> bulkMappingFunction() {
         return this.bulkMappingFunction;
      }

      @Override
      Object writeReplace() {
         SerializationProxy<K, V> proxy = (SerializationProxy)super.writeReplace();
         proxy.loader = this.loader;
         return proxy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }
   }

   static class UnboundedLocalManualCache<K, V> implements LocalManualCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, V> cache;
      @Nullable
      Policy<K, V> policy;

      UnboundedLocalManualCache(Caffeine<K, V> builder) {
         this.cache = new UnboundedLocalCache<>(builder, false);
      }

      public UnboundedLocalCache<K, V> cache() {
         return this.cache;
      }

      @Override
      public Policy<K, V> policy() {
         return this.policy == null ? (this.policy = new UnboundedLocalCache.UnboundedPolicy<>(this.cache, Function.identity())) : this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<>();
         proxy.isRecordingStats = this.cache.isRecordingStats;
         proxy.removalListener = this.cache.removalListener;
         proxy.ticker = this.cache.ticker;
         proxy.writer = this.cache.writer;
         return proxy;
      }
   }

   static final class UnboundedPolicy<K, V> implements Policy<K, V> {
      final UnboundedLocalCache<K, V> cache;
      final Function<V, V> transformer;

      UnboundedPolicy(UnboundedLocalCache<K, V> cache, Function<V, V> transformer) {
         this.transformer = transformer;
         this.cache = cache;
      }

      @Override
      public boolean isRecordingStats() {
         return this.cache.isRecordingStats;
      }

      @Override
      public V getIfPresentQuietly(Object key) {
         return (V)this.transformer.apply(this.cache.data.get(key));
      }

      @Override
      public Optional<Policy.Eviction<K, V>> eviction() {
         return Optional.empty();
      }

      @Override
      public Optional<Policy.Expiration<K, V>> expireAfterAccess() {
         return Optional.empty();
      }

      @Override
      public Optional<Policy.Expiration<K, V>> expireAfterWrite() {
         return Optional.empty();
      }

      @Override
      public Optional<Policy.Expiration<K, V>> refreshAfterWrite() {
         return Optional.empty();
      }
   }

   static final class ValuesIterator<K, V> implements Iterator<V> {
      final UnboundedLocalCache<K, V> cache;
      final Iterator<Entry<K, V>> iterator;
      @Nullable
      Entry<K, V> entry;

      ValuesIterator(UnboundedLocalCache<K, V> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
         this.iterator = cache.data.entrySet().iterator();
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public V next() {
         this.entry = (Entry)this.iterator.next();
         return (V)this.entry.getValue();
      }

      public void remove() {
         if (this.entry == null) {
            throw new IllegalStateException();
         } else {
            this.cache.remove(this.entry.getKey());
            this.entry = null;
         }
      }
   }

   static final class ValuesView<K, V> extends AbstractCollection<V> {
      final UnboundedLocalCache<K, V> cache;

      ValuesView(UnboundedLocalCache<K, V> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public boolean isEmpty() {
         return this.cache.isEmpty();
      }

      public int size() {
         return this.cache.size();
      }

      public void clear() {
         this.cache.clear();
      }

      public boolean contains(Object o) {
         return this.cache.containsValue(o);
      }

      public boolean removeIf(Predicate<? super V> filter) {
         boolean removed = false;

         for(Entry<K, V> entry : this.cache.data.entrySet()) {
            if (filter.test(entry.getValue())) {
               removed |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return removed;
      }

      public Iterator<V> iterator() {
         return new UnboundedLocalCache.ValuesIterator<>(this.cache);
      }

      public Spliterator<V> spliterator() {
         return this.cache.data.values().spliterator();
      }
   }
}
