package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

final class PSMS<K, V> extends PS<K, V> {
   int queueType;
   Node<K, V> previousInAccessOrder;
   Node<K, V> nextInAccessOrder;

   PSMS() {
   }

   PSMS(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   PSMS(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
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
   public Node<K, V> getPreviousInAccessOrder() {
      return this.previousInAccessOrder;
   }

   @Override
   public void setPreviousInAccessOrder(Node<K, V> previousInAccessOrder) {
      this.previousInAccessOrder = previousInAccessOrder;
   }

   @Override
   public Node<K, V> getNextInAccessOrder() {
      return this.nextInAccessOrder;
   }

   @Override
   public void setNextInAccessOrder(Node<K, V> nextInAccessOrder) {
      this.nextInAccessOrder = nextInAccessOrder;
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PSMS<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PSMS<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
