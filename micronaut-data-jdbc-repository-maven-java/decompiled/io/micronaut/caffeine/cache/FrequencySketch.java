package io.micronaut.caffeine.cache;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

final class FrequencySketch<E> {
   static final long[] SEED = new long[]{-4348849565147123417L, -5435081209227447693L, -7286425919675154353L, -3750763034362895579L};
   static final long RESET_MASK = 8608480567731124087L;
   static final long ONE_MASK = 1229782938247303441L;
   int sampleSize;
   int tableMask;
   long[] table;
   int size;

   public FrequencySketch() {
   }

   public void ensureCapacity(@NonNegative long maximumSize) {
      Caffeine.requireArgument(maximumSize >= 0L);
      int maximum = (int)Math.min(maximumSize, 1073741823L);
      if (this.table == null || this.table.length < maximum) {
         this.table = new long[maximum == 0 ? 1 : Caffeine.ceilingPowerOfTwo(maximum)];
         this.tableMask = Math.max(0, this.table.length - 1);
         this.sampleSize = maximumSize == 0L ? 10 : 10 * maximum;
         if (this.sampleSize <= 0) {
            this.sampleSize = Integer.MAX_VALUE;
         }

         this.size = 0;
      }
   }

   public boolean isNotInitialized() {
      return this.table == null;
   }

   @NonNegative
   public int frequency(@NonNull E e) {
      if (this.isNotInitialized()) {
         return 0;
      } else {
         int hash = this.spread(e.hashCode());
         int start = (hash & 3) << 2;
         int frequency = Integer.MAX_VALUE;

         for(int i = 0; i < 4; ++i) {
            int index = this.indexOf(hash, i);
            int count = (int)(this.table[index] >>> (start + i << 2) & 15L);
            frequency = Math.min(frequency, count);
         }

         return frequency;
      }
   }

   public void increment(@NonNull E e) {
      if (!this.isNotInitialized()) {
         int hash = this.spread(e.hashCode());
         int start = (hash & 3) << 2;
         int index0 = this.indexOf(hash, 0);
         int index1 = this.indexOf(hash, 1);
         int index2 = this.indexOf(hash, 2);
         int index3 = this.indexOf(hash, 3);
         boolean added = this.incrementAt(index0, start);
         added |= this.incrementAt(index1, start + 1);
         added |= this.incrementAt(index2, start + 2);
         added |= this.incrementAt(index3, start + 3);
         if (added && ++this.size == this.sampleSize) {
            this.reset();
         }

      }
   }

   boolean incrementAt(int i, int j) {
      int offset = j << 2;
      long mask = 15L << offset;
      if ((this.table[i] & mask) != mask) {
         this.table[i] += 1L << offset;
         return true;
      } else {
         return false;
      }
   }

   void reset() {
      int count = 0;

      for(int i = 0; i < this.table.length; ++i) {
         count += Long.bitCount(this.table[i] & 1229782938247303441L);
         this.table[i] = this.table[i] >>> 1 & 8608480567731124087L;
      }

      this.size = (this.size >>> 1) - (count >>> 2);
   }

   int indexOf(int item, int i) {
      long hash = ((long)item + SEED[i]) * SEED[i];
      hash += hash >>> 32;
      return (int)hash & this.tableMask;
   }

   int spread(int x) {
      x = (x >>> 16 ^ x) * 73244475;
      x = (x >>> 16 ^ x) * 73244475;
      return x >>> 16 ^ x;
   }
}
