package io.micronaut.caffeine.cache;

import io.micronaut.caffeine.cache.stats.CacheStats;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

interface LocalAsyncCache<K, V> extends AsyncCache<K, V> {
   Logger logger = Logger.getLogger(LocalAsyncCache.class.getName());

   LocalCache<K, CompletableFuture<V>> cache();

   Policy<K, V> policy();

   @Nullable
   @Override
   default CompletableFuture<V> getIfPresent(@NonNull Object key) {
      return (CompletableFuture<V>)this.cache().getIfPresent(key, true);
   }

   @Override
   default CompletableFuture<V> get(@NonNull K key, @NonNull Function<? super K, ? extends V> mappingFunction) {
      return this.get(
         key,
         (BiFunction<? super K, Executor, CompletableFuture<V>>)((k1, executor) -> CompletableFuture.supplyAsync(() -> mappingFunction.apply(key), executor))
      );
   }

   @Override
   default CompletableFuture<V> get(K key, BiFunction<? super K, Executor, CompletableFuture<V>> mappingFunction) {
      return this.get(key, mappingFunction, true);
   }

   default CompletableFuture<V> get(K key, BiFunction<? super K, Executor, CompletableFuture<V>> mappingFunction, boolean recordStats) {
      long startTime = this.cache().statsTicker().read();
      CompletableFuture<V>[] result = new CompletableFuture[1];
      CompletableFuture<V> future = (CompletableFuture)this.cache().computeIfAbsent(key, k -> {
         result[0] = (CompletableFuture)mappingFunction.apply(key, this.cache().executor());
         return (CompletableFuture)Objects.requireNonNull(result[0]);
      }, recordStats, false);
      if (result[0] != null) {
         this.handleCompletion(key, result[0], startTime, false);
      }

      return future;
   }

