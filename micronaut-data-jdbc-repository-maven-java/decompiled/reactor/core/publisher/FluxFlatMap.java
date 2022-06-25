package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
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
import reactor.util.context.Context;

final class FluxFlatMap<T, R> extends InternalFluxOperator<T, R> {
   final Function<? super T, ? extends Publisher<? extends R>> mapper;
   final boolean delayError;
   final int maxConcurrency;
   final Supplier<? extends Queue<R>> mainQueueSupplier;
   final int prefetch;
   final Supplier<? extends Queue<R>> innerQueueSupplier;

   FluxFlatMap(
      Flux<? extends T> source,
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      boolean delayError,
      int maxConcurrency,
      Supplier<? extends Queue<R>> mainQueueSupplier,
      int prefetch,
      Supplier<? extends Queue<R>> innerQueueSupplier
   ) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else if (maxConcurrency <= 0) {
         throw new IllegalArgumentException("maxConcurrency > 0 required but it was " + maxConcurrency);
      } else {
         this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
         this.delayError = delayError;
         this.prefetch = prefetch;
         this.maxConcurrency = maxConcurrency;
         this.mainQueueSupplier = (Supplier)Objects.requireNonNull(mainQueueSupplier, "mainQueueSupplier");
         this.innerQueueSupplier = (Supplier)Objects.requireNonNull(innerQueueSupplier, "innerQueueSupplier");
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return trySubscribeScalarMap(this.source, actual, this.mapper, false, true)
         ? null
         : new FluxFlatMap.FlatMapMain<>(
            actual, this.mapper, this.delayError, this.maxConcurrency, this.mainQueueSupplier, this.prefetch, this.innerQueueSupplier
         );
   }

