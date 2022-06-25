package reactor.core.publisher;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class BlockingIterable<T> implements Iterable<T>, Scannable {
   final CorePublisher<? extends T> source;
   final int batchSize;
   final Supplier<Queue<T>> queueSupplier;

   BlockingIterable(CorePublisher<? extends T> source, int batchSize, Supplier<Queue<T>> queueSupplier) {
      if (batchSize <= 0) {
         throw new IllegalArgumentException("batchSize > 0 required but it was " + batchSize);
      } else {
         this.source = (CorePublisher)Objects.requireNonNull(source, "source");
         this.batchSize = batchSize;
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
      }
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return Math.min(Integer.MAX_VALUE, this.batchSize);
      } else if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   public Iterator<T> iterator() {
      BlockingIterable.SubscriberIterator<T> it = this.createIterator();
      this.source.subscribe(it);
      return it;
   }

   public Spliterator<T> spliterator() {
      return this.stream().spliterator();
   }

   public Stream<T> stream() {
      BlockingIterable.SubscriberIterator<T> it = this.createIterator();
      this.source.subscribe(it);
      Spliterator<T> sp = Spliterators.spliteratorUnknownSize(it, 0);
      return (Stream<T>)StreamSupport.stream(sp, false).onClose(it);
   }

   BlockingIterable.SubscriberIterator<T> createIterator() {
      Queue<T> q;
      try {
         q = (Queue)Objects.requireNonNull(this.queueSupplier.get(), "The queueSupplier returned a null queue");
      } catch (Throwable var3) {
         throw Exceptions.propagate(var3);
      }

      return new BlockingIterable.SubscriberIterator<>(q, this.batchSize);
   }

   static final class SubscriberIterator<T> implements InnerConsumer<T>, Iterator<T>, Runnable {
      final Queue<T> queue;
      final int batchSize;
      final int limit;
      final Lock lock;
      final Condition condition;
      long produced;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<BlockingIterable.SubscriberIterator, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         BlockingIterable.SubscriberIterator.class, Subscription.class, "s"
      );
      volatile boolean done;
      Throwable error;

      SubscriberIterator(Queue<T> queue, int batchSize) {
         this.queue = queue;
         this.batchSize = batchSize;
         this.limit = Operators.unboundedOrLimit(batchSize);
         this.lock = new ReentrantLock();
         this.condition = this.lock.newCondition();
      }

      @Override
      public Context currentContext() {
         return Context.empty();
      }

      public boolean hasNext() {
         if (Schedulers.isInNonBlockingThread()) {
            throw new IllegalStateException(
               "Iterating over a toIterable() / toStream() is blocking, which is not supported in thread " + Thread.currentThread().getName()
            );
         } else {
            while(true) {
               boolean d = this.done;
               boolean empty = this.queue.isEmpty();
               if (d) {
                  Throwable e = this.error;
                  if (e != null) {
                     throw Exceptions.propagate(e);
                  }

                  if (empty) {
                     return false;
                  }
               }

               if (!empty) {
                  return true;
               }

               this.lock.lock();

               try {
                  while(!this.done && this.queue.isEmpty()) {
                     this.condition.await();
                  }
               } catch (InterruptedException var7) {
                  this.run();
                  throw Exceptions.propagate(var7);
               } finally {
                  this.lock.unlock();
               }
            }
         }
      }

      public T next() {
         if (this.hasNext()) {
            T v = (T)this.queue.poll();
            if (v == null) {
               this.run();
               throw new IllegalStateException("Queue is empty: Expected one element to be available from the Reactive Streams source.");
            } else {
               long p = this.produced + 1L;
               if (p == (long)this.limit) {
                  this.produced = 0L;
                  this.s.request(p);
               } else {
                  this.produced = p;
               }

               return v;
            }
         } else {
            throw new NoSuchElementException();
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Operators.unboundedOrPrefetch(this.batchSize));
         }

      }

      @Override
      public void onNext(T t) {
         if (!this.queue.offer(t)) {
            Operators.terminate(S, this);
            this.onError(
               Operators.onOperatorError(
                  null, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.currentContext()
               )
            );
         } else {
            this.signalConsumer();
         }

      }

      @Override
      public void onError(Throwable t) {
         this.error = t;
         this.done = true;
         this.signalConsumer();
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.signalConsumer();
      }

      void signalConsumer() {
         this.lock.lock();

         try {
            this.condition.signalAll();
         } finally {
            this.lock.unlock();
         }

      }

      public void run() {
         Operators.terminate(S, this);
         this.signalConsumer();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.batchSize;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }
}
