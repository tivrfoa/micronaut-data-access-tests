package reactor.core.publisher;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class ParallelSource<T> extends ParallelFlux<T> implements Scannable {
   final Publisher<? extends T> source;
   final int parallelism;
   final int prefetch;
   final Supplier<Queue<T>> queueSupplier;

   ParallelSource(Publisher<? extends T> source, int parallelism, int prefetch, Supplier<Queue<T>> queueSupplier) {
      if (parallelism <= 0) {
         throw new IllegalArgumentException("parallelism > 0 required but it was " + parallelism);
      } else if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.source = source;
         this.parallelism = parallelism;
         this.prefetch = prefetch;
         this.queueSupplier = queueSupplier;
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public int parallelism() {
      return this.parallelism;
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
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      if (this.validate(subscribers)) {
         this.source.subscribe(new ParallelSource.ParallelSourceMain<>(subscribers, this.prefetch, this.queueSupplier));
      }
   }

   static final class ParallelSourceMain<T> implements InnerConsumer<T> {
      final CoreSubscriber<? super T>[] subscribers;
      final AtomicLongArray requests;
      final long[] emissions;
      final int prefetch;
      final int limit;
      final Supplier<Queue<T>> queueSupplier;
      Subscription s;
      Queue<T> queue;
      Throwable error;
      volatile boolean done;
      int index;
      volatile boolean cancelled;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<ParallelSource.ParallelSourceMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         ParallelSource.ParallelSourceMain.class, "wip"
      );
      volatile int subscriberCount;
      static final AtomicIntegerFieldUpdater<ParallelSource.ParallelSourceMain> SUBSCRIBER_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         ParallelSource.ParallelSourceMain.class, "subscriberCount"
      );
      int produced;
      int sourceMode;

      ParallelSourceMain(CoreSubscriber<? super T>[] subscribers, int prefetch, Supplier<Queue<T>> queueSupplier) {
         this.subscribers = subscribers;
         this.prefetch = prefetch;
         this.queueSupplier = queueSupplier;
         this.limit = Operators.unboundedOrLimit(prefetch);
         this.requests = new AtomicLongArray(subscribers.length);
         this.emissions = new long[subscribers.length];
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers).map(Scannable::from);
      }

      @Override
      public Context currentContext() {
         return this.subscribers[0].currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> qs = (Fuseable.QueueSubscription)s;
               int m = qs.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = m;
                  this.queue = qs;
                  this.done = true;
                  this.setupSubscribers();
                  this.drain();
                  return;
               }

               if (m == 2) {
                  this.sourceMode = m;
                  this.queue = qs;
                  this.setupSubscribers();
                  s.request(Operators.unboundedOrPrefetch(this.prefetch));
                  return;
               }
            }

            this.queue = (Queue)this.queueSupplier.get();
            this.setupSubscribers();
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      void setupSubscribers() {
         int m = this.subscribers.length;

         for(int i = 0; i < m; ++i) {
            if (this.cancelled) {
               return;
            }

            SUBSCRIBER_COUNT.lazySet(this, i + 1);
            this.subscribers[i].onSubscribe(new ParallelSource.ParallelSourceMain.ParallelSourceInner<>(this, i, m));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.currentContext());
         } else if (this.sourceMode == 0 && !this.queue.offer(t)) {
            this.onError(
               Operators.onOperatorError(
                  this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.currentContext()
               )
            );
         } else {
            this.drain();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.currentContext());
         } else {
            this.error = t;
            this.done = true;
            this.drain();
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.drain();
         }
      }

      void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            if (WIP.getAndIncrement(this) == 0) {
               this.queue.clear();
            }
         }

      }

      void drainAsync() {
         int missed = 1;
         Queue<T> q = this.queue;
         CoreSubscriber<? super T>[] a = this.subscribers;
         AtomicLongArray r = this.requests;
         long[] e = this.emissions;
         int n = e.length;
         int idx = this.index;
         int consumed = this.produced;

         while(true) {
            int notReady = 0;

            do {
               if (this.cancelled) {
                  q.clear();
                  return;
               }

               boolean d = this.done;
               if (d) {
                  Throwable ex = this.error;
                  if (ex != null) {
                     q.clear();

                     for(Subscriber<? super T> s : a) {
                        s.onError(ex);
                     }

                     return;
                  }
               }

               boolean empty = q.isEmpty();
               if (d && empty) {
                  for(Subscriber<? super T> s : a) {
                     s.onComplete();
                  }

                  return;
               }

               if (empty) {
                  break;
               }

               long ridx = r.get(idx);
               long eidx = e[idx];
               if (ridx != eidx) {
                  T v;
                  try {
                     v = (T)q.poll();
                  } catch (Throwable var22) {
                     Throwable c = Operators.onOperatorError(this.s, var22, a[idx].currentContext());

                     for(Subscriber<? super T> s : a) {
                        s.onError(c);
                     }

                     return;
                  }

                  if (v == null) {
                     break;
                  }

                  a[idx].onNext(v);
                  e[idx] = eidx + 1L;
                  int c = ++consumed;
                  if (consumed == this.limit) {
                     consumed = 0;
                     this.s.request((long)c);
                  }

                  notReady = 0;
               } else {
                  ++notReady;
               }

               if (++idx == n) {
                  idx = 0;
               }
            } while(notReady != n);

            int w = this.wip;
            if (w == missed) {
               this.index = idx;
               this.produced = consumed;
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            } else {
               missed = w;
            }
         }
      }

      void drainSync() {
         int missed = 1;
         Queue<T> q = this.queue;
         CoreSubscriber<? super T>[] a = this.subscribers;
         AtomicLongArray r = this.requests;
         long[] e = this.emissions;
         int n = e.length;
         int idx = this.index;

         label77:
         while(true) {
            int notReady = 0;

            while(!this.cancelled) {
               if (q.isEmpty()) {
                  for(Subscriber<? super T> s : a) {
                     s.onComplete();
                  }

                  return;
               }

               long ridx = r.get(idx);
               long eidx = e[idx];
               if (ridx != eidx) {
                  T v;
                  try {
                     v = (T)q.poll();
                  } catch (Throwable var19) {
                     Throwable ex = Operators.onOperatorError(this.s, var19, a[idx].currentContext());

                     for(Subscriber<? super T> s : a) {
                        s.onError(ex);
                     }

                     return;
                  }

                  if (v == null) {
                     for(Subscriber<? super T> s : a) {
                        s.onComplete();
                     }

                     return;
                  }

                  a[idx].onNext(v);
                  e[idx] = eidx + 1L;
                  notReady = 0;
               } else {
                  ++notReady;
               }

               if (++idx == n) {
                  idx = 0;
               }

               if (notReady == n) {
                  int w = this.wip;
                  if (w == missed) {
                     this.index = idx;
                     missed = WIP.addAndGet(this, -missed);
                     if (missed == 0) {
                        return;
                     }
                  } else {
                     missed = w;
                  }
                  continue label77;
               }
            }

            q.clear();
            return;
         }
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            if (this.sourceMode == 1) {
               this.drainSync();
            } else {
               this.drainAsync();
            }

         }
      }

      static final class ParallelSourceInner<T> implements InnerProducer<T> {
         final ParallelSource.ParallelSourceMain<T> parent;
         final int index;
         final int length;

         ParallelSourceInner(ParallelSource.ParallelSourceMain<T> parent, int index, int length) {
            this.index = index;
            this.length = length;
            this.parent = parent;
         }

         @Override
         public CoreSubscriber<? super T> actual() {
            return this.parent.subscribers[this.index];
         }

         @Nullable
         @Override
         public Object scanUnsafe(Scannable.Attr key) {
            if (key == Scannable.Attr.PARENT) {
               return this.parent;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
            }
         }

         @Override
         public void request(long n) {
            if (Operators.validate(n)) {
               AtomicLongArray ra = this.parent.requests;

               long r;
               long u;
               do {
                  r = ra.get(this.index);
                  if (r == Long.MAX_VALUE) {
                     return;
                  }

                  u = Operators.addCap(r, n);
               } while(!ra.compareAndSet(this.index, r, u));

               if (this.parent.subscriberCount == this.length) {
                  this.parent.drain();
               }
            }

         }

         @Override
         public void cancel() {
            this.parent.cancel();
         }
      }
   }
}
