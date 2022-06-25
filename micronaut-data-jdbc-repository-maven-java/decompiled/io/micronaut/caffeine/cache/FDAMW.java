package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

final class FDAMW<K, V> extends FDA<K, V> {
   int queueType;
   int weight;
   int policyWeight;

   FDAMW() {
   }

   FDAMW(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
      this.weight = weight;
   }

   FDAMW(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(keyReference, value, valueReferenceQueue, weight, now);
      this.weight = weight;
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
   public int getWeight() {
      return this.weight;
   }

   @Override
   public void setWeight(int weight) {
      this.weight = weight;
   }

   @Override
   public int getPolicyWeight() {
      return this.policyWeight;
   }

   @Override
   public void setPolicyWeight(int policyWeight) {
      this.policyWeight = policyWeight;
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FDAMW<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FDAMW<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
