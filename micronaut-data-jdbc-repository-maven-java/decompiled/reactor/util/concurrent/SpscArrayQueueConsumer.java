package reactor.util.concurrent;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

class SpscArrayQueueConsumer<T> extends SpscArrayQueueP2<T> {
   private static final long serialVersionUID = 4075549732218321659L;
   volatile long consumerIndex;
   static final AtomicLongFieldUpdater<SpscArrayQueueConsumer> CONSUMER_INDEX = AtomicLongFieldUpdater.newUpdater(SpscArrayQueueConsumer.class, "consumerIndex");

   SpscArrayQueueConsumer(int length) {
      super(length);
   }
}