   static <T, R> boolean trySubscribeScalarMap(
      Publisher<? extends T> source,
      CoreSubscriber<? super R> s,
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      boolean fuseableExpected,
      boolean errorContinueExpected
   ) {
      if (source instanceof Callable) {
         T t;
         try {
            t = (T)((Callable)source).call();
         } catch (Throwable var13) {
            Context ctx = s.currentContext();
            Throwable e_ = errorContinueExpected ? Operators.onNextError((T)null, var13, ctx) : Operators.onOperatorError(var13, ctx);
            if (e_ != null) {
               Operators.error(s, e_);
            } else {
               Operators.complete(s);
            }

            return true;
         }

         if (t == null) {
            Operators.complete(s);
            return true;
         } else {
            Publisher<? extends R> p;
            try {
               p = (Publisher)Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Publisher");
            } catch (Throwable var12) {
               Context ctx = s.currentContext();
               Throwable e_ = errorContinueExpected ? Operators.onNextError(t, var12, ctx) : Operators.onOperatorError(null, var12, t, ctx);
               if (e_ != null) {
                  Operators.error(s, e_);
               } else {
                  Operators.complete(s);
               }

               return true;
            }

            if (p instanceof Callable) {
               R v;
               try {
                  v = (R)((Callable)p).call();
               } catch (Throwable var11) {
                  Context ctx = s.currentContext();
                  Throwable e_ = errorContinueExpected ? Operators.onNextError(t, var11, ctx) : Operators.onOperatorError(null, var11, t, ctx);
                  if (e_ != null) {
                     Operators.error(s, e_);
                  } else {
                     Operators.complete(s);
                  }

                  return true;
               }

               if (v != null) {
                  s.onSubscribe(Operators.scalarSubscription(s, v));
               } else {
                  Operators.complete(s);
               }
            } else if (fuseableExpected && !(p instanceof Fuseable)) {
               p.subscribe(new FluxHide.SuppressFuseableSubscriber<>(s));
            } else {
               p.subscribe(s);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FlatMapInner<R> implements InnerConsumer<R>, Subscription {
      final FluxFlatMap.FlatMapMain<?, R> parent;
      final int prefetch;
      final int limit;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxFlatMap.FlatMapInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxFlatMap.FlatMapInner.class, Subscription.class, "s"
      );
      long produced;
      volatile Queue<R> queue;
      volatile boolean done;
      int sourceMode;
      int index;

      FlatMapInner(FluxFlatMap.FlatMapMain<?, R> parent, int prefetch) {
         this.parent = parent;
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<R> f = (Fuseable.QueueSubscription)s;
               int m = f.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = 1;
                  this.queue = f;
                  this.done = true;
                  this.parent.drain((R)null);
                  return;
               }

               if (m == 2) {
                  this.sourceMode = 2;
                  this.queue = f;
               }
            }

            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(R t) {
         if (this.sourceMode == 2) {
            this.parent.drain(t);
         } else {
            if (this.done) {
               Operators.onNextDropped(t, this.parent.currentContext());
               return;
            }

            if (this.s == Operators.cancelledSubscription()) {
               Operators.onDiscard(t, this.parent.currentContext());
               return;
            }

            this.parent.tryEmit(this, t);
         }

      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(this, t);
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.parent.innerComplete(this);
      }

      @Override
      public void request(long n) {
         if (this.sourceMode != 1) {
            long p = this.produced + n;
            if (p >= (long)this.limit) {
               this.produced = 0L;
               this.s.request(p);
            } else {
               this.produced = p;
            }

         }
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void cancel() {
         Operators.terminate(S, this);
         Operators.onDiscardQueueWithClear(this.queue, this.parent.currentContext(), null);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.CANCELLED) {
               return this.s == Operators.cancelledSubscription();
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
   }

   static final class FlatMapMain<T, R> extends FlatMapTracker<FluxFlatMap.FlatMapInner<R>> implements InnerOperator<T, R> {
      final boolean delayError;
      final int maxConcurrency;
      final int prefetch;
      final int limit;
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      final Supplier<? extends Queue<R>> mainQueueSupplier;
      final Supplier<? extends Queue<R>> innerQueueSupplier;
      final CoreSubscriber<? super R> actual;
      volatile Queue<R> scalarQueue;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxFlatMap.FlatMapMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxFlatMap.FlatMapMain.class, Throwable.class, "error"
      );
      volatile boolean done;
      volatile boolean cancelled;
      Subscription s;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxFlatMap.FlatMapMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(FluxFlatMap.FlatMapMain.class, "requested");
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxFlatMap.FlatMapMain> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxFlatMap.FlatMapMain.class, "wip");
      static final FluxFlatMap.FlatMapInner[] EMPTY = new FluxFlatMap.FlatMapInner[0];
      static final FluxFlatMap.FlatMapInner[] TERMINATED = new FluxFlatMap.FlatMapInner[0];
      int lastIndex;
      int produced;

      FlatMapMain(
         CoreSubscriber<? super R> actual,
         Function<? super T, ? extends Publisher<? extends R>> mapper,
         boolean delayError,
         int maxConcurrency,
         Supplier<? extends Queue<R>> mainQueueSupplier,
         int prefetch,
         Supplier<? extends Queue<R>> innerQueueSupplier
      ) {
         this.actual = actual;
         this.mapper = mapper;
         this.delayError = delayError;
         this.maxConcurrency = maxConcurrency;
         this.mainQueueSupplier = mainQueueSupplier;
         this.prefetch = prefetch;
         this.innerQueueSupplier = innerQueueSupplier;
         this.limit = Operators.unboundedOrLimit(maxConcurrency);
      }

      @Override
      public final CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.array).filter(Objects::nonNull);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.DELAY_ERROR) {
               return this.delayError;
            } else if (key == Scannable.Attr.PREFETCH) {
               return this.maxConcurrency;
            } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
               return this.requested;
            } else if (key == Scannable.Attr.LARGE_BUFFERED) {
               return (this.scalarQueue != null ? (long)this.scalarQueue.size() : 0L) + (long)this.size;
            } else if (key == Scannable.Attr.BUFFERED) {
               long realBuffered = (this.scalarQueue != null ? (long)this.scalarQueue.size() : 0L) + (long)this.size;
               return realBuffered <= 2147483647L ? (int)realBuffered : Integer.MIN_VALUE;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return this.done && (this.scalarQueue == null || this.scalarQueue.isEmpty());
         }
      }

      FluxFlatMap.FlatMapInner<R>[] empty() {
         return EMPTY;
      }

      FluxFlatMap.FlatMapInner<R>[] terminated() {
         return TERMINATED;
      }

      FluxFlatMap.FlatMapInner<R>[] newArray(int size) {
         return new FluxFlatMap.FlatMapInner[size];
      }

      void setIndex(FluxFlatMap.FlatMapInner<R> entry, int index) {
         entry.index = index;
      }

      void unsubscribeEntry(FluxFlatMap.FlatMapInner<R> entry) {
         entry.cancel();
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain((R)null);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            if (WIP.getAndIncrement(this) == 0) {
               Operators.onDiscardQueueWithClear(this.scalarQueue, this.actual.currentContext(), null);
               this.scalarQueue = null;
               this.s.cancel();
               this.unsubscribe();
            }
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Operators.unboundedOrPrefetch(this.maxConcurrency));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            Publisher<? extends R> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.mapper.apply(t), "The mapper returned a null Publisher");
            } catch (Throwable var8) {
               Context ctx = this.actual.currentContext();
               Throwable e_ = Operators.onNextError(t, var8, ctx, this.s);
               Operators.onDiscard(t, ctx);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.tryEmitScalar((R)null);
               }

               return;
            }

            if (p instanceof Callable) {
               R v;
               try {
                  v = (R)((Callable)p).call();
               } catch (Throwable var7) {
                  Context ctx = this.actual.currentContext();
                  Throwable e_ = Operators.onNextError(t, var7, ctx);
                  if (e_ == null) {
                     this.tryEmitScalar((R)null);
                  } else if (!this.delayError || !Exceptions.addThrowable(ERROR, this, e_)) {
                     this.onError(Operators.onOperatorError(this.s, e_, t, ctx));
                  }

                  Operators.onDiscard(t, ctx);
                  return;
               }

               this.tryEmitScalar(v);
            } else {
               FluxFlatMap.FlatMapInner<R> inner = new FluxFlatMap.FlatMapInner<>(this, this.prefetch);
               if (this.add((T)inner)) {
                  p.subscribe(inner);
               } else {
                  Operators.onDiscard(t, this.actual.currentContext());
               }
            }

         }
      }

      Queue<R> getOrCreateScalarQueue() {
         Queue<R> q = this.scalarQueue;
         if (q == null) {
            q = (Queue)this.mainQueueSupplier.get();
            this.scalarQueue = q;
         }

         return q;
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            if (Exceptions.addThrowable(ERROR, this, t)) {
               this.done = true;
               this.drain((R)null);
            } else {
               Operators.onErrorDropped(t, this.actual.currentContext());
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.drain((R)null);
         }
      }

      void tryEmitScalar(@Nullable R v) {
         if (v == null) {
            if (this.maxConcurrency != Integer.MAX_VALUE) {
               int p = this.produced + 1;
               if (p == this.limit) {
                  this.produced = 0;
                  this.s.request((long)p);
               } else {
                  this.produced = p;
               }
            }

         } else {
            if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
               long r = this.requested;
               Queue<R> q = this.scalarQueue;
               if (r != 0L && (q == null || q.isEmpty())) {
                  this.actual.onNext(v);
                  if (r != Long.MAX_VALUE) {
                     REQUESTED.decrementAndGet(this);
                  }

                  if (this.maxConcurrency != Integer.MAX_VALUE) {
                     int p = this.produced + 1;
                     if (p == this.limit) {
                        this.produced = 0;
                        this.s.request((long)p);
                     } else {
                        this.produced = p;
                     }
                  }
               } else {
                  if (q == null) {
                     q = this.getOrCreateScalarQueue();
                  }

                  if (!q.offer(v) && this.failOverflow(v, this.s)) {
                     this.done = true;
                     this.drainLoop();
                     return;
                  }
               }

               if (WIP.decrementAndGet(this) == 0) {
                  if (this.cancelled) {
                     Operators.onDiscard(v, this.actual.currentContext());
                  }

                  return;
               }

               this.drainLoop();
            } else {
               Queue<R> q = this.getOrCreateScalarQueue();
               if (!q.offer(v) && this.failOverflow(v, this.s)) {
                  this.done = true;
               }

               this.drain(v);
            }

         }
      }

      void tryEmit(FluxFlatMap.FlatMapInner<R> inner, R v) {
         if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
            long r = this.requested;
            Queue<R> q = inner.queue;
            if (r != 0L && (q == null || q.isEmpty())) {
               this.actual.onNext(v);
               if (r != Long.MAX_VALUE) {
                  REQUESTED.decrementAndGet(this);
               }

               inner.request(1L);
            } else {
               if (q == null) {
                  q = this.getOrCreateInnerQueue(inner);
               }

               if (!q.offer(v) && this.failOverflow(v, inner)) {
                  inner.done = true;
                  this.drainLoop();
                  return;
               }
            }

            if (WIP.decrementAndGet(this) == 0) {
               if (this.cancelled) {
                  Operators.onDiscard(v, this.actual.currentContext());
               }

               return;
            }

            this.drainLoop();
         } else {
            Queue<R> q = this.getOrCreateInnerQueue(inner);
            if (!q.offer(v) && this.failOverflow(v, inner)) {
               inner.done = true;
            }

            this.drain(v);
         }

      }

      void drain(@Nullable R dataSignal) {
         if (WIP.getAndIncrement(this) != 0) {
            if (dataSignal != null && this.cancelled) {
               Operators.onDiscard(dataSignal, this.actual.currentContext());
            }

         } else {
            this.drainLoop();
         }
      }

      void drainLoop() {
         int missed = 1;
         Subscriber<? super R> a = this.actual;

         while(true) {
            boolean d = this.done;
            FluxFlatMap.FlatMapInner<R>[] as = this.get();
            int n = as.length;
            Queue<R> sq = this.scalarQueue;
            boolean noSources = this.isEmpty();
            if (this.checkTerminated(d, noSources && (sq == null || sq.isEmpty()), a, (R)null)) {
               return;
            }

            boolean again = false;
            long r = this.requested;
            long e = 0L;
            long replenishMain = 0L;
            if (r != 0L && sq != null) {
               while(e != r) {
                  d = this.done;
                  R v = (R)sq.poll();
                  boolean empty = v == null;
                  if (this.checkTerminated(d, false, a, v)) {
                     return;
                  }

                  if (empty) {
                     break;
                  }

                  a.onNext(v);
                  ++e;
               }

               if (e != 0L) {
                  replenishMain += e;
                  if (r != Long.MAX_VALUE) {
                     r = REQUESTED.addAndGet(this, -e);
                  }

                  e = 0L;
                  again = true;
               }
            }

            if (r != 0L && !noSources) {
               int j = this.lastIndex;

               for(int i = 0; i < n; ++i) {
                  if (this.cancelled) {
                     Operators.onDiscardQueueWithClear(this.scalarQueue, this.actual.currentContext(), null);
                     this.scalarQueue = null;
                     this.s.cancel();
                     this.unsubscribe();
                     return;
                  }

                  FluxFlatMap.FlatMapInner<R> inner = as[j];
                  if (inner != null) {
                     d = inner.done;
                     Queue<R> q = inner.queue;
                     if (d && q == null) {
                        this.remove(inner.index);
                        again = true;
                        ++replenishMain;
                     } else if (q != null) {
                        while(e != r) {
                           d = inner.done;

                           R v;
                           try {
                              v = (R)q.poll();
                           } catch (Throwable var21) {
                              Throwable empty = Operators.onOperatorError(inner, var21, this.actual.currentContext());
                              if (!Exceptions.addThrowable(ERROR, this, empty)) {
                                 Operators.onErrorDropped(empty, this.actual.currentContext());
                              }

                              v = null;
                              d = true;
                           }

                           boolean empty = v == null;
                           if (this.checkTerminated(d, false, a, v)) {
                              return;
                           }

                           if (d && empty) {
                              this.remove(inner.index);
                              again = true;
                              ++replenishMain;
                              break;
                           }

                           if (empty) {
                              break;
                           }

                           a.onNext(v);
                           ++e;
                        }

                        if (e == r) {
                           d = inner.done;
                           boolean empty = q.isEmpty();
                           if (d && empty) {
                              this.remove(inner.index);
                              again = true;
                              ++replenishMain;
                           }
                        }

                        if (e != 0L) {
                           if (!inner.done) {
                              inner.request(e);
                           }

                           if (r != Long.MAX_VALUE) {
                              r = REQUESTED.addAndGet(this, -e);
                              if (r == 0L) {
                                 break;
                              }
                           }

                           e = 0L;
                        }
                     }
                  }

                  if (r == 0L) {
                     break;
                  }

                  if (++j == n) {
                     j = 0;
                  }
               }

               this.lastIndex = j;
            }

            if (r == 0L && !noSources) {
               as = this.get();
               n = as.length;

               for(int i = 0; i < n; ++i) {
                  if (this.cancelled) {
                     Operators.onDiscardQueueWithClear(this.scalarQueue, this.actual.currentContext(), null);
                     this.scalarQueue = null;
                     this.s.cancel();
                     this.unsubscribe();
                     return;
                  }

                  FluxFlatMap.FlatMapInner<R> inner = as[i];
                  if (inner != null) {
                     d = inner.done;
                     Queue<R> q = inner.queue;
                     boolean empty = q == null || q.isEmpty();
                     if (!empty) {
                        break;
                     }

                     if (d && empty) {
                        this.remove(inner.index);
                        again = true;
                        ++replenishMain;
                     }
                  }
               }
            }

            if (replenishMain != 0L && !this.done && !this.cancelled) {
               this.s.request(replenishMain);
            }

            if (!again) {
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            }
         }
      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, @Nullable R value) {
         if (this.cancelled) {
            Context ctx = this.actual.currentContext();
            Operators.onDiscard(value, ctx);
            Operators.onDiscardQueueWithClear(this.scalarQueue, ctx, null);
            this.scalarQueue = null;
            this.s.cancel();
            this.unsubscribe();
            return true;
         } else {
            if (this.delayError) {
               if (d && empty) {
                  Throwable e = this.error;
                  if (e != null && e != Exceptions.TERMINATED) {
                     e = Exceptions.terminate(ERROR, this);
                     a.onError(e);
                  } else {
                     a.onComplete();
                  }

                  return true;
               }
            } else if (d) {
               Throwable e = this.error;
               if (e != null && e != Exceptions.TERMINATED) {
                  e = Exceptions.terminate(ERROR, this);
                  Context ctx = this.actual.currentContext();
                  Operators.onDiscard(value, ctx);
                  Operators.onDiscardQueueWithClear(this.scalarQueue, ctx, null);
                  this.scalarQueue = null;
                  this.s.cancel();
                  this.unsubscribe();
                  a.onError(e);
                  return true;
               }

               if (empty) {
                  a.onComplete();
                  return true;
               }
            }

            return false;
         }
      }

      void innerError(FluxFlatMap.FlatMapInner<R> inner, Throwable e) {
         e = Operators.onNextInnerError(e, this.currentContext(), this.s);
         if (e != null) {
            if (Exceptions.addThrowable(ERROR, this, e)) {
               if (!this.delayError) {
                  this.done = true;
               }

               inner.done = true;
               this.drain((R)null);
            } else {
               inner.done = true;
               Operators.onErrorDropped(e, this.actual.currentContext());
            }
         } else {
            inner.done = true;
            this.drain((R)null);
         }

      }

      boolean failOverflow(R v, Subscription toCancel) {
         Throwable e = Operators.onOperatorError(
            toCancel, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), v, this.actual.currentContext()
         );
         Operators.onDiscard(v, this.actual.currentContext());
         if (!Exceptions.addThrowable(ERROR, this, e)) {
            Operators.onErrorDropped(e, this.actual.currentContext());
            return false;
         } else {
            return true;
         }
      }

      void innerComplete(FluxFlatMap.FlatMapInner<R> inner) {
         if (WIP.getAndIncrement(this) == 0) {
            this.drainLoop();
         }
      }

      Queue<R> getOrCreateInnerQueue(FluxFlatMap.FlatMapInner<R> inner) {
         Queue<R> q = inner.queue;
         if (q == null) {
            q = (Queue)this.innerQueueSupplier.get();
            inner.queue = q;
         }

         return q;
      }
   }
}
