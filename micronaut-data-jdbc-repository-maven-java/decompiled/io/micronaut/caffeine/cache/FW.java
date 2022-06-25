package io.micronaut.caffeine.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

class FW<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final long VALUE_OFFSET = UnsafeAccess.objectFieldOffset(FW.class, "value");
   volatile References.WeakValueReference<V> value;

   FW() {
   }

   FW(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      this(new References.WeakKeyReference(key, keyReferenceQueue), value, valueReferenceQueue, weight, now);
   }

   FW(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      UnsafeAccess.UNSAFE.putObject(this, VALUE_OFFSET, new References.WeakValueReference(keyReference, value, valueReferenceQueue));
   }

   @Override
   public final Object getKeyReference() {
      References.WeakValueReference<V> valueRef = (References.WeakValueReference)this.getValueReference();
      return valueRef.getKeyReference();
   }

   @Override
   public final K getKey() {
      References.WeakValueReference<V> valueRef = (References.WeakValueReference)this.getValueReference();
      References.WeakKeyReference<K> keyRef = (References.WeakKeyReference)valueRef.getKeyReference();
      return keyRef.get();
   }

   @Override
   public final V getValue() {
      Reference<V> ref;
      V referent;
      do {
         ref = (Reference)UnsafeAccess.UNSAFE.getObject(this, VALUE_OFFSET);
         referent = (V)ref.get();
      } while(referent == null && ref != this.value);

      return referent;
   }

   @Override
   public final Object getValueReference() {
      return UnsafeAccess.UNSAFE.getObject(this, VALUE_OFFSET);
   }

   @Override
   public final void setValue(V value, ReferenceQueue<V> referenceQueue) {
      Reference<V> ref = (Reference)UnsafeAccess.UNSAFE.getObject(this, VALUE_OFFSET);
      UnsafeAccess.UNSAFE.putOrderedObject(this, VALUE_OFFSET, new References.WeakValueReference(this.getKeyReference(), value, referenceQueue));
      ref.clear();
   }

   @Override
   public final boolean containsValue(Object value) {
      return this.getValue() == value;
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FW<>(key, keyReferenceQueue, value, valueReferenceQueue, weight, now);
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new FW<>(keyReference, value, valueReferenceQueue, weight, now);
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
   public boolean weakValues() {
      return true;
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
      References.WeakValueReference<V> valueRef = (References.WeakValueReference)this.getValueReference();
      References.WeakKeyReference<K> keyRef = (References.WeakKeyReference)valueRef.getKeyReference();
      keyRef.clear();
      valueRef.setKeyReference(RETIRED_WEAK_KEY);
      valueRef.clear();
   }

   @Override
   public final boolean isDead() {
      return this.getKeyReference() == DEAD_WEAK_KEY;
   }

   @Override
   public final void die() {
      References.WeakValueReference<V> valueRef = (References.WeakValueReference)this.getValueReference();
      References.WeakKeyReference<K> keyRef = (References.WeakKeyReference)valueRef.getKeyReference();
      keyRef.clear();
      valueRef.setKeyReference(DEAD_WEAK_KEY);
      valueRef.clear();
   }
}
