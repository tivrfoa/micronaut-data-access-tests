package io.netty.handler.codec.serialization;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

abstract class ReferenceMap<K, V> implements Map<K, V> {
   private final Map<K, Reference<V>> delegate;

   protected ReferenceMap(Map<K, Reference<V>> delegate) {
      this.delegate = delegate;
   }

   abstract Reference<V> fold(V var1);

   private V unfold(Reference<V> ref) {
      return (V)(ref == null ? null : ref.get());
   }

   public int size() {
      return this.delegate.size();
   }

   public boolean isEmpty() {
      return this.delegate.isEmpty();
   }

   public boolean containsKey(Object key) {
      return this.delegate.containsKey(key);
   }

   public boolean containsValue(Object value) {
      throw new UnsupportedOperationException();
   }

   public V get(Object key) {
      return this.unfold((Reference<V>)this.delegate.get(key));
   }

   public V put(K key, V value) {
      return this.unfold((Reference<V>)this.delegate.put(key, this.fold(value)));
   }

   public V remove(Object key) {
      return this.unfold((Reference<V>)this.delegate.remove(key));
   }

   public void putAll(Map<? extends K, ? extends V> m) {
      for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
         this.delegate.put(entry.getKey(), this.fold((V)entry.getValue()));
      }

   }

   public void clear() {
      this.delegate.clear();
   }

   public Set<K> keySet() {
      return this.delegate.keySet();
   }

   public Collection<V> values() {
      throw new UnsupportedOperationException();
   }

   public Set<Entry<K, V>> entrySet() {
      throw new UnsupportedOperationException();
   }
}
