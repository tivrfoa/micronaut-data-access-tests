package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

interface NodeFactory<K, V> {
   Object RETIRED_STRONG_KEY = new Object();
   Object DEAD_STRONG_KEY = new Object();
   References.WeakKeyReference<Object> RETIRED_WEAK_KEY = new References.WeakKeyReference<>(null, null);
   References.WeakKeyReference<Object> DEAD_WEAK_KEY = new References.WeakKeyReference<>(null, null);

   Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6);

   Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5);

   default Object newReferenceKey(K key, ReferenceQueue<K> referenceQueue) {
      return key;
   }

   default Object newLookupKey(Object key) {
      return key;
   }

   static <K, V> NodeFactory<K, V> newFactory(Caffeine<K, V> builder, boolean isAsync) {
      StringBuilder sb = new StringBuilder("io.micronaut.caffeine.cache.");
      if (builder.isStrongKeys()) {
         sb.append('P');
      } else {
         sb.append('F');
      }

      if (builder.isStrongValues()) {
         sb.append('S');
      } else if (builder.isWeakValues()) {
         sb.append('W');
      } else {
         sb.append('D');
      }

      if (builder.expiresVariable()) {
         if (builder.refreshAfterWrite()) {
            sb.append('A');
            if (builder.evicts()) {
               sb.append('W');
            }
         } else {
            sb.append('W');
         }
      } else {
         if (builder.expiresAfterAccess()) {
            sb.append('A');
         }

         if (builder.expiresAfterWrite()) {
            sb.append('W');
         }
      }

      if (builder.refreshAfterWrite()) {
         sb.append('R');
      }

      if (builder.evicts()) {
         sb.append('M');
         if (!isAsync && (!builder.isWeighted() || builder.weigher == Weigher.singletonWeigher())) {
            sb.append('S');
         } else {
            sb.append('W');
         }
      }

      try {
         Class<?> clazz = Class.forName(sb.toString());
         return (NodeFactory<K, V>)clazz.getDeclaredConstructor().newInstance();
      } catch (ReflectiveOperationException var5) {
         throw new IllegalStateException(sb.toString(), var5);
      }
   }

   default boolean weakValues() {
      return false;
   }

   default boolean softValues() {
      return false;
   }
}
