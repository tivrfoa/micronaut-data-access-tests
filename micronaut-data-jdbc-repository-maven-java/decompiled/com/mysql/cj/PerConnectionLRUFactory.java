package com.mysql.cj;

import com.mysql.cj.util.LRUCache;
import java.util.Set;

public class PerConnectionLRUFactory implements CacheAdapterFactory<String, QueryInfo> {
   @Override
   public CacheAdapter<String, QueryInfo> getInstance(Object syncMutex, String url, int cacheMaxSize, int maxKeySize) {
      return new PerConnectionLRUFactory.PerConnectionLRU(syncMutex, cacheMaxSize, maxKeySize);
   }

   class PerConnectionLRU implements CacheAdapter<String, QueryInfo> {
      private final int cacheSqlLimit;
      private final LRUCache<String, QueryInfo> cache;
      private final Object syncMutex;

      protected PerConnectionLRU(Object syncMutex, int cacheMaxSize, int maxKeySize) {
         this.cacheSqlLimit = maxKeySize;
         this.cache = new LRUCache<>(cacheMaxSize);
         this.syncMutex = syncMutex;
      }

      public QueryInfo get(String key) {
         if (key != null && key.length() <= this.cacheSqlLimit) {
            synchronized(this.syncMutex) {
               return (QueryInfo)this.cache.get(key);
            }
         } else {
            return null;
         }
      }

      public void put(String key, QueryInfo value) {
         if (key != null && key.length() <= this.cacheSqlLimit) {
            synchronized(this.syncMutex) {
               this.cache.put(key, value);
            }
         }
      }

      public void invalidate(String key) {
         synchronized(this.syncMutex) {
            this.cache.remove(key);
         }
      }

      @Override
      public void invalidateAll(Set<String> keys) {
         synchronized(this.syncMutex) {
            for(String key : keys) {
               this.cache.remove(key);
            }

         }
      }

      @Override
      public void invalidateAll() {
         synchronized(this.syncMutex) {
            this.cache.clear();
         }
      }
   }
}
