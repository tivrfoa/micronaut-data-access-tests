package io.micronaut.caffeine.cache;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class StripedBuffer<E> implements Buffer<E> {
   static final long TABLE_BUSY = UnsafeAccess.objectFieldOffset(StripedBuffer.class, "tableBusy");
   static final long PROBE = UnsafeAccess.objectFieldOffset(Thread.class, "threadLocalRandomProbe");
   static final int NCPU = Runtime.getRuntime().availableProcessors();
   static final int MAXIMUM_TABLE_SIZE = 4 * Caffeine.ceilingPowerOfTwo(NCPU);
   static final int ATTEMPTS = 3;
   @Nullable
   transient volatile Buffer<E>[] table;
   transient volatile int tableBusy;

   final boolean casTableBusy() {
      return UnsafeAccess.UNSAFE.compareAndSwapInt(this, TABLE_BUSY, 0, 1);
   }

   static final int getProbe() {
      return UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
   }

   static final int advanceProbe(int probe) {
      probe ^= probe << 13;
      probe ^= probe >>> 17;
      probe ^= probe << 5;
      UnsafeAccess.UNSAFE.putInt(Thread.currentThread(), PROBE, probe);
      return probe;
   }

   protected abstract Buffer<E> create(E var1);

   @Override
   public int offer(E e) {
      int result = 0;
      boolean uncontended = true;
      Buffer<E>[] buffers = this.table;
      int mask;
      Buffer<E> buffer;
      if (buffers == null
         || (mask = buffers.length - 1) < 0
         || (buffer = buffers[getProbe() & mask]) == null
         || !(uncontended = (result = buffer.offer(e)) != -1)) {
         this.expandOrRetry(e, uncontended);
      }

      return result;
   }

   @Override
   public void drainTo(Consumer<E> consumer) {
      Buffer<E>[] buffers = this.table;
      if (buffers != null) {
         for(Buffer<E> buffer : buffers) {
            if (buffer != null) {
               buffer.drainTo(consumer);
            }
         }

      }
   }

   @Override
   public int reads() {
      Buffer<E>[] buffers = this.table;
      if (buffers == null) {
         return 0;
      } else {
         int reads = 0;

         for(Buffer<E> buffer : buffers) {
            if (buffer != null) {
               reads += buffer.reads();
            }
         }

         return reads;
      }
   }

   @Override
   public int writes() {
      Buffer<E>[] buffers = this.table;
      if (buffers == null) {
         return 0;
      } else {
         int writes = 0;

         for(Buffer<E> buffer : buffers) {
            if (buffer != null) {
               writes += buffer.writes();
            }
         }

         return writes;
      }
   }

   final void expandOrRetry(E e, boolean wasUncontended) {
      int h;
      if ((h = getProbe()) == 0) {
         ThreadLocalRandom.current();
         h = getProbe();
         wasUncontended = true;
      }

      boolean collide = false;

      for(int attempt = 0; attempt < 3; ++attempt) {
         Buffer<E>[] buffers = this.table;
         int n;
         if (this.table != null && (n = buffers.length) > 0) {
            Buffer<E> buffer;
            if ((buffer = buffers[n - 1 & h]) == null) {
               if (this.tableBusy == 0 && this.casTableBusy()) {
                  boolean created = false;

                  try {
                     Buffer<E>[] rs = this.table;
                     int mask;
                     int j;
                     if (this.table != null && (mask = rs.length) > 0 && rs[j = mask - 1 & h] == null) {
                        rs[j] = this.create(e);
                        created = true;
                     }
                  } finally {
                     this.tableBusy = 0;
                  }

                  if (created) {
                     break;
                  }
                  continue;
               }

               collide = false;
            } else if (!wasUncontended) {
               wasUncontended = true;
            } else {
               if (buffer.offer(e) != -1) {
                  break;
               }

               if (n >= MAXIMUM_TABLE_SIZE || this.table != buffers) {
                  collide = false;
               } else if (!collide) {
                  collide = true;
               } else if (this.tableBusy == 0 && this.casTableBusy()) {
                  try {
                     if (this.table == buffers) {
                        this.table = (Buffer[])Arrays.copyOf(buffers, n << 1);
                     }
                  } finally {
                     this.tableBusy = 0;
                  }

                  collide = false;
                  continue;
               }
            }

            h = advanceProbe(h);
         } else if (this.tableBusy == 0 && this.table == buffers && this.casTableBusy()) {
            boolean init = false;

            try {
               if (this.table == buffers) {
                  Buffer<E>[] rs = new Buffer[]{this.create(e)};
                  this.table = rs;
                  init = true;
               }
            } finally {
               this.tableBusy = 0;
            }

            if (init) {
               break;
            }
         }
      }

   }
}
