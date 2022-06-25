package reactor.util.concurrent;

import java.util.concurrent.atomic.AtomicReferenceArray;

class SpscArrayQueueCold<T> extends AtomicReferenceArray<T> {
   private static final long serialVersionUID = 8491797459632447132L;
   final int mask;

   public SpscArrayQueueCold(int length) {
      super(length);
      this.mask = length - 1;
   }
}
