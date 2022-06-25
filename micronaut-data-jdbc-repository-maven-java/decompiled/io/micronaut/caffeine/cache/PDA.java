package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class PDA<K, V> extends PD<K, V> {
   protected static final long ACCESS_TIME_OFFSET = UnsafeAccess.objectFieldOffset(PDA.class, "accessTime");
   volatile long accessTime;
   Node<K, V> previousInAccessOrder;
   Node<K, V> nextInAccessOrder;

   PDA() {
   }

   PDA(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
      UnsafeAccess.UNSAFE.putLong(this, ACCESS_TIME_OFFSET, now);
   }

   PDA(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(keyReference, value, valueReferenceQueue, weight, now);
      UnsafeAccess.UNSAFE.putLong(this, ACCESS_TIME_OFFSET, now);
   }

   @Override
   public Node<K, V> getPreviousInVariableOrder() {
      return this.previousInAccessOrder;
   }

   @Override
   public void setPreviousInVariableOrder(Node<K, V> previousInAccessOrder) {
      this.previousInAccessOrder = previousInAccessOrder;
   }

   @Override
   public Node<K, V> getNextInVariableOrder() {
      return this.nextInAccessOrder;
   }

   @Override
   public void setNextInVariableOrder(Node<K, V> nextInAccessOrder) {
      this.nextInAccessOrder = nextInAccessOrder;
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
   public final long getAccessTime() {
      return UnsafeAccess.UNSAFE.getLong(this, ACCESS_TIME_OFFSET);
   }

   @Override
   public final void setAccessTime(long accessTime) {
      UnsafeAccess.UNSAFE.putLong(this, ACCESS_TIME_OFFSET, accessTime);
   }

   @Override
   public final Node<K, V> getPreviousInAccessOrder() {
      return this.previousInAccessOrder;
   }

   @Override
   public final void setPreviousInAccessOrder(Node<K, V> previousInAccessOrder) {
      this.previousInAccessOrder = previousInAccessOrder;
   }

   @Override
   public final Node<K, V> getNextInAccessOrder() {
      return this.nextInAccessOrder;
   }

   @Override
   public final void setNextInAccessOrder(Node<K, V> nextInAccessOrder) {
      this.nextInAccessOrder = nextInAccessOrder;
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PDA<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PDA<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
