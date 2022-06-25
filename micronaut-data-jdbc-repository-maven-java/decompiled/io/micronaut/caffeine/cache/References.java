package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class References {
   private References() {
   }

   interface InternalReference<E> {
      @Nullable
      E get();

      @NonNull
      Object getKeyReference();

      default boolean referenceEquals(@Nullable Object object) {
         if (object == this) {
            return true;
         } else if (object instanceof References.InternalReference) {
            References.InternalReference<?> referent = (References.InternalReference)object;
            return this.get() == referent.get();
         } else {
            return false;
         }
      }
   }

   static final class LookupKeyReference<E> implements References.InternalReference<E> {
      private final int hashCode;
      private final E e;

      public LookupKeyReference(@NonNull E e) {
         this.hashCode = System.identityHashCode(e);
         this.e = (E)Objects.requireNonNull(e);
      }

      @Override
      public E get() {
         return this.e;
      }

      @Override
      public Object getKeyReference() {
         return this;
      }

      public boolean equals(Object object) {
         return this.referenceEquals(object);
      }

      public int hashCode() {
         return this.hashCode;
      }
   }

   static final class SoftValueReference<V> extends SoftReference<V> implements References.InternalReference<V> {
      private Object keyReference;

      public SoftValueReference(@NonNull Object keyReference, @Nullable V value, @Nullable ReferenceQueue<V> queue) {
         super(value, queue);
         this.keyReference = keyReference;
      }

      @Override
      public Object getKeyReference() {
         return this.keyReference;
      }

      public void setKeyReference(Object keyReference) {
         this.keyReference = keyReference;
      }

      public boolean equals(Object object) {
         return this.referenceEquals(object);
      }

      public int hashCode() {
         return super.hashCode();
      }
   }

   static class WeakKeyReference<K> extends WeakReference<K> implements References.InternalReference<K> {
      private final int hashCode;

      public WeakKeyReference(@Nullable K key, @Nullable ReferenceQueue<K> queue) {
         super(key, queue);
         this.hashCode = System.identityHashCode(key);
      }

      @Override
      public Object getKeyReference() {
         return this;
      }

      public boolean equals(Object object) {
         return this.referenceEquals(object);
      }

      public int hashCode() {
         return this.hashCode;
      }
   }

   static final class WeakValueReference<V> extends WeakReference<V> implements References.InternalReference<V> {
      private Object keyReference;

      public WeakValueReference(@NonNull Object keyReference, @Nullable V value, @Nullable ReferenceQueue<V> queue) {
         super(value, queue);
         this.keyReference = keyReference;
      }

      @Override
      public Object getKeyReference() {
         return this.keyReference;
      }

      public void setKeyReference(Object keyReference) {
         this.keyReference = keyReference;
      }

      public boolean equals(Object object) {
         return this.referenceEquals(object);
      }

      public int hashCode() {
         return super.hashCode();
      }
   }
}
