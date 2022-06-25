package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

final class PWMW<K, V> extends PW<K, V> {
   int queueType;
   int weight;
   int policyWeight;
   Node<K, V> previousInAccessOrder;
   Node<K, V> nextInAccessOrder;

   PWMW() {
   }

   PWMW(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
      this.weight = weight;
   }

   PWMW(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
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
      return new PWMW<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PWMW<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