   @Override
   default CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys, Function<Iterable<? extends K>, Map<K, V>> mappingFunction) {
      return this.getAll(
         keys,
         (BiFunction<Iterable<? extends K>, Executor, CompletableFuture<Map<K, V>>>)((keysToLoad, executor) -> CompletableFuture.supplyAsync(
               () -> (Map)mappingFunction.apply(keysToLoad), executor
            ))
      );
   }

   @Override
   default CompletableFuture<Map<K, V>> getAll(
      Iterable<? extends K> keys, BiFunction<Iterable<? extends K>, Executor, CompletableFuture<Map<K, V>>> mappingFunction
   ) {
      Objects.requireNonNull(mappingFunction);
      Objects.requireNonNull(keys);
      Map<K, CompletableFuture<V>> futures = new LinkedHashMap();
      Map<K, CompletableFuture<V>> proxies = new HashMap();

      for(K key : keys) {
         if (!futures.containsKey(key)) {
            CompletableFuture<V> future = (CompletableFuture)this.cache().getIfPresent(key, false);
            if (future == null) {
               CompletableFuture<V> proxy = new CompletableFuture();
               future = (CompletableFuture)this.cache().putIfAbsent(key, proxy);
               if (future == null) {
                  future = proxy;
                  proxies.put(key, proxy);
               }
            }

            futures.put(key, future);
         }
      }

      this.cache().statsCounter().recordMisses(proxies.size());
      this.cache().statsCounter().recordHits(futures.size() - proxies.size());
      if (proxies.isEmpty()) {
         return this.composeResult(futures);
      } else {
         LocalAsyncCache.AsyncBulkCompleter<K, V> completer = new LocalAsyncCache.AsyncBulkCompleter<>(this.cache(), proxies);

         try {
            ((CompletableFuture)mappingFunction.apply(proxies.keySet(), this.cache().executor())).whenComplete(completer);
            return this.composeResult(futures);
         } catch (Throwable var9) {
            completer.accept(null, var9);
            throw var9;
         }
      }
   }

   default CompletableFuture<Map<K, V>> composeResult(Map<K, CompletableFuture<V>> futures) {
      if (futures.isEmpty()) {
         return CompletableFuture.completedFuture(Collections.emptyMap());
      } else {
         CompletableFuture<?>[] array = (CompletableFuture[])futures.values().toArray(new CompletableFuture[0]);
         return CompletableFuture.allOf(array).thenApply(ignored -> {
            Map<K, V> result = new LinkedHashMap(futures.size());
            futures.forEach((key, future) -> {
               V value = (V)future.getNow(null);
               if (value != null) {
                  result.put(key, value);
               }

            });
            return Collections.unmodifiableMap(result);
         });
      }
   }

   @Override
   default void put(K key, CompletableFuture<V> valueFuture) {
      if (!valueFuture.isCompletedExceptionally() && (!valueFuture.isDone() || valueFuture.join() != null)) {
         long startTime = this.cache().statsTicker().read();
         this.cache().put(key, valueFuture);
         this.handleCompletion(key, valueFuture, startTime, false);
      } else {
         this.cache().statsCounter().recordLoadFailure(0L);
         this.cache().remove(key);
      }
   }

   default void handleCompletion(K key, CompletableFuture<V> valueFuture, long startTime, boolean recordMiss) {
      AtomicBoolean completed = new AtomicBoolean();
      valueFuture.whenComplete((value, error) -> {
         if (completed.compareAndSet(false, true)) {
            long loadTime = this.cache().statsTicker().read() - startTime;
            if (value == null) {
               if (error != null && !(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
                  logger.log(Level.WARNING, "Exception thrown during asynchronous load", error);
               }

               this.cache().remove(key, valueFuture);
               this.cache().statsCounter().recordLoadFailure(loadTime);
               if (recordMiss) {
                  this.cache().statsCounter().recordMisses(1);
               }
            } else {
               this.cache().replace(key, valueFuture, valueFuture);
               this.cache().statsCounter().recordLoadSuccess(loadTime);
               if (recordMiss) {
                  this.cache().statsCounter().recordMisses(1);
               }
            }

         }
      });
   }

   public abstract static class AbstractCacheView<K, V> implements Cache<K, V>, Serializable {
      @Nullable
      transient LocalAsyncCache.AsMapView<K, V> asMapView;

      abstract LocalAsyncCache<K, V> asyncCache();

      @Nullable
      @Override
      public V getIfPresent(Object key) {
         CompletableFuture<V> future = (CompletableFuture)this.asyncCache().cache().getIfPresent(key, true);
         return Async.getIfReady(future);
      }

      @Override
      public Map<K, V> getAllPresent(Iterable<?> keys) {
         Set<Object> uniqueKeys = new LinkedHashSet();

         for(Object key : keys) {
            uniqueKeys.add(key);
         }

         int misses = 0;
         Map<Object, Object> result = new LinkedHashMap();

         for(Object key : uniqueKeys) {
            CompletableFuture<V> future = (CompletableFuture)this.asyncCache().cache().get(key);
            Object value = Async.getIfReady(future);
            if (value == null) {
               ++misses;
            } else {
               result.put(key, value);
            }
         }

         this.asyncCache().cache().statsCounter().recordMisses(misses);
         this.asyncCache().cache().statsCounter().recordHits(result.size());
         return Collections.unmodifiableMap(result);
      }

      @Override
      public V get(K key, Function<? super K, ? extends V> mappingFunction) {
         return resolve(this.asyncCache().get(key, mappingFunction));
      }

      @Override
      public Map<K, V> getAll(Iterable<? extends K> keys, Function<Iterable<? extends K>, Map<K, V>> mappingFunction) {
         return resolve(this.asyncCache().getAll(keys, mappingFunction));
      }

      protected static <T> T resolve(CompletableFuture<T> future) throws Error {
         try {
            return (T)future.get();
         } catch (ExecutionException var2) {
            if (var2.getCause() instanceof LocalAsyncCache.AsyncBulkCompleter.NullMapCompletionException) {
               throw new NullPointerException(var2.getCause().getMessage());
            } else if (var2.getCause() instanceof RuntimeException) {
               throw (RuntimeException)var2.getCause();
            } else if (var2.getCause() instanceof Error) {
               throw (Error)var2.getCause();
            } else {
               throw new CompletionException(var2.getCause());
            }
         } catch (InterruptedException var3) {
            throw new CompletionException(var3);
         }
      }

      @Override
      public void put(K key, V value) {
         Objects.requireNonNull(value);
         this.asyncCache().cache().put(key, CompletableFuture.completedFuture(value));
      }

      @Override
      public void putAll(Map<? extends K, ? extends V> map) {
         map.forEach(this::put);
      }

      @Override
      public void invalidate(Object key) {
         this.asyncCache().cache().remove(key);
      }

      @Override
      public void invalidateAll(Iterable<?> keys) {
         this.asyncCache().cache().invalidateAll(keys);
      }

      @Override
      public void invalidateAll() {
         this.asyncCache().cache().clear();
      }

      @Override
      public long estimatedSize() {
         return this.asyncCache().cache().estimatedSize();
      }

      @Override
      public CacheStats stats() {
         return this.asyncCache().cache().statsCounter().snapshot();
      }

      @Override
      public void cleanUp() {
         this.asyncCache().cache().cleanUp();
      }

      @Override
      public Policy<K, V> policy() {
         return this.asyncCache().policy();
      }

      @Override
      public ConcurrentMap<K, V> asMap() {
         return this.asMapView == null ? (this.asMapView = new LocalAsyncCache.AsMapView<>(this.asyncCache().cache())) : this.asMapView;
      }
   }

   public static final class AsMapView<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
      final LocalCache<K, CompletableFuture<V>> delegate;
      @Nullable
      Collection<V> values;
      @Nullable
      Set<Entry<K, V>> entries;

      AsMapView(LocalCache<K, CompletableFuture<V>> delegate) {
         this.delegate = delegate;
      }

      public boolean isEmpty() {
         return this.delegate.isEmpty();
      }

      public int size() {
         return this.delegate.size();
      }

      public void clear() {
         this.delegate.clear();
      }

      public boolean containsKey(Object key) {
         return this.delegate.containsKey(key);
      }

      public boolean containsValue(Object value) {
         Objects.requireNonNull(value);

         for(CompletableFuture<V> valueFuture : this.delegate.values()) {
            if (value.equals(Async.getIfReady(valueFuture))) {
               return true;
            }
         }

         return false;
      }

      @Nullable
      public V get(Object key) {
         return Async.getIfReady((CompletableFuture<V>)this.delegate.get(key));
      }

      @Nullable
      public V putIfAbsent(K key, V value) {
         Objects.requireNonNull(value);

         while(true) {
            CompletableFuture<V> priorFuture = (CompletableFuture)this.delegate.get(key);
            if (priorFuture != null) {
               if (!priorFuture.isDone()) {
                  Async.getWhenSuccessful(priorFuture);
                  continue;
               }

               V prior = Async.getWhenSuccessful(priorFuture);
               if (prior != null) {
                  return prior;
               }
            }

            boolean[] added = new boolean[]{false};
            CompletableFuture<V> computed = (CompletableFuture)this.delegate.compute(key, (k, valueFuture) -> {
               added[0] = valueFuture == null || valueFuture.isDone() && Async.getIfReady(valueFuture) == null;
               return added[0] ? CompletableFuture.completedFuture(value) : valueFuture;
            }, false, false, false);
            if (added[0]) {
               return null;
            }

            V prior = Async.getWhenSuccessful(computed);
            if (prior != null) {
               return prior;
            }
         }
      }

      @Nullable
      public V put(K key, V value) {
         Objects.requireNonNull(value);
         CompletableFuture<V> oldValueFuture = (CompletableFuture)this.delegate.put(key, CompletableFuture.completedFuture(value));
         return Async.getWhenSuccessful(oldValueFuture);
      }

      @Nullable
      public V remove(Object key) {
         CompletableFuture<V> oldValueFuture = (CompletableFuture)this.delegate.remove(key);
         return Async.getWhenSuccessful(oldValueFuture);
      }

      public boolean remove(Object key, Object value) {
         Objects.requireNonNull(key);
         if (value == null) {
            return false;
         } else {
            K castedKey = (K)key;
            boolean[] done = new boolean[]{false};
            boolean[] removed = new boolean[]{false};

            do {
               CompletableFuture<V> future = (CompletableFuture)this.delegate.get(key);
               if (future == null || future.isCompletedExceptionally()) {
                  return false;
               }

               Async.getWhenSuccessful(future);
               this.delegate.compute(castedKey, (k, oldValueFuture) -> {
                  if (oldValueFuture == null) {
                     done[0] = true;
                     return null;
                  } else if (!oldValueFuture.isDone()) {
                     return oldValueFuture;
                  } else {
                     done[0] = true;
                     V oldValue = Async.getIfReady(oldValueFuture);
                     removed[0] = value.equals(oldValue);
                     return oldValue != null && !removed[0] ? oldValueFuture : null;
                  }
               }, false, false, true);
            } while(!done[0]);

            return removed[0];
         }
      }

      @Nullable
      public V replace(K key, V value) {
         V[] oldValue = (V[])(new Object[1]);
         boolean[] done = new boolean[]{false};

         do {
            CompletableFuture<V> future = (CompletableFuture)this.delegate.get(key);
            if (future == null || future.isCompletedExceptionally()) {
               return null;
            }

            Async.getWhenSuccessful(future);
            this.delegate.compute(key, (k, oldValueFuture) -> {
               if (oldValueFuture == null) {
                  done[0] = true;
                  return null;
               } else if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  done[0] = true;
                  oldValue[0] = Async.getIfReady(oldValueFuture);
                  return oldValue[0] == null ? null : CompletableFuture.completedFuture(value);
               }
            }, false, false, false);
         } while(!done[0]);

         return oldValue[0];
      }

      public boolean replace(K key, V oldValue, V newValue) {
         Objects.requireNonNull(oldValue);
         boolean[] done = new boolean[]{false};
         boolean[] replaced = new boolean[]{false};

         do {
            CompletableFuture<V> future = (CompletableFuture)this.delegate.get(key);
            if (future == null || future.isCompletedExceptionally()) {
               return false;
            }

            Async.getWhenSuccessful(future);
            this.delegate.compute(key, (k, oldValueFuture) -> {
               if (oldValueFuture == null) {
                  done[0] = true;
                  return null;
               } else if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  done[0] = true;
                  replaced[0] = oldValue.equals(Async.getIfReady(oldValueFuture));
                  return replaced[0] ? CompletableFuture.completedFuture(newValue) : oldValueFuture;
               }
            }, false, false, false);
         } while(!done[0]);

         return replaced[0];
      }

      @Nullable
      public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
         Objects.requireNonNull(mappingFunction);

         while(true) {
            while(true) {
               CompletableFuture<V> priorFuture = (CompletableFuture)this.delegate.get(key);
               if (priorFuture == null) {
                  break;
               }

               if (priorFuture.isDone()) {
                  V prior = Async.getWhenSuccessful(priorFuture);
                  if (prior != null) {
                     this.delegate.statsCounter().recordHits(1);
                     return prior;
                  }
                  break;
               }

               Async.getWhenSuccessful(priorFuture);
            }

            CompletableFuture<V>[] future = new CompletableFuture[1];
            CompletableFuture<V> computed = (CompletableFuture)this.delegate.compute(key, (k, valueFuture) -> {
               if (valueFuture != null && valueFuture.isDone() && Async.getIfReady(valueFuture) != null) {
                  return valueFuture;
               } else {
                  V newValue = (V)this.delegate.statsAware(mappingFunction, true).apply(key);
                  if (newValue == null) {
                     return null;
                  } else {
                     future[0] = CompletableFuture.completedFuture(newValue);
                     return future[0];
                  }
               }
            }, false, false, false);
            V result = Async.getWhenSuccessful(computed);
            if (computed == future[0] || result != null) {
               return result;
            }
         }
      }

      @Nullable
      public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
         V[] newValue = (V[])(new Object[1]);

         CompletableFuture<V> valueFuture;
         do {
            Async.getWhenSuccessful((CompletableFuture<V>)this.delegate.get(key));
            valueFuture = (CompletableFuture)this.delegate.computeIfPresent(key, (k, oldValueFuture) -> {
               if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  V oldValue = Async.getIfReady(oldValueFuture);
                  if (oldValue == null) {
                     return null;
                  } else {
                     newValue[0] = (V)remappingFunction.apply(key, oldValue);
                     return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
                  }
               }
            });
            if (newValue[0] != null) {
               return newValue[0];
            }
         } while(valueFuture != null);

         return null;
      }

      @Nullable
      public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
         V[] newValue = (V[])(new Object[1]);

         CompletableFuture<V> valueFuture;
         do {
            Async.getWhenSuccessful((CompletableFuture<V>)this.delegate.get(key));
            valueFuture = (CompletableFuture)this.delegate.compute(key, (k, oldValueFuture) -> {
               if (oldValueFuture != null && !oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  V oldValue = Async.getIfReady(oldValueFuture);
                  BiFunction<? super K, ? super V, ? extends V> function = this.delegate.statsAware(remappingFunction, false, true, true);
                  newValue[0] = (V)function.apply(key, oldValue);
                  return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
               }
            }, false, false, false);
            if (newValue[0] != null) {
               return newValue[0];
            }
         } while(valueFuture != null);

         return null;
      }

      @Nullable
      public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
         Objects.requireNonNull(value);
         Objects.requireNonNull(remappingFunction);
         CompletableFuture<V> newValueFuture = CompletableFuture.completedFuture(value);
         boolean[] merged = new boolean[]{false};

         CompletableFuture<V> mergedValueFuture;
         do {
            Async.getWhenSuccessful((CompletableFuture<V>)this.delegate.get(key));
            mergedValueFuture = (CompletableFuture)this.delegate.merge(key, newValueFuture, (oldValueFuture, valueFuture) -> {
               if (oldValueFuture != null && !oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  merged[0] = true;
                  V oldValue = Async.getIfReady(oldValueFuture);
                  if (oldValue == null) {
                     return valueFuture;
                  } else {
                     V mergedValue = (V)remappingFunction.apply(oldValue, value);
                     if (mergedValue == null) {
                        return null;
                     } else if (mergedValue == oldValue) {
                        return oldValueFuture;
                     } else {
                        return mergedValue == value ? valueFuture : CompletableFuture.completedFuture(mergedValue);
                     }
                  }
               }
            });
         } while(!merged[0] && mergedValueFuture != newValueFuture);

         return Async.getWhenSuccessful(mergedValueFuture);
      }

      public Set<K> keySet() {
         return this.delegate.keySet();
      }

      public Collection<V> values() {
         return this.values == null ? (this.values = new LocalAsyncCache.AsMapView.Values()) : this.values;
      }

      public Set<Entry<K, V>> entrySet() {
         return this.entries == null ? (this.entries = new LocalAsyncCache.AsMapView.EntrySet()) : this.entries;
      }

      private final class EntrySet extends AbstractSet<Entry<K, V>> {
         private EntrySet() {
         }

         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         public int size() {
            return AsMapView.this.size();
         }

         public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
               return false;
            } else {
               Entry<?, ?> entry = (Entry)o;
               Object key = entry.getKey();
               Object value = entry.getValue();
               if (key != null && value != null) {
                  V cachedValue = AsMapView.this.get(key);
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
               return AsMapView.this.remove(entry.getKey(), entry.getValue());
            }
         }

         public void clear() {
            AsMapView.this.clear();
         }

         public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Entry<K, V>>() {
               Iterator<Entry<K, CompletableFuture<V>>> iterator = AsMapView.this.delegate.entrySet().iterator();
               @Nullable
               Entry<K, V> cursor;
               @Nullable
               K removalKey;

               public boolean hasNext() {
                  while(this.cursor == null && this.iterator.hasNext()) {
                     Entry<K, CompletableFuture<V>> entry = (Entry)this.iterator.next();
                     V value = Async.getIfReady((CompletableFuture<V>)entry.getValue());
                     if (value != null) {
                        this.cursor = new WriteThroughEntry<K, V>(AsMapView.this, (K)entry.getKey(), value);
                     }
                  }

                  return this.cursor != null;
               }

               public Entry<K, V> next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     K key = (K)this.cursor.getKey();
                     Entry<K, V> entry = this.cursor;
                     this.removalKey = key;
                     this.cursor = null;
                     return entry;
                  }
               }

               public void remove() {
                  Caffeine.requireState(this.removalKey != null);
                  AsMapView.this.delegate.remove(this.removalKey);
                  this.removalKey = null;
               }
            };
         }
      }

      private final class Values extends AbstractCollection<V> {
         private Values() {
         }

         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         public int size() {
            return AsMapView.this.size();
         }

         public boolean contains(Object o) {
            return AsMapView.this.containsValue(o);
         }

         public void clear() {
            AsMapView.this.clear();
         }

         public Iterator<V> iterator() {
            return new Iterator<V>() {
               Iterator<Entry<K, V>> iterator = AsMapView.this.entrySet().iterator();

               public boolean hasNext() {
                  return this.iterator.hasNext();
               }

               public V next() {
                  return (V)((Entry)this.iterator.next()).getValue();
               }

               public void remove() {
                  this.iterator.remove();
               }
            };
         }
      }
   }

   public static final class AsyncAsMapView<K, V> implements ConcurrentMap<K, CompletableFuture<V>> {
      final LocalAsyncCache<K, V> asyncCache;

      AsyncAsMapView(LocalAsyncCache<K, V> asyncCache) {
         this.asyncCache = (LocalAsyncCache)Objects.requireNonNull(asyncCache);
      }

      public boolean isEmpty() {
         return this.asyncCache.cache().isEmpty();
      }

      public int size() {
         return this.asyncCache.cache().size();
      }

      public void clear() {
         this.asyncCache.cache().clear();
      }

      public boolean containsKey(Object key) {
         return this.asyncCache.cache().containsKey(key);
      }

      public boolean containsValue(Object value) {
         return this.asyncCache.cache().containsValue(value);
      }

      @Nullable
      public CompletableFuture<V> get(Object key) {
         return (CompletableFuture<V>)this.asyncCache.cache().get(key);
      }

      public CompletableFuture<V> putIfAbsent(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = (CompletableFuture)this.asyncCache.cache().putIfAbsent(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (prior == null) {
            this.asyncCache.handleCompletion(key, value, startTime, false);
         }

         return prior;
      }

      public CompletableFuture<V> put(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = (CompletableFuture)this.asyncCache.cache().put(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.handleCompletion(key, value, startTime, false);
         return prior;
      }

      public void putAll(Map<? extends K, ? extends CompletableFuture<V>> map) {
         map.forEach(this::put);
      }

      public CompletableFuture<V> replace(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = (CompletableFuture)this.asyncCache.cache().replace(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (prior != null) {
            this.asyncCache.handleCompletion(key, value, startTime, false);
         }

         return prior;
      }

      public boolean replace(K key, CompletableFuture<V> oldValue, CompletableFuture<V> newValue) {
         boolean replaced = this.asyncCache.cache().replace(key, oldValue, newValue);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (replaced) {
            this.asyncCache.handleCompletion(key, newValue, startTime, false);
         }

         return replaced;
      }

      public CompletableFuture<V> remove(Object key) {
         return (CompletableFuture<V>)this.asyncCache.cache().remove(key);
      }

      public boolean remove(Object key, Object value) {
         return this.asyncCache.cache().remove(key, value);
      }

      @Nullable
      public CompletableFuture<V> computeIfAbsent(K key, Function<? super K, ? extends CompletableFuture<V>> mappingFunction) {
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         CompletableFuture<V> future = (CompletableFuture)this.asyncCache.cache().computeIfAbsent(key, k -> {
            result[0] = (CompletableFuture)mappingFunction.apply(k);
            return result[0];
         }, false, false);
         if (result[0] == null) {
            if (future != null && this.asyncCache.cache().isRecordingStats()) {
               future.whenComplete((r, e) -> {
                  if (r != null || e == null) {
                     this.asyncCache.cache().statsCounter().recordHits(1);
                  }

               });
            }
         } else {
            this.asyncCache.handleCompletion(key, result[0], startTime, true);
         }

         return future;
      }

      public CompletableFuture<V> computeIfPresent(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (k, oldValue) -> {
            result[0] = oldValue == null ? null : (CompletableFuture)remappingFunction.apply(k, oldValue);
            return result[0];
         }, false, false, false);
         if (result[0] != null) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public CompletableFuture<V> compute(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (k, oldValue) -> {
            result[0] = (CompletableFuture)remappingFunction.apply(k, oldValue);
            return result[0];
         }, false, false, false);
         if (result[0] != null) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public CompletableFuture<V> merge(
         K key,
         CompletableFuture<V> value,
         BiFunction<? super CompletableFuture<V>, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction
      ) {
         Objects.requireNonNull(value);
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (k, oldValue) -> {
            result[0] = oldValue == null ? value : (CompletableFuture)remappingFunction.apply(oldValue, value);
            return result[0];
         }, false, false, false);
         if (result[0] != null) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public Set<K> keySet() {
         return this.asyncCache.cache().keySet();
      }

      public Collection<CompletableFuture<V>> values() {
         return this.asyncCache.cache().values();
      }

      public Set<Entry<K, CompletableFuture<V>>> entrySet() {
         return this.asyncCache.cache().entrySet();
      }

      public boolean equals(Object o) {
         return this.asyncCache.cache().equals(o);
      }

      public int hashCode() {
         return this.asyncCache.cache().hashCode();
      }

      public String toString() {
         return this.asyncCache.cache().toString();
      }
   }

   public static final class AsyncBulkCompleter<K, V> implements BiConsumer<Map<K, V>, Throwable> {
      private final LocalCache<K, CompletableFuture<V>> cache;
      private final Map<K, CompletableFuture<V>> proxies;
      private final long startTime;

      AsyncBulkCompleter(LocalCache<K, CompletableFuture<V>> cache, Map<K, CompletableFuture<V>> proxies) {
         this.startTime = cache.statsTicker().read();
         this.proxies = proxies;
         this.cache = cache;
      }

      public void accept(@Nullable Map<K, V> result, @Nullable Throwable error) {
         long loadTime = this.cache.statsTicker().read() - this.startTime;
         if (result == null) {
            if (error == null) {
               error = new LocalAsyncCache.AsyncBulkCompleter.NullMapCompletionException();
            }

            for(Entry<K, CompletableFuture<V>> entry : this.proxies.entrySet()) {
               this.cache.remove(entry.getKey(), entry.getValue());
               ((CompletableFuture)entry.getValue()).obtrudeException(error);
            }

            this.cache.statsCounter().recordLoadFailure(loadTime);
            if (!(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
               LocalAsyncCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", error);
            }
         } else {
            this.fillProxies(result);
            this.addNewEntries(result);
            this.cache.statsCounter().recordLoadSuccess(loadTime);
         }

      }

      private void fillProxies(Map<K, V> result) {
         this.proxies.forEach((key, future) -> {
            V value = (V)result.get(key);
            future.obtrudeValue(value);
            if (value == null) {
               this.cache.remove(key, future);
            } else {
               this.cache.replace(key, future, future);
            }

         });
      }

      private void addNewEntries(Map<K, V> result) {
         if (this.proxies.size() != result.size()) {
            result.forEach((key, value) -> {
               if (!this.proxies.containsKey(key)) {
                  this.cache.put(key, CompletableFuture.completedFuture(value));
               }

            });
         }
      }

      static final class NullMapCompletionException extends CompletionException {
         private static final long serialVersionUID = 1L;

         public NullMapCompletionException() {
            super("null map", null);
         }
      }
   }

   public static final class CacheView<K, V> extends LocalAsyncCache.AbstractCacheView<K, V> {
      private static final long serialVersionUID = 1L;
      final LocalAsyncCache<K, V> asyncCache;

      CacheView(LocalAsyncCache<K, V> asyncCache) {
         this.asyncCache = (LocalAsyncCache)Objects.requireNonNull(asyncCache);
      }

      @Override
      LocalAsyncCache<K, V> asyncCache() {
         return this.asyncCache;
      }
   }
}
