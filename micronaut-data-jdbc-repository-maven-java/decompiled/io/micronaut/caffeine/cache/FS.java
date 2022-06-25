package io.micronaut.caffeine.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;

class FS<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final long KEY_OFFSET = UnsafeAccess.objectFieldOffset(FS.class, "key");
   protected static final long VALUE_OFFSET = UnsafeAccess.objectFieldOffset(FS.class, "value");
   volatile References.WeakKeyReference<K> key;
   volatile V value;

   FS() {
   }

   FS(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      this(new References.WeakKeyReference(key, keyReferenceQueue), value, valueReferenceQueue, weight, now);
   }

   FS(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      UnsafeAccess.UNSAFE.putObject(this, KEY_OFFSET, keyReference);
      UnsafeAccess.UNSAFE.putObject(this, VALUE_OFFSET, value);
   }

   @Override
   public final K getKey() {
      return (K)((Reference)UnsafeAccess.UNSAFE.getObject(this, KEY_OFFSET)).get();
   }

   @Override
   public final Object getKeyReference() {
      return UnsafeAccess.UNSAFE.getObject(this, KEY_OFFSET);
   }

   @Override
   public final V getValue() {
      return this.value;
   }

   @Override
   public final Object getValueReference() {
      return UnsafeAccess.UNSAFE.getObject(this, VALUE_OFFSET);
   }

   @Override
   public final void setValue(V value, ReferenceQueue<V> referenceQueue) {
      UnsafeAccess.UNSAFE.putObject(this, VALUE_OFFSET, value);
   }

   @Override
   public final boolean containsValue(Object value) {
      return Objects.equals(value, this.getValue());
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FS<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FS<>(keyReference, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Object newLookupKey(Object key) {
      return new References.LookupKeyReference<>(key);
   }

   @Override
   public Object newReferenceKey(K key, ReferenceQueue<K> referenceQueue) {
      return new References.WeakKeyReference(key, referenceQueue);
   }

   @Override
   public final boolean isAlive() {
      Object key = this.getKeyReference();
      return key != RETIRED_WEAK_KEY && key != DEAD_WEAK_KEY;
   }

   @Override
   public final boolean isRetired() {
      return this.getKeyReference() == RETIRED_WEAK_KEY;
   }

   @Override
   public final void retire() {
      UnsafeAccess.UNSAFE.putObject(this, KEY_OFFSET, RETIRED_WEAK_KEY);
   }

   @Override
   public final boolean isDead() {
      return this.getKeyReference() == DEAD_WEAK_KEY;
   }

   @Override
   public final void die() {
      UnsafeAccess.UNSAFE.putObject(this, VALUE_OFFSET, null);
      UnsafeAccess.UNSAFE.putObject(this, KEY_OFFSET, DEAD_WEAK_KEY);
   }
}
