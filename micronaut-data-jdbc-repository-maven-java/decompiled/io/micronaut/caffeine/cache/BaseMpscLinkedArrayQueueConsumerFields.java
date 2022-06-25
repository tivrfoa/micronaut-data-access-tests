package io.micronaut.caffeine.cache;

abstract class BaseMpscLinkedArrayQueueConsumerFields<E> extends BaseMpscLinkedArrayQueuePad2<E> {
   protected long consumerMask;
   protected E[] consumerBuffer;
   protected long consumerIndex;
}
