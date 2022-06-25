package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
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
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class FluxMergeSequential<T, R> extends InternalFluxOperator<T, R> {
   final FluxConcatMap.ErrorMode errorMode;
   final Function<? super T, ? extends Publisher<? extends R>> mapper;
   final int maxConcurrency;
   final int prefetch;
   final Supplier<Queue<FluxMergeSequential.MergeSequentialInner<R>>> queueSupplier;

   FluxMergeSequential(
      Flux<? extends T> source,
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      int maxConcurrency,
      int prefetch,
      FluxConcatMap.ErrorMode errorMode
   ) {
      this(source, mapper, maxConcurrency, prefetch, errorMode, Queues.get(Math.max(prefetch, maxConcurrency)));
   }

   FluxMergeSequential(
      Flux<? extends T> source,
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      int maxConcurrency,
      int prefetch,
      FluxConcatMap.ErrorMode errorMode,
      Supplier<Queue<FluxMergeSequential.MergeSequentialInner<R>>> queueSupplier
   ) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else if (maxConcurrency <= 0) {
         throw new IllegalArgumentException("maxConcurrency > 0 required but it was " + maxConcurrency);
      } else {
         this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
         this.maxConcurrency = maxConcurrency;
         this.prefetch = prefetch;
         this.errorMode = errorMode;
         this.queueSupplier = queueSupplier;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return FluxFlatMap.trySubscribeScalarMap(this.source, actual, this.mapper, false, false)
         ? null
         : new FluxMergeSequential.MergeSequentialMain<>(actual, this.mapper, this.maxConcurrency, this.prefetch, this.errorMode, this.queueSupplier);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MergeSequentialInner<R> implements InnerConsumer<R> {
      final FluxMergeSequential.MergeSequentialMain<?, R> parent;
      final int prefetch;
      final int limit;
      volatile Queue<R> queue;
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<FluxMergeSequential.MergeSequentialInner, Subscription> SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         FluxMergeSequential.MergeSequentialInner.class, Subscription.class, "subscription"
      );
      volatile boolean done;
      long produced;
      int fusionMode;

      MergeSequentialInner(FluxMergeSequential.MergeSequentialMain<?, R> parent, int prefetch) {
         this.parent = parent;
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.subscription;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.CANCELLED) {
               return this.subscription == Operators.cancelledSubscription();
            } else if (key == Scannable.Attr.BUFFERED) {
               return this.queue == null ? 0 : this.queue.size();
            } else if (key == Scannable.Attr.PREFETCH) {
               return this.prefetch;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
            }
         } else {
            return this.done && (this.queue == null || this.queue.isEmpty());
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<R> qs = (Fuseable.QueueSubscription)s;
               int m = qs.requestFusion(7);
               if (m == 1) {
                  this.fusionMode = m;
                  this.queue = qs;
                  this.done = true;
                  this.parent.innerComplete(this);
                  return;
               }

               if (m == 2) {
                  this.fusionMode = m;
                  this.queue = qs;
                  s.request(Operators.unboundedOrPrefetch(this.prefetch));
                  return;
               }
            }

            this.queue = (Queue)Queues.get(this.prefetch).get();
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(R t) {
         if (this.fusionMode == 0) {
            this.parent.innerNext(this, t);
         } else {
            this.parent.drain();
         }

      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(this, t);
      }

      @Override
      public void onComplete() {
         this.parent.innerComplete(this);
      }

      void requestOne() {
         if (this.fusionMode != 1) {
            long p = this.produced + 1L;
            if (p == (long)this.limit) {
               this.produced = 0L;
               this.subscription.request(p);
            } else {
               this.produced = p;
            }
         }

      }

      void cancel() {
         Operators.set(SUBSCRIPTION, this, Operators.cancelledSubscription());
      }

      boolean isDone() {
         return this.done;
      }

      void setDone() {
         this.done = true;
      }

      Queue<R> queue() {
         return this.queue;
      }
   }

   static final class MergeSequentialMain<T, R> implements InnerOperator<T, R> {
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      final int maxConcurrency;
      final int prefetch;
      final Queue<FluxMergeSequential.MergeSequentialInner<R>> subscribers;
      final FluxConcatMap.ErrorMode errorMode;
      final CoreSubscriber<? super R> actual;
      Subscription s;
      volatile boolean done;
      volatile boolean cancelled;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxMergeSequential.MergeSequentialMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxMergeSequential.MergeSequentialMain.class, Throwable.class, "error"
      );
      FluxMergeSequential.MergeSequentialInner<R> current;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxMergeSequential.MergeSequentialMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxMergeSequential.MergeSequentialMain.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxMergeSequential.MergeSequentialMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxMergeSequential.MergeSequentialMain.class, "requested"
      );

      MergeSequentialMain(
         CoreSubscriber<? super R> actual,
         Function<? super T, ? extends Publisher<? extends R>> mapper,
         int maxConcurrency,
         int prefetch,
         FluxConcatMap.ErrorMode errorMode,
         Supplier<Queue<FluxMergeSequential.MergeSequentialInner<R>>> queueSupplier
      ) {
         this.actual = actual;
         this.mapper = mapper;
         this.maxConcurrency = maxConcurrency;
         this.prefetch = prefetch;
         this.errorMode = errorMode;
         this.subscribers = (Queue)queueSupplier.get();
      }

      @Override
      public final CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers.peek());
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.DELAY_ERROR) {
               return this.errorMode != FluxConcatMap.ErrorMode.IMMEDIATE;
            } else if (key == Scannable.Attr.PREFETCH) {
               return this.maxConcurrency;
            } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
               return this.requested;
            } else if (key == Scannable.Attr.BUFFERED) {
               return this.subscribers.size();
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return this.done && this.subscribers.isEmpty();
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(this.maxConcurrency == Integer.MAX_VALUE ? Long.MAX_VALUE : (long)this.maxConcurrency);
         }

      }

      @Override
      public void onNext(T t) {
         Publisher<? extends R> publisher;
         try {
            publisher = (Publisher)Objects.requireNonNull(this.mapper.apply(t), "publisher");
         } catch (Throwable var5) {
            this.onError(Operators.onOperatorError(this.s, var5, t, this.actual.currentContext()));
            return;
         }

         FluxMergeSequential.MergeSequentialInner<R> inner = new FluxMergeSequential.MergeSequentialInner<>(this, this.prefetch);
         if (!this.cancelled) {
            if (!this.subscribers.offer(inner)) {
               int badSize = this.subscribers.size();
               inner.cancel();
               this.drainAndCancel();
               this.onError(
                  Operators.onOperatorError(
                     this.s,
                     new IllegalStateException("Too many subscribers for fluxMergeSequential on item: " + t + "; subscribers: " + badSize),
                     t,
                     this.actual.currentContext()
                  )
               );
            } else if (!this.cancelled) {
               publisher.subscribe(inner);
               if (this.cancelled) {
                  inner.cancel();
                  this.drainAndCancel();
               }

            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (Exceptions.addThrowable(ERROR, this, t)) {
            this.done = true;
            this.drain();
         } else {
            Operators.onErrorDropped(t, this.actual.currentContext());
         }

      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain();
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            this.drainAndCancel();
         }
      }

      void drainAndCancel() {
         if (WIP.getAndIncrement(this) == 0) {
            do {
               this.cancelAll();
            } while(WIP.decrementAndGet(this) != 0);
         }

      }

      void cancelAll() {
         FluxMergeSequential.MergeSequentialInner<R> c = this.current;
         if (c != null) {
            c.cancel();
         }

         FluxMergeSequential.MergeSequentialInner<R> inner;
         while((inner = (FluxMergeSequential.MergeSequentialInner)this.subscribers.poll()) != null) {
            inner.cancel();
         }

      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain();
         }

      }

      void innerNext(FluxMergeSequential.MergeSequentialInner<R> inner, R value) {
         if (inner.queue().offer(value)) {
            this.drain();
         } else {
            inner.cancel();
            this.onError(
               Operators.onOperatorError(
                  null, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), value, this.actual.currentContext()
               )
            );
         }

      }

      void innerError(FluxMergeSequential.MergeSequentialInner<R> inner, Throwable e) {
         if (Exceptions.addThrowable(ERROR, this, e)) {
            inner.setDone();
            if (this.errorMode != FluxConcatMap.ErrorMode.END) {
               this.s.cancel();
            }

            this.drain();
         } else {
            Operators.onErrorDropped(e, this.actual.currentContext());
         }

      }

      void innerComplete(FluxMergeSequential.MergeSequentialInner<R> inner) {
         inner.setDone();
         this.drain();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;
            FluxMergeSequential.MergeSequentialInner<R> inner = this.current;
            Subscriber<? super R> a = this.actual;
            FluxConcatMap.ErrorMode em = this.errorMode;

            while(true) {
               long r = this.requested;
               long e = 0L;
               if (inner == null) {
                  if (em != FluxConcatMap.ErrorMode.END) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        this.cancelAll();
                        a.onError(ex);
                        return;
                     }
                  }

                  boolean outerDone = this.done;
                  inner = (FluxMergeSequential.MergeSequentialInner)this.subscribers.poll();
                  if (outerDone && inner == null) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        a.onError(ex);
                     } else {
                        a.onComplete();
                     }

                     return;
                  }

                  if (inner != null) {
                     this.current = inner;
                  }
               }

               boolean continueNextSource = false;
               if (inner != null) {
                  Queue<R> q = inner.queue();
                  if (q != null) {
                     while(e != r) {
                        if (this.cancelled) {
                           this.cancelAll();
                           return;
                        }

                        if (em == FluxConcatMap.ErrorMode.IMMEDIATE) {
                           Throwable ex = this.error;
                           if (ex != null) {
                              this.current = null;
                              inner.cancel();
                              this.cancelAll();
                              a.onError(ex);
                              return;
                           }
                        }

                        boolean d = inner.isDone();

                        R v;
                        try {
                           v = (R)q.poll();
                        } catch (Throwable var14) {
                           this.current = null;
                           inner.cancel();
                           Throwable empty = Operators.onOperatorError(var14, this.actual.currentContext());
                           this.cancelAll();
                           a.onError(empty);
                           return;
                        }

                        boolean empty = v == null;
                        if (d && empty) {
                           inner = null;
                           this.current = null;
                           this.s.request(1L);
                           continueNextSource = true;
                           break;
                        }

                        if (empty) {
                           break;
                        }

                        a.onNext(v);
                        ++e;
                        inner.requestOne();
                     }

                     if (e == r) {
                        if (this.cancelled) {
                           this.cancelAll();
                           return;
                        }

                        if (em == FluxConcatMap.ErrorMode.IMMEDIATE) {
                           Throwable ex = this.error;
                           if (ex != null) {
                              this.current = null;
                              inner.cancel();
                              this.cancelAll();
                              a.onError(ex);
                              return;
                           }
                        }

                        boolean d = inner.isDone();
                        boolean empty = q.isEmpty();
                        if (d && empty) {
                           inner = null;
                           this.current = null;
                           this.s.request(1L);
                           continueNextSource = true;
                        }
                     }
                  }
               }

               if (e != 0L && r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }

               if (!continueNextSource) {
                  missed = WIP.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               }
            }
         }
      }
   }
}
