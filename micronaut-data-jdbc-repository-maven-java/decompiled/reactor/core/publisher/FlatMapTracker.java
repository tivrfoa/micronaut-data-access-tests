package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

abstract class FlatMapTracker<T> {
   volatile T[] array = this.empty();
   int[] free;
   long producerIndex;
   long consumerIndex;
   volatile int size;
   static final AtomicIntegerFieldUpdater<FlatMapTracker> SIZE = AtomicIntegerFieldUpdater.newUpdater(FlatMapTracker.class, "size");
   static final int[] FREE_EMPTY = new int[0];

   FlatMapTracker() {
      this.free = FREE_EMPTY;
   }

   abstract T[] empty();

   abstract T[] terminated();

   abstract T[] newArray(int var1);

   abstract void unsubscribeEntry(T var1);

   abstract void setIndex(T var1, int var2);

   final void unsubscribe() {
      T[] t = this.terminated();
      T[] a;
      synchronized(this) {
         a = this.array;
         if (a == t) {
            return;
         }

         SIZE.lazySet(this, 0);
         this.free = null;
         this.array = t;
      }

      for(T e : a) {
         if (e != null) {
            this.unsubscribeEntry(e);
         }
      }

   }

   final T[] get() {
      return this.array;
   }

   final boolean add(T entry) {
      T[] a = this.array;
      if (a == this.terminated()) {
         return false;
      } else {
         synchronized(this) {
            a = this.array;
            if (a == this.terminated()) {
               return false;
            } else {
               int idx = this.pollFree();
               if (idx < 0) {
                  int n = a.length;
                  T[] b = (T[])(n != 0 ? this.newArray(n << 1) : this.newArray(4));
                  System.arraycopy(a, 0, b, 0, n);
                  this.array = b;
                  a = b;
                  int m = b.length;
                  int[] u = new int[m];
                  int i = n + 1;

                  while(i < m) {
                     u[i] = i++;
                  }

                  this.free = u;
                  this.consumerIndex = (long)(n + 1);
                  this.producerIndex = (long)m;
                  idx = n;
               }

               this.setIndex(entry, idx);
               SIZE.lazySet(this, this.size);
               a[idx] = entry;
               SIZE.lazySet(this, this.size + 1);
               return true;
            }
         }
      }
   }

   final void remove(int index) {
      synchronized(this) {
         T[] a = this.array;
         if (a != this.terminated()) {
            a[index] = null;
            this.offerFree(index);
            SIZE.lazySet(this, this.size - 1);
         }

      }
   }

   int pollFree() {
      int[] a = this.free;
      int m = a.length - 1;
      long ci = this.consumerIndex;
      if (this.producerIndex == ci) {
         return -1;
      } else {
         int offset = (int)ci & m;
         this.consumerIndex = ci + 1L;
         return a[offset];
      }
   }

   void offerFree(int index) {
      int[] a = this.free;
      int m = a.length - 1;
      long pi = this.producerIndex;
      int offset = (int)pi & m;
      a[offset] = index;
      this.producerIndex = pi + 1L;
   }

   final boolean isEmpty() {
      return this.size == 0;
   }
}
