package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

final class FWAWRMS<K, V> extends FWAWR<K, V> {
   int queueType;

   FWAWRMS() {
   }

   FWAWRMS(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   FWAWRMS(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(keyReference, value, valueReferenceQueue, weight, now);
   }

   @Override
   public int getQueueType() {
      return this.queueType;
   }

   @Override
   public void setQueueType(int queueType) {
      this.queueType = queueType;
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FWAWRMS<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FWAWRMS<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
