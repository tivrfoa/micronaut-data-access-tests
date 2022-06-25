package io.micronaut.core.util.clhm;

import io.micronaut.core.util.ArgumentUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Weighers {
   private Weighers() {
      throw new AssertionError();
   }

   public static <K, V> EntryWeigher<K, V> asEntryWeigher(final Weigher<? super V> weigher) {
      return (EntryWeigher<K, V>)(weigher == singleton() ? entrySingleton() : new Weighers.EntryWeigherView<>(weigher));
   }

   public static <K, V> EntryWeigher<K, V> entrySingleton() {
      return Weighers.SingletonEntryWeigher.INSTANCE;
   }

   public static <V> Weigher<V> singleton() {
      return Weighers.SingletonWeigher.INSTANCE;
   }

   public static Weigher<byte[]> byteArray() {
      return Weighers.ByteArrayWeigher.INSTANCE;
   }

   public static <E> Weigher<? super Iterable<E>> iterable() {
      return Weighers.IterableWeigher.INSTANCE;
   }

   public static <E> Weigher<? super Collection<E>> collection() {
      return Weighers.CollectionWeigher.INSTANCE;
   }

   public static <E> Weigher<? super List<E>> list() {
      return Weighers.ListWeigher.INSTANCE;
   }

   public static <E> Weigher<? super Set<E>> set() {
      return Weighers.SetWeigher.INSTANCE;
   }

   public static <A, B> Weigher<? super Map<A, B>> map() {
      return Weighers.MapWeigher.INSTANCE;
   }

   private static enum ByteArrayWeigher implements Weigher<byte[]> {
      INSTANCE;

      public int weightOf(byte[] value) {
         return value.length;
      }
   }

   private static enum CollectionWeigher implements Weigher<Collection<?>> {
      INSTANCE;

      public int weightOf(Collection<?> values) {
         return values.size();
      }
   }

   private static final class EntryWeigherView<K, V> implements EntryWeigher<K, V>, Serializable {
      static final long serialVersionUID = 1L;
      final Weigher<? super V> weigher;

      EntryWeigherView(Weigher<? super V> weigher) {
         ArgumentUtils.requireNonNull("weigher", weigher);
         this.weigher = weigher;
      }

      @Override
      public int weightOf(K key, V value) {
         return this.weigher.weightOf(value);
      }
   }

   private static enum IterableWeigher implements Weigher<Iterable<?>> {
      INSTANCE;

      public int weightOf(Iterable<?> values) {
         if (values instanceof Collection) {
            return ((Collection)values).size();
         } else {
            int size = 0;

            for(Iterator<?> i = values.iterator(); i.hasNext(); ++size) {
               i.next();
            }

            return size;
         }
      }
   }

   private static enum ListWeigher implements Weigher<List<?>> {
      INSTANCE;

      public int weightOf(List<?> values) {
         return values.size();
      }
   }

   private static enum MapWeigher implements Weigher<Map<?, ?>> {
      INSTANCE;

      public int weightOf(Map<?, ?> values) {
         return values.size();
      }
   }

   private static enum SetWeigher implements Weigher<Set<?>> {
      INSTANCE;

      public int weightOf(Set<?> values) {
         return values.size();
      }
   }

   private static enum SingletonEntryWeigher implements EntryWeigher<Object, Object> {
      INSTANCE;

      @Override
      public int weightOf(Object key, Object value) {
         return 1;
      }
   }

   private static enum SingletonWeigher implements Weigher<Object> {
      INSTANCE;

      @Override
      public int weightOf(Object value) {
         return 1;
      }
   }
}
