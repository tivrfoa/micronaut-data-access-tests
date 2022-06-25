package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class PSAW<K, V> extends PSA<K, V> {
   protected static final long WRITE_TIME_OFFSET = UnsafeAccess.objectFieldOffset(PSAW.class, "writeTime");
   volatile long writeTime;
   Node<K, V> previousInWriteOrder;
   Node<K, V> nextInWriteOrder;

   PSAW() {
   }

   PSAW(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
      UnsafeAccess.UNSAFE.putLong(this, WRITE_TIME_OFFSET, now);
   }

   PSAW(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(keyReference, value, valueReferenceQueue, weight, now);
      UnsafeAccess.UNSAFE.putLong(this, WRITE_TIME_OFFSET, now);
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
      return UnsafeAccess.UNSAFE.getLong(this, WRITE_TIME_OFFSET);
   }

   @Override
   public void setVariableTime(long writeTime) {
      UnsafeAccess.UNSAFE.putLong(this, WRITE_TIME_OFFSET, writeTime);
   }

   @Override
   public boolean casVariableTime(long expect, long update) {
      return this.writeTime == expect && UnsafeAccess.UNSAFE.compareAndSwapLong(this, WRITE_TIME_OFFSET, expect, update);
   }

   @Override
   public final long getWriteTime() {
      return UnsafeAccess.UNSAFE.getLong(this, WRITE_TIME_OFFSET);
   }

   @Override
   public final void setWriteTime(long writeTime) {
      UnsafeAccess.UNSAFE.putLong(this, WRITE_TIME_OFFSET, writeTime);
   }

   @Override
   public final Node<K, V> getPreviousInWriteOrder() {
      return this.previousInWriteOrder;
   }

   @Override
   public final void setPreviousInWriteOrder(Node<K, V> previousInWriteOrder) {
      this.previousInWriteOrder = previousInWriteOrder;
   }

   @Override
   public final Node<K, V> getNextInWriteOrder() {
      return this.nextInWriteOrder;
   }

   @Override
   public final void setNextInWriteOrder(Node<K, V> nextInWriteOrder) {
      this.nextInWriteOrder = nextInWriteOrder;
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PSAW<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PSAW<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
