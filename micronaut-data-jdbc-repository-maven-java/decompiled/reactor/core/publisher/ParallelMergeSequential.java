package reactor.core.publisher;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class ParallelMergeSequential<T> extends Flux<T> implements Scannable {
   final ParallelFlux<? extends T> source;
   final int prefetch;
   final Supplier<Queue<T>> queueSupplier;

   ParallelMergeSequential(ParallelFlux<? extends T> source, int prefetch, Supplier<Queue<T>> queueSupplier) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.source = source;
         this.prefetch = prefetch;
         this.queueSupplier = queueSupplier;
      }
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      ParallelMergeSequential.MergeSequentialMain<T> parent = new ParallelMergeSequential.MergeSequentialMain<>(
         actual, this.source.parallelism(), this.prefetch, this.queueSupplier
      );
      actual.onSubscribe(parent);
      this.source.subscribe(parent.subscribers);
   }

   static final class MergeSequentialInner<T> implements InnerConsumer<T> {
      final ParallelMergeSequential.MergeSequentialMain<T> parent;
      final int prefetch;
      final int limit;
      long produced;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<ParallelMergeSequential.MergeSequentialInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         ParallelMergeSequential.MergeSequentialInner.class, Subscription.class, "s"
      );
      volatile Queue<T> queue;
      volatile boolean done;

      MergeSequentialInner(ParallelMergeSequential.MergeSequentialMain<T> parent, int prefetch) {
         this.parent = parent;
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Context currentContext() {
         return this.parent.actual.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         this.parent.onNext(this, t);
      }

      @Override
      public void onError(Throwable t) {
         this.parent.onError(t);
      }

      @Override
      public void onComplete() {
         this.parent.onComplete();
      }

      void requestOne() {
         long p = this.produced + 1L;
         if (p == (long)this.limit) {
            this.produced = 0L;
            this.s.request(p);
         } else {
            this.produced = p;
         }

      }

      public void cancel() {
         Operators.terminate(S, this);
      }

      Queue<T> getQueue(Supplier<Queue<T>> queueSupplier) {
         Queue<T> q = this.queue;
         if (q == null) {
            q = (Queue)queueSupplier.get();
            this.queue = q;
         }

         return q;
      }
   }

   static final class MergeSequentialMain<T> implements InnerProducer<T> {
      final ParallelMergeSequential.MergeSequentialInner<T>[] subscribers;
      final Supplier<Queue<T>> queueSupplier;
      final CoreSubscriber<? super T> actual;
      static final AtomicReferenceFieldUpdater<ParallelMergeSequential.MergeSequentialMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         ParallelMergeSequential.MergeSequentialMain.class, Throwable.class, "error"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<ParallelMergeSequential.MergeSequentialMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         ParallelMergeSequential.MergeSequentialMain.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<ParallelMergeSequential.MergeSequentialMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         ParallelMergeSequential.MergeSequentialMain.class, "requested"
      );
      volatile boolean cancelled;
      volatile int done;
      static final AtomicIntegerFieldUpdater<ParallelMergeSequential.MergeSequentialMain> DONE = AtomicIntegerFieldUpdater.newUpdater(
         ParallelMergeSequential.MergeSequentialMain.class, "done"
      );
      volatile Throwable error;

      MergeSequentialMain(CoreSubscriber<? super T> actual, int n, int prefetch, Supplier<Queue<T>> queueSupplier) {
         this.actual = actual;
         this.queueSupplier = queueSupplier;
         ParallelMergeSequential.MergeSequentialInner<T>[] a = new ParallelMergeSequential.MergeSequentialInner[n];

         for(int i = 0; i < n; ++i) {
            a[i] = new ParallelMergeSequential.MergeSequentialInner<>(this, prefetch);
         }

         this.subscribers = a;
         DONE.lazySet(this, n);
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done == 0;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain();
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.cancelAll();
            if (WIP.getAndIncrement(this) == 0) {
               this.cleanup();
            }
         }

      }

      void cancelAll() {
         for(ParallelMergeSequential.MergeSequentialInner<T> s : this.subscribers) {
            s.cancel();
         }

      }

      void cleanup() {
         for(ParallelMergeSequential.MergeSequentialInner<T> s : this.subscribers) {
            s.queue = null;
         }

      }

      void onNext(ParallelMergeSequential.MergeSequentialInner<T> inner, T value) {
         if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
            if (this.requested != 0L) {
               this.actual.onNext(value);
               if (this.requested != Long.MAX_VALUE) {
                  REQUESTED.decrementAndGet(this);
               }

               inner.requestOne();
            } else {
               Queue<T> q = inner.getQueue(this.queueSupplier);
               if (!q.offer(value)) {
                  this.onError(
                     Operators.onOperatorError(
                        this,
                        Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"),
                        value,
                        this.actual.currentContext()
                     )
                  );
                  return;
               }
            }

            if (WIP.decrementAndGet(this) == 0) {
               return;
            }
         } else {
            Queue<T> q = inner.getQueue(this.queueSupplier);
            if (!q.offer(value)) {
               this.onError(
                  Operators.onOperatorError(
                     this,
                     Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"),
                     value,
                     this.actual.currentContext()
                  )
               );
               return;
            }

            if (WIP.getAndIncrement(this) != 0) {
               return;
            }
         }

         this.drainLoop();
      }

      void onError(Throwable ex) {
         if (ERROR.compareAndSet(this, null, ex)) {
            this.cancelAll();
            this.drain();
         } else if (this.error != ex) {
            Operators.onErrorDropped(ex, this.actual.currentContext());
         }

      }

      void onComplete() {
         if (DONE.decrementAndGet(this) >= 0) {
            this.drain();
         }
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            this.drainLoop();
         }
      }

      void drainLoop() {
         int missed = 1;
         ParallelMergeSequential.MergeSequentialInner<T>[] s = this.subscribers;
         int n = s.length;
         Subscriber<? super T> a = this.actual;

         while(true) {
            long r = this.requested;
            long e = 0L;

            label89:
            while(e != r) {
               if (this.cancelled) {
                  this.cleanup();
                  return;
               }

               Throwable ex = this.error;
               if (ex != null) {
                  this.cleanup();
                  a.onError(ex);
                  return;
               }

               boolean d = this.done == 0;
               boolean empty = true;

               for(int i = 0; i < n; ++i) {
                  ParallelMergeSequential.MergeSequentialInner<T> inner = s[i];
                  Queue<T> q = inner.queue;
                  if (q != null) {
                     T v = (T)q.poll();
                     if (v != null) {
                        empty = false;
                        a.onNext(v);
                        inner.requestOne();
                        if (++e == r) {
                           break label89;
                        }
                     }
                  }
               }

               if (d && empty) {
                  a.onComplete();
                  return;
               }

               if (empty) {
                  break;
               }
            }

            if (e == r) {
               if (this.cancelled) {
                  this.cleanup();
                  return;
               }

               Throwable ex = this.error;
               if (ex != null) {
                  this.cleanup();
                  a.onError(ex);
                  return;
               }

               boolean d = this.done == 0;
               boolean empty = true;

               for(int i = 0; i < n; ++i) {
                  ParallelMergeSequential.MergeSequentialInner<T> inner = s[i];
                  Queue<T> q = inner.queue;
                  if (q != null && !q.isEmpty()) {
                     empty = false;
                     break;
                  }
               }

               if (d && empty) {
                  a.onComplete();
                  return;
               }
            }

            if (e != 0L && r != Long.MAX_VALUE) {
               REQUESTED.addAndGet(this, -e);
            }

            int w = this.wip;
            if (w == missed) {
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            } else {
               missed = w;
            }
         }
      }
   }
}
