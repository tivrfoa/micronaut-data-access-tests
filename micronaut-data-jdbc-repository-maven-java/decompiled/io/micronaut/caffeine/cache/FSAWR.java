package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class FSAWR<K, V> extends FSAW<K, V> {
   FSAWR() {
   }

   FSAWR(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   FSAWR(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(keyReference, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> getPreviousInVariableOrder() {
      return this.previousInWriteOrder;
   }

   @Override
   public void setPreviousInVariableOrder(Node<K, V> previousInWriteOrder) {
      this.previousInWriteOrder = previousInWriteOrder;
   }

   @Override
   public Node<K, V> getNextInVariableOrder() {
      return this.nextInWriteOrder;
   }

   @Override
   public void setNextInVariableOrder(Node<K, V> nextInWriteOrder) {
      this.nextInWriteOrder = nextInWriteOrder;
   }

   @Override
   public long getVariableTime() {
      return UnsafeAccess.UNSAFE.getLong(this, ACCESS_TIME_OFFSET);
   }

   @Override
   public void setVariableTime(long accessTime) {
      UnsafeAccess.UNSAFE.putLong(this, ACCESS_TIME_OFFSET, accessTime);
   }

   @Override
   public boolean casVariableTime(long expect, long update) {
      return this.accessTime == expect && UnsafeAccess.UNSAFE.compareAndSwapLong(this, ACCESS_TIME_OFFSET, expect, update);
   }

   @Override
   public final boolean casWriteTime(long expect, long update) {
      return this.writeTime == expect && UnsafeAccess.UNSAFE.compareAndSwapLong(this, WRITE_TIME_OFFSET, expect, update);
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FSAWR<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FSAWR<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
