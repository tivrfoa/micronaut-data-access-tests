package io.micronaut.core.annotation;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

@Internal
final class ImmutableSortedStringsArrayMap<V> implements Map<String, V> {
   private final int[] index;
   private final String[] keys;
   private final Object[] values;

   ImmutableSortedStringsArrayMap(String[] keys, Object[] values) {
      this.index = computeIndex(keys);
      this.keys = keys;
      this.values = values;
   }

   private static int[] computeIndex(String[] keys) {
      int len = keys.length;
      int[] filter = new int[8 * len];
      Arrays.fill(filter, -1);

      int reduced;
      for(int i = 0; i < keys.length; filter[reduced] = i++) {
         String key = keys[i];
         reduced = reduceHashCode(key.hashCode(), len);
      }

      return filter;
   }

   private static int reduceHashCode(int hashCode, int mod) {
      return (hashCode & 16777215) % mod;
   }

   private int findKeyIndex(Object key) {
      if (!(key instanceof Comparable)) {
         return -1;
      } else {
         int v = this.index[reduceHashCode(key.hashCode(), this.keys.length)];
         if (v < 0) {
            return -1;
         } else {
            String k = this.keys[v];
            return k.equals(key) ? v : Arrays.binarySearch(this.keys, key);
         }
      }
   }

   public int size() {
      return this.keys.length;
   }

   public boolean isEmpty() {
      return this.keys.length == 0;
   }

   public boolean containsKey(Object key) {
      return this.findKeyIndex(key) > -1;
   }

   public boolean containsValue(Object value) {
      for(int i = 0; i < this.values.length; ++i) {
         Object tableValue = this.values[i];
         if (tableValue.equals(value)) {
            return true;
         }
      }

      return false;
   }

   public V get(Object key) {
      Objects.requireNonNull(key);
      int keyIndex = this.findKeyIndex(key);
      return (V)(keyIndex < 0 ? null : this.values[keyIndex]);
   }

   @Nullable
   public V put(String key, V value) {
      throw new UnsupportedOperationException();
   }

   public V remove(Object key) {
      throw new UnsupportedOperationException();
   }

   public void putAll(Map<? extends String, ? extends V> m) {
      throw new UnsupportedOperationException();
   }

   public void clear() {
      throw new UnsupportedOperationException();
   }

   @NonNull
   public Set<String> keySet() {
      return new HashSet(Arrays.asList(this.keys));
   }

   @NonNull
   public Collection<V> values() {
      return new AbstractCollection<V>() {
         public Iterator<V> iterator() {
            return new Iterator<V>() {
               private int index = 0;

               public boolean hasNext() {
                  return this.index < ImmutableSortedStringsArrayMap.this.values.length;
               }

               public V next() {
                  if (this.hasNext()) {
                     V v = (V)ImmutableSortedStringsArrayMap.this.values[this.index];
                     ++this.index;
                     return v;
                  } else {
                     throw new NoSuchElementException();
                  }
               }

               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }

         public int size() {
            return ImmutableSortedStringsArrayMap.this.size();
         }

         public boolean isEmpty() {
            return ImmutableSortedStringsArrayMap.this.isEmpty();
         }

         public void clear() {
            ImmutableSortedStringsArrayMap.this.clear();
         }

         public boolean contains(Object v) {
            return ImmutableSortedStringsArrayMap.this.containsValue(v);
         }
      };
   }

   public void forEach(BiConsumer<? super String, ? super V> action) {
      for(int i = 0; i < this.keys.length; ++i) {
         action.accept(this.keys[i], this.values[i]);
      }

   }

   @NonNull
   public Set<Entry<String, V>> entrySet() {
      Set<Entry<String, V>> set = new HashSet();

      for(int i = 0; i < this.keys.length; ++i) {
         set.add(new SimpleEntry(this.keys[i], this.values[i]));
      }

      return set;
   }
}
