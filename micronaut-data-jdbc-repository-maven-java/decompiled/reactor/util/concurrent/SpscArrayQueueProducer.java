package reactor.util.concurrent;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

class SpscArrayQueueProducer<T> extends SpscArrayQueueP1<T> {
   private static final long serialVersionUID = 1657408315616277653L;
   volatile long producerIndex;
   static final AtomicLongFieldUpdater<SpscArrayQueueProducer> PRODUCER_INDEX = AtomicLongFieldUpdater.newUpdater(SpscArrayQueueProducer.class, "producerIndex");

   SpscArrayQueueProducer(int length) {
      super(length);
   }
}
