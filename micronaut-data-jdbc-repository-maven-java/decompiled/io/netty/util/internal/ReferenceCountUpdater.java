package io.netty.util.internal;

import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCounted;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class ReferenceCountUpdater<T extends ReferenceCounted> {
   protected ReferenceCountUpdater() {
   }

   public static long getUnsafeOffset(Class<? extends ReferenceCounted> clz, String fieldName) {
      try {
         if (PlatformDependent.hasUnsafe()) {
            return PlatformDependent.objectFieldOffset(clz.getDeclaredField(fieldName));
         }
      } catch (Throwable var3) {
      }

      return -1L;
   }

   protected abstract AtomicIntegerFieldUpdater<T> updater();

   protected abstract long unsafeOffset();

   public final int initialValue() {
      return 2;
   }

   public void setInitialValue(T instance) {
      long offset = this.unsafeOffset();
      if (offset == -1L) {
         this.updater().set(instance, this.initialValue());
      } else {
         PlatformDependent.safeConstructPutInt(instance, offset, this.initialValue());
      }

   }

   private static int realRefCnt(int rawCnt) {
      return rawCnt != 2 && rawCnt != 4 && (rawCnt & 1) != 0 ? 0 : rawCnt >>> 1;
   }

   private static int toLiveRealRefCnt(int rawCnt, int decrement) {
      if (rawCnt != 2 && rawCnt != 4 && (rawCnt & 1) != 0) {
         throw new IllegalReferenceCountException(0, -decrement);
      } else {
         return rawCnt >>> 1;
      }
   }

   private int nonVolatileRawCnt(T instance) {
      long offset = this.unsafeOffset();
      return offset != -1L ? PlatformDependent.getInt(instance, offset) : this.updater().get(instance);
   }

   public final int refCnt(T instance) {
      return realRefCnt(this.updater().get(instance));
   }

   public final boolean isLiveNonVolatile(T instance) {
      long offset = this.unsafeOffset();
      int rawCnt = offset != -1L ? PlatformDependent.getInt(instance, offset) : this.updater().get(instance);
      return rawCnt == 2 || rawCnt == 4 || rawCnt == 6 || rawCnt == 8 || (rawCnt & 1) == 0;
   }

   public final void setRefCnt(T instance, int refCnt) {
      this.updater().set(instance, refCnt > 0 ? refCnt << 1 : 1);
   }

   public final void resetRefCnt(T instance) {
      this.updater().set(instance, this.initialValue());
   }

   public final T retain(T instance) {
      return this.retain0(instance, 1, 2);
   }

   public final T retain(T instance, int increment) {
      int rawIncrement = ObjectUtil.checkPositive(increment, "increment") << 1;
      return this.retain0(instance, increment, rawIncrement);
   }

   private T retain0(T instance, int increment, int rawIncrement) {
      int oldRef = this.updater().getAndAdd(instance, rawIncrement);
      if (oldRef != 2 && oldRef != 4 && (oldRef & 1) != 0) {
         throw new IllegalReferenceCountException(0, increment);
      } else if ((oldRef > 0 || oldRef + rawIncrement < 0) && (oldRef < 0 || oldRef + rawIncrement >= oldRef)) {
         return instance;
      } else {
         this.updater().getAndAdd(instance, -rawIncrement);
         throw new IllegalReferenceCountException(realRefCnt(oldRef), increment);
      }
   }

   public final boolean release(T instance) {
      int rawCnt = this.nonVolatileRawCnt(instance);
      return rawCnt == 2
         ? this.tryFinalRelease0(instance, 2) || this.retryRelease0(instance, 1)
         : this.nonFinalRelease0(instance, 1, rawCnt, toLiveRealRefCnt(rawCnt, 1));
   }

   public final boolean release(T instance, int decrement) {
      int rawCnt = this.nonVolatileRawCnt(instance);
      int realCnt = toLiveRealRefCnt(rawCnt, ObjectUtil.checkPositive(decrement, "decrement"));
      return decrement == realCnt
         ? this.tryFinalRelease0(instance, rawCnt) || this.retryRelease0(instance, decrement)
         : this.nonFinalRelease0(instance, decrement, rawCnt, realCnt);
   }

   private boolean tryFinalRelease0(T instance, int expectRawCnt) {
      return this.updater().compareAndSet(instance, expectRawCnt, 1);
   }

   private boolean nonFinalRelease0(T instance, int decrement, int rawCnt, int realCnt) {
      return decrement < realCnt && this.updater().compareAndSet(instance, rawCnt, rawCnt - (decrement << 1)) ? false : this.retryRelease0(instance, decrement);
   }

   private boolean retryRelease0(T instance, int decrement) {
      while(true) {
         int rawCnt = this.updater().get(instance);
         int realCnt = toLiveRealRefCnt(rawCnt, decrement);
         if (decrement == realCnt) {
            if (this.tryFinalRelease0(instance, rawCnt)) {
               return true;
            }
         } else {
            if (decrement >= realCnt) {
               throw new IllegalReferenceCountException(realCnt, -decrement);
            }

            if (this.updater().compareAndSet(instance, rawCnt, rawCnt - (decrement << 1))) {
               return false;
            }
         }

         Thread.yield();
      }
   }
}
