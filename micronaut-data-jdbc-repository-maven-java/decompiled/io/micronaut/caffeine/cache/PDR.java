package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class PDR<K, V> extends PD<K, V> {
   protected static final long WRITE_TIME_OFFSET = UnsafeAccess.objectFieldOffset(PDR.class, "writeTime");
   volatile long writeTime;

   PDR() {
   }

   PDR(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
      UnsafeAccess.UNSAFE.putLong(this, WRITE_TIME_OFFSET, now);
   }

   PDR(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      super(keyReference, value, valueReferenceQueue, weight, now);
      UnsafeAccess.UNSAFE.putLong(this, WRITE_TIME_OFFSET, now);
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
   public final boolean casWriteTime(long expect, long update) {
      return this.writeTime == expect && UnsafeAccess.UNSAFE.compareAndSwapLong(this, WRITE_TIME_OFFSET, expect, update);
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PDR<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new PDR<>(keyReference, value, valueReferenceQueue, weight, now);
   }
}
