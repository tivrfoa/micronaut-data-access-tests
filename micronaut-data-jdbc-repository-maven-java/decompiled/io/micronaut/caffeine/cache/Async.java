package io.micronaut.caffeine.cache;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.Nullable;

final class Async {
   static final long ASYNC_EXPIRY = 6917529027641081854L;

   private Async() {
   }

   static boolean isReady(@Nullable CompletableFuture<?> future) {
      return future != null && future.isDone() && !future.isCompletedExceptionally() && future.join() != null;
   }

   @Nullable
   static <V> V getIfReady(@Nullable CompletableFuture<V> future) {
      return (V)(isReady(future) ? future.join() : null);
   }

   @Nullable
   static <V> V getWhenSuccessful(@Nullable CompletableFuture<V> future) {
      try {
         return (V)(future == null ? null : future.join());
      } catch (CompletionException | CancellationException var2) {
         return null;
      }
   }

   static final class AsyncExpiry<K, V> implements Expiry<K, CompletableFuture<V>>, Serializable {
      private static final long serialVersionUID = 1L;
      final Expiry<K, V> delegate;

      AsyncExpiry(Expiry<K, V> delegate) {
         this.delegate = (Expiry)Objects.requireNonNull(delegate);
      }

      public long expireAfterCreate(K key, CompletableFuture<V> future, long currentTime) {
         if (Async.isReady(future)) {
            long duration = this.delegate.expireAfterCreate(key, (V)future.join(), currentTime);
            return Math.min(duration, 4611686018427387903L);
         } else {
            return 6917529027641081854L;
         }
      }

      public long expireAfterUpdate(K key, CompletableFuture<V> future, long currentTime, long currentDuration) {
         if (Async.isReady(future)) {
            long duration = currentDuration > 4611686018427387903L
               ? this.delegate.expireAfterCreate(key, (V)future.join(), currentTime)
               : this.delegate.expireAfterUpdate(key, (V)future.join(), currentTime, currentDuration);
            return Math.min(duration, 4611686018427387903L);
         } else {
            return 6917529027641081854L;
         }
      }

      public long expireAfterRead(K key, CompletableFuture<V> future, long currentTime, long currentDuration) {
         if (Async.isReady(future)) {
            long duration = this.delegate.expireAfterRead(key, (V)future.join(), currentTime, currentDuration);
            return Math.min(duration, 4611686018427387903L);
         } else {
            return 6917529027641081854L;
         }
      }

      Object writeReplace() {
         return this.delegate;
      }
   }

   static final class AsyncRemovalListener<K, V> implements RemovalListener<K, CompletableFuture<V>>, Serializable {
      private static final long serialVersionUID = 1L;
      final RemovalListener<K, V> delegate;
      final Executor executor;

      AsyncRemovalListener(RemovalListener<K, V> delegate, Executor executor) {
         this.delegate = (RemovalListener)Objects.requireNonNull(delegate);
         this.executor = (Executor)Objects.requireNonNull(executor);
      }

      public void onRemoval(@Nullable K key, @Nullable CompletableFuture<V> future, RemovalCause cause) {
         if (future != null) {
            future.thenAcceptAsync(value -> {
               if (value != null) {
                  this.delegate.onRemoval(key, (V)value, cause);
               }

            }, this.executor);
         }

      }

      Object writeReplace() {
         return this.delegate;
      }
   }

   static final class AsyncWeigher<K, V> implements Weigher<K, CompletableFuture<V>>, Serializable {
      private static final long serialVersionUID = 1L;
      final Weigher<K, V> delegate;

      AsyncWeigher(Weigher<K, V> delegate) {
         this.delegate = (Weigher)Objects.requireNonNull(delegate);
      }

      public int weigh(K key, CompletableFuture<V> future) {
         return Async.isReady(future) ? this.delegate.weigh(key, (V)future.join()) : 0;
      }

      Object writeReplace() {
         return this.delegate;
      }
   }
}
