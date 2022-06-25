package com.google.protobuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class MapField<K, V> implements MutabilityOracle {
   private volatile boolean isMutable;
   private volatile MapField.StorageMode mode;
   private MapField.MutabilityAwareMap<K, V> mapData;
   private List<Message> listData;
   private final MapField.Converter<K, V> converter;

   private MapField(MapField.Converter<K, V> converter, MapField.StorageMode mode, Map<K, V> mapData) {
      this.converter = converter;
      this.isMutable = true;
      this.mode = mode;
      this.mapData = new MapField.MutabilityAwareMap<>(this, mapData);
      this.listData = null;
   }

   private MapField(MapEntry<K, V> defaultEntry, MapField.StorageMode mode, Map<K, V> mapData) {
      this(new MapField.ImmutableMessageConverter<>(defaultEntry), mode, mapData);
   }

   public static <K, V> MapField<K, V> emptyMapField(MapEntry<K, V> defaultEntry) {
      return new MapField<>(defaultEntry, MapField.StorageMode.MAP, Collections.emptyMap());
   }

   public static <K, V> MapField<K, V> newMapField(MapEntry<K, V> defaultEntry) {
      return new MapField<>(defaultEntry, MapField.StorageMode.MAP, new LinkedHashMap());
   }

   private Message convertKeyAndValueToMessage(K key, V value) {
      return this.converter.convertKeyAndValueToMessage(key, value);
   }

   private void convertMessageToKeyAndValue(Message message, Map<K, V> map) {
      this.converter.convertMessageToKeyAndValue(message, map);
   }

   private List<Message> convertMapToList(MapField.MutabilityAwareMap<K, V> mapData) {
      List<Message> listData = new ArrayList();

      for(Entry<K, V> entry : mapData.entrySet()) {
         listData.add(this.convertKeyAndValueToMessage((K)entry.getKey(), (V)entry.getValue()));
      }

      return listData;
   }

   private MapField.MutabilityAwareMap<K, V> convertListToMap(List<Message> listData) {
      Map<K, V> mapData = new LinkedHashMap();

      for(Message item : listData) {
         this.convertMessageToKeyAndValue(item, mapData);
      }

      return new MapField.MutabilityAwareMap<>(this, mapData);
   }

   public Map<K, V> getMap() {
      if (this.mode == MapField.StorageMode.LIST) {
         synchronized(this) {
            if (this.mode == MapField.StorageMode.LIST) {
               this.mapData = this.convertListToMap(this.listData);
               this.mode = MapField.StorageMode.BOTH;
            }
         }
      }

      return Collections.unmodifiableMap(this.mapData);
   }

   public Map<K, V> getMutableMap() {
      if (this.mode != MapField.StorageMode.MAP) {
         if (this.mode == MapField.StorageMode.LIST) {
            this.mapData = this.convertListToMap(this.listData);
         }

         this.listData = null;
         this.mode = MapField.StorageMode.MAP;
      }

      return this.mapData;
   }

   public void mergeFrom(MapField<K, V> other) {
      this.getMutableMap().putAll(MapFieldLite.copy(other.getMap()));
   }

   public void clear() {
      this.mapData = new MapField.MutabilityAwareMap<>(this, new LinkedHashMap());
      this.mode = MapField.StorageMode.MAP;
   }

   public boolean equals(Object object) {
      if (!(object instanceof MapField)) {
         return false;
      } else {
         MapField<K, V> other = (MapField)object;
         return MapFieldLite.equals(this.getMap(), other.getMap());
      }
   }

   public int hashCode() {
      return MapFieldLite.calculateHashCodeForMap(this.getMap());
   }

   public MapField<K, V> copy() {
      return new MapField<>(this.converter, MapField.StorageMode.MAP, MapFieldLite.copy(this.getMap()));
   }

   List<Message> getList() {
      if (this.mode == MapField.StorageMode.MAP) {
         synchronized(this) {
            if (this.mode == MapField.StorageMode.MAP) {
               this.listData = this.convertMapToList(this.mapData);
               this.mode = MapField.StorageMode.BOTH;
            }
         }
      }

      return Collections.unmodifiableList(this.listData);
   }

   List<Message> getMutableList() {
      if (this.mode != MapField.StorageMode.LIST) {
         if (this.mode == MapField.StorageMode.MAP) {
            this.listData = this.convertMapToList(this.mapData);
         }

         this.mapData = null;
         this.mode = MapField.StorageMode.LIST;
      }

      return this.listData;
   }

   Message getMapEntryMessageDefaultInstance() {
      return this.converter.getMessageDefaultInstance();
   }

   public void makeImmutable() {
      this.isMutable = false;
   }

   public boolean isMutable() {
      return this.isMutable;
   }

   @Override
   public void ensureMutable() {
      if (!this.isMutable()) {
         throw new UnsupportedOperationException();
      }
   }

   private interface Converter<K, V> {
      Message convertKeyAndValueToMessage(K var1, V var2);

      void convertMessageToKeyAndValue(Message var1, Map<K, V> var2);

      Message getMessageDefaultInstance();
   }

   private static class ImmutableMessageConverter<K, V> implements MapField.Converter<K, V> {
      private final MapEntry<K, V> defaultEntry;

      public ImmutableMessageConverter(MapEntry<K, V> defaultEntry) {
         this.defaultEntry = defaultEntry;
      }

      @Override
      public Message convertKeyAndValueToMessage(K key, V value) {
         return this.defaultEntry.newBuilderForType().setKey(key).setValue(value).buildPartial();
      }

      @Override
      public void convertMessageToKeyAndValue(Message message, Map<K, V> map) {
         MapEntry<K, V> entry = (MapEntry)message;
         map.put(entry.getKey(), entry.getValue());
      }

      @Override
      public Message getMessageDefaultInstance() {
         return this.defaultEntry;
      }
   }

   private static class MutabilityAwareMap<K, V> implements Map<K, V> {
      private final MutabilityOracle mutabilityOracle;
      private final Map<K, V> delegate;

      MutabilityAwareMap(MutabilityOracle mutabilityOracle, Map<K, V> delegate) {
         this.mutabilityOracle = mutabilityOracle;
         this.delegate = delegate;
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
         return this.delegate.containsValue(value);
      }

      public V get(Object key) {
         return (V)this.delegate.get(key);
      }

      public V put(K key, V value) {
         this.mutabilityOracle.ensureMutable();
         Internal.checkNotNull(key);
         Internal.checkNotNull(value);
         return (V)this.delegate.put(key, value);
      }

      public V remove(Object key) {
         this.mutabilityOracle.ensureMutable();
         return (V)this.delegate.remove(key);
      }

      public void putAll(Map<? extends K, ? extends V> m) {
         this.mutabilityOracle.ensureMutable();

         for(K key : m.keySet()) {
            Internal.checkNotNull(key);
            Internal.checkNotNull(m.get(key));
         }

         this.delegate.putAll(m);
      }

      public void clear() {
         this.mutabilityOracle.ensureMutable();
         this.delegate.clear();
      }

      public Set<K> keySet() {
         return new MapField.MutabilityAwareMap.MutabilityAwareSet<>(this.mutabilityOracle, this.delegate.keySet());
      }

      public Collection<V> values() {
         return new MapField.MutabilityAwareMap.MutabilityAwareCollection<>(this.mutabilityOracle, this.delegate.values());
      }

      public Set<Entry<K, V>> entrySet() {
         return new MapField.MutabilityAwareMap.MutabilityAwareSet(this.mutabilityOracle, this.delegate.entrySet());
      }

      public boolean equals(Object o) {
         return this.delegate.equals(o);
      }

      public int hashCode() {
         return this.delegate.hashCode();
      }

      public String toString() {
         return this.delegate.toString();
      }

      private static class MutabilityAwareCollection<E> implements Collection<E> {
         private final MutabilityOracle mutabilityOracle;
         private final Collection<E> delegate;

         MutabilityAwareCollection(MutabilityOracle mutabilityOracle, Collection<E> delegate) {
            this.mutabilityOracle = mutabilityOracle;
            this.delegate = delegate;
         }

         public int size() {
            return this.delegate.size();
         }

         public boolean isEmpty() {
            return this.delegate.isEmpty();
         }

         public boolean contains(Object o) {
            return this.delegate.contains(o);
         }

         public Iterator<E> iterator() {
            return new MapField.MutabilityAwareMap.MutabilityAwareIterator<>(this.mutabilityOracle, this.delegate.iterator());
         }

         public Object[] toArray() {
            return this.delegate.toArray();
         }

         public <T> T[] toArray(T[] a) {
            return (T[])this.delegate.toArray(a);
         }

         public boolean add(E e) {
            throw new UnsupportedOperationException();
         }

         public boolean remove(Object o) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.remove(o);
         }

         public boolean containsAll(Collection<?> c) {
            return this.delegate.containsAll(c);
         }

         public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
         }

         public boolean removeAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.removeAll(c);
         }

         public boolean retainAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.retainAll(c);
         }

         public void clear() {
            this.mutabilityOracle.ensureMutable();
            this.delegate.clear();
         }

         public boolean equals(Object o) {
            return this.delegate.equals(o);
         }

         public int hashCode() {
            return this.delegate.hashCode();
         }

         public String toString() {
            return this.delegate.toString();
         }
      }

      private static class MutabilityAwareIterator<E> implements Iterator<E> {
         private final MutabilityOracle mutabilityOracle;
         private final Iterator<E> delegate;

         MutabilityAwareIterator(MutabilityOracle mutabilityOracle, Iterator<E> delegate) {
            this.mutabilityOracle = mutabilityOracle;
            this.delegate = delegate;
         }

         public boolean hasNext() {
            return this.delegate.hasNext();
         }

         public E next() {
            return (E)this.delegate.next();
         }

         public void remove() {
            this.mutabilityOracle.ensureMutable();
            this.delegate.remove();
         }

         public boolean equals(Object obj) {
            return this.delegate.equals(obj);
         }

         public int hashCode() {
            return this.delegate.hashCode();
         }

         public String toString() {
            return this.delegate.toString();
         }
      }

      private static class MutabilityAwareSet<E> implements Set<E> {
         private final MutabilityOracle mutabilityOracle;
         private final Set<E> delegate;

         MutabilityAwareSet(MutabilityOracle mutabilityOracle, Set<E> delegate) {
            this.mutabilityOracle = mutabilityOracle;
            this.delegate = delegate;
         }

         public int size() {
            return this.delegate.size();
         }

         public boolean isEmpty() {
            return this.delegate.isEmpty();
         }

         public boolean contains(Object o) {
            return this.delegate.contains(o);
         }

         public Iterator<E> iterator() {
            return new MapField.MutabilityAwareMap.MutabilityAwareIterator<>(this.mutabilityOracle, this.delegate.iterator());
         }

         public Object[] toArray() {
            return this.delegate.toArray();
         }

         public <T> T[] toArray(T[] a) {
            return (T[])this.delegate.toArray(a);
         }

         public boolean add(E e) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.add(e);
         }

         public boolean remove(Object o) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.remove(o);
         }

         public boolean containsAll(Collection<?> c) {
            return this.delegate.containsAll(c);
         }

         public boolean addAll(Collection<? extends E> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.addAll(c);
         }

         public boolean retainAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.retainAll(c);
         }

         public boolean removeAll(Collection<?> c) {
            this.mutabilityOracle.ensureMutable();
            return this.delegate.removeAll(c);
         }

         public void clear() {
            this.mutabilityOracle.ensureMutable();
            this.delegate.clear();
         }

         public boolean equals(Object o) {
            return this.delegate.equals(o);
         }

         public int hashCode() {
            return this.delegate.hashCode();
         }

         public String toString() {
            return this.delegate.toString();
         }
      }
   }

   private static enum StorageMode {
      MAP,
      LIST,
      BOTH;
   }
}
