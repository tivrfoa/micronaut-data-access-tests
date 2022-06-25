package reactor.util.concurrent;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiPredicate;
import reactor.util.annotation.Nullable;

final class MpscLinkedQueue<E> extends AbstractQueue<E> implements BiPredicate<E, E> {
   private volatile MpscLinkedQueue.LinkedQueueNode<E> producerNode;
   private static final AtomicReferenceFieldUpdater<MpscLinkedQueue, MpscLinkedQueue.LinkedQueueNode> PRODUCER_NODE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(
      MpscLinkedQueue.class, MpscLinkedQueue.LinkedQueueNode.class, "producerNode"
   );
   private volatile MpscLinkedQueue.LinkedQueueNode<E> consumerNode;
   private static final AtomicReferenceFieldUpdater<MpscLinkedQueue, MpscLinkedQueue.LinkedQueueNode> CONSUMER_NODE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(
      MpscLinkedQueue.class, MpscLinkedQueue.LinkedQueueNode.class, "consumerNode"
   );

   public MpscLinkedQueue() {
      MpscLinkedQueue.LinkedQueueNode<E> node = new MpscLinkedQueue.LinkedQueueNode<>();
      CONSUMER_NODE_UPDATER.lazySet(this, node);
      PRODUCER_NODE_UPDATER.getAndSet(this, node);
   }

   public final boolean offer(E e) {
      Objects.requireNonNull(e, "The offered value 'e' must be non-null");
      MpscLinkedQueue.LinkedQueueNode<E> nextNode = new MpscLinkedQueue.LinkedQueueNode<>(e);
      MpscLinkedQueue.LinkedQueueNode<E> prevProducerNode = (MpscLinkedQueue.LinkedQueueNode)PRODUCER_NODE_UPDATER.getAndSet(this, nextNode);
      prevProducerNode.soNext(nextNode);
      return true;
   }

   public boolean test(E e1, E e2) {
      Objects.requireNonNull(e1, "The offered value 'e1' must be non-null");
      Objects.requireNonNull(e2, "The offered value 'e2' must be non-null");
      MpscLinkedQueue.LinkedQueueNode<E> nextNode = new MpscLinkedQueue.LinkedQueueNode<>(e1);
      MpscLinkedQueue.LinkedQueueNode<E> nextNextNode = new MpscLinkedQueue.LinkedQueueNode<>(e2);
      MpscLinkedQueue.LinkedQueueNode<E> prevProducerNode = (MpscLinkedQueue.LinkedQueueNode)PRODUCER_NODE_UPDATER.getAndSet(this, nextNextNode);
      nextNode.soNext(nextNextNode);
      prevProducerNode.soNext(nextNode);
      return true;
   }

   @Nullable
   public E poll() {
      MpscLinkedQueue.LinkedQueueNode<E> currConsumerNode = this.consumerNode;
      MpscLinkedQueue.LinkedQueueNode<E> nextNode = currConsumerNode.lvNext();
      if (nextNode != null) {
         E nextValue = nextNode.getAndNullValue();
         currConsumerNode.soNext(currConsumerNode);
         CONSUMER_NODE_UPDATER.lazySet(this, nextNode);
         return nextValue;
      } else if (currConsumerNode == this.producerNode) {
         return null;
      } else {
         while((nextNode = currConsumerNode.lvNext()) == null) {
         }

         E nextValue = nextNode.getAndNullValue();
         currConsumerNode.soNext(currConsumerNode);
         CONSUMER_NODE_UPDATER.lazySet(this, nextNode);
         return nextValue;
      }
   }

   @Nullable
   public E peek() {
      MpscLinkedQueue.LinkedQueueNode<E> currConsumerNode = this.consumerNode;
      MpscLinkedQueue.LinkedQueueNode<E> nextNode = currConsumerNode.lvNext();
      if (nextNode != null) {
         return nextNode.lpValue();
      } else if (currConsumerNode == this.producerNode) {
         return null;
      } else {
         while((nextNode = currConsumerNode.lvNext()) == null) {
         }

         return nextNode.lpValue();
      }
   }

   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   public void clear() {
      while(this.poll() != null && !this.isEmpty()) {
      }

   }

   public int size() {
      MpscLinkedQueue.LinkedQueueNode<E> chaserNode = this.consumerNode;
      MpscLinkedQueue.LinkedQueueNode<E> producerNode = this.producerNode;

      int size;
      for(size = 0; chaserNode != producerNode && chaserNode != null && size < Integer.MAX_VALUE; ++size) {
         MpscLinkedQueue.LinkedQueueNode<E> next = chaserNode.lvNext();
         if (next == chaserNode) {
            return size;
         }

         chaserNode = next;
      }

      return size;
   }

   public boolean isEmpty() {
      return this.consumerNode == this.producerNode;
   }

   public Iterator<E> iterator() {
      throw new UnsupportedOperationException();
   }

   static final class LinkedQueueNode<E> {
      private volatile MpscLinkedQueue.LinkedQueueNode<E> next;
      private static final AtomicReferenceFieldUpdater<MpscLinkedQueue.LinkedQueueNode, MpscLinkedQueue.LinkedQueueNode> NEXT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(
         MpscLinkedQueue.LinkedQueueNode.class, MpscLinkedQueue.LinkedQueueNode.class, "next"
      );
      private E value;

      LinkedQueueNode() {
         this((E)null);
      }

      LinkedQueueNode(@Nullable E val) {
         this.spValue(val);
      }

      @Nullable
      public E getAndNullValue() {
         E temp = this.lpValue();
         this.spValue((E)null);
         return temp;
      }

      @Nullable
      public E lpValue() {
         return this.value;
      }

      public void spValue(@Nullable E newValue) {
         this.value = newValue;
      }

      public void soNext(@Nullable MpscLinkedQueue.LinkedQueueNode<E> n) {
         NEXT_UPDATER.lazySet(this, n);
      }

      @Nullable
      public MpscLinkedQueue.LinkedQueueNode<E> lvNext() {
         return this.next;
      }
   }
}
