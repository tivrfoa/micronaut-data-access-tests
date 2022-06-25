package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxConcatMap<T, R> extends InternalFluxOperator<T, R> {
   final Function<? super T, ? extends Publisher<? extends R>> mapper;
   final Supplier<? extends Queue<T>> queueSupplier;
   final int prefetch;
   final FluxConcatMap.ErrorMode errorMode;

   static <T, R> CoreSubscriber<T> subscriber(
      CoreSubscriber<? super R> s,
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      Supplier<? extends Queue<T>> queueSupplier,
      int prefetch,
      FluxConcatMap.ErrorMode errorMode
   ) {
      switch(errorMode) {
         case BOUNDARY:
            return new FluxConcatMap.ConcatMapDelayed<>(s, mapper, queueSupplier, prefetch, false);
         case END:
            return new FluxConcatMap.ConcatMapDelayed<>(s, mapper, queueSupplier, prefetch, true);
         default:
            return new FluxConcatMap.ConcatMapImmediate<>(s, mapper, queueSupplier, prefetch);
      }
   }

   FluxConcatMap(
      Flux<? extends T> source,
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      Supplier<? extends Queue<T>> queueSupplier,
      int prefetch,
      FluxConcatMap.ErrorMode errorMode
   ) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
         this.prefetch = prefetch;
         this.errorMode = (FluxConcatMap.ErrorMode)Objects.requireNonNull(errorMode, "errorMode");
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return FluxFlatMap.trySubscribeScalarMap(this.source, actual, this.mapper, false, true)
         ? null
         : subscriber(actual, this.mapper, this.queueSupplier, this.prefetch, this.errorMode);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ConcatMapDelayed<T, R> implements FluxConcatMap.FluxConcatMapSupport<T, R> {
      final CoreSubscriber<? super R> actual;
      final FluxConcatMap.ConcatMapInner<R> inner;
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      final Supplier<? extends Queue<T>> queueSupplier;
      final int prefetch;
      final int limit;
      final boolean veryEnd;
      Subscription s;
      int consumed;
      volatile Queue<T> queue;
      volatile boolean done;
      volatile boolean cancelled;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxConcatMap.ConcatMapDelayed, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxConcatMap.ConcatMapDelayed.class, Throwable.class, "error"
      );
      volatile boolean active;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxConcatMap.ConcatMapDelayed> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxConcatMap.ConcatMapDelayed.class, "wip"
      );
      int sourceMode;

      ConcatMapDelayed(
         CoreSubscriber<? super R> actual,
         Function<? super T, ? extends Publisher<? extends R>> mapper,
         Supplier<? extends Queue<T>> queueSupplier,
         int prefetch,
         boolean veryEnd
      ) {
         this.actual = actual;
         this.mapper = mapper;
         this.queueSupplier = queueSupplier;
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
         this.veryEnd = veryEnd;
         this.inner = new FluxConcatMap.ConcatMapInner<>(this);
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.DELAY_ERROR) {
            return true;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : FluxConcatMap.FluxConcatMapSupport.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> f = (Fuseable.QueueSubscription)s;
               int m = f.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = 1;
                  this.queue = f;
                  this.done = true;
                  this.actual.onSubscribe(this);
                  this.drain();
                  return;
               }

               if (m == 2) {
                  this.sourceMode = 2;
                  this.queue = f;
               } else {
                  this.queue = (Queue)this.queueSupplier.get();
               }
            } else {
               this.queue = (Queue)this.queueSupplier.get();
            }

            this.actual.onSubscribe(this);
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.drain();
         } else if (!this.queue.offer(t)) {
            Context ctx = this.actual.currentContext();
            this.onError(
               Operators.onOperatorError(this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, ctx)
            );
            Operators.onDiscard(t, ctx);
         } else {
            this.drain();
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
      public void innerNext(R value) {
         this.actual.onNext(value);
      }

      @Override
      public void innerComplete() {
         this.active = false;
         this.drain();
      }

      @Override
      public void innerError(Throwable e) {
         e = Operators.onNextInnerError(e, this.currentContext(), this.s);
         if (e != null) {
            if (Exceptions.addThrowable(ERROR, this, e)) {
               if (!this.veryEnd) {
                  this.s.cancel();
                  this.done = true;
               }

               this.active = false;
               this.drain();
            } else {
               Operators.onErrorDropped(e, this.actual.currentContext());
            }
         } else {
            this.active = false;
         }

      }

      @Override
      public void request(long n) {
         this.inner.request(n);
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.inner.cancel();
            this.s.cancel();
            Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Context ctx = null;

            while(true) {
               if (this.cancelled) {
                  return;
               }

               if (!this.active) {
                  boolean d = this.done;
                  if (d && !this.veryEnd) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        ex = Exceptions.terminate(ERROR, this);
                        if (ex != Exceptions.TERMINATED) {
                           this.actual.onError(ex);
                        }

                        return;
                     }
                  }

                  T v;
                  try {
                     v = (T)this.queue.poll();
                  } catch (Throwable var10) {
                     this.actual.onError(Operators.onOperatorError(this.s, var10, this.actual.currentContext()));
                     return;
                  }

                  boolean empty = v == null;
                  if (d && empty) {
                     Throwable ex = Exceptions.terminate(ERROR, this);
                     if (ex != null && ex != Exceptions.TERMINATED) {
                        this.actual.onError(ex);
                     } else {
                        this.actual.onComplete();
                     }

                     return;
                  }

                  if (!empty) {
                     Publisher<? extends R> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.mapper.apply(v), "The mapper returned a null Publisher");
                     } catch (Throwable var12) {
                        if (ctx == null) {
                           ctx = this.actual.currentContext();
                        }

                        Operators.onDiscard(v, ctx);
                        Throwable e_ = Operators.onNextError(v, var12, ctx, this.s);
                        if (e_ != null) {
                           this.actual.onError(Operators.onOperatorError(this.s, var12, v, ctx));
                           return;
                        }
                        continue;
                     }

                     if (this.sourceMode != 1) {
                        int c = this.consumed + 1;
                        if (c == this.limit) {
                           this.consumed = 0;
                           this.s.request((long)c);
                        } else {
                           this.consumed = c;
                        }
                     }

                     if (p instanceof Callable) {
                        Callable<R> supplier = (Callable)p;

                        R vr;
                        try {
                           vr = (R)supplier.call();
                        } catch (Throwable var11) {
                           if (ctx == null) {
                              ctx = this.actual.currentContext();
                           }

                           Throwable e_ = Operators.onNextError(v, var11, ctx);
                           if (e_ == null || this.veryEnd && Exceptions.addThrowable(ERROR, this, e_)) {
                              continue;
                           }

                           this.actual.onError(Operators.onOperatorError(this.s, e_, v, ctx));
                           return;
                        }

                        if (vr == null) {
                           continue;
                        }

                        if (this.inner.isUnbounded()) {
                           this.actual.onNext(vr);
                           continue;
                        }

                        this.active = true;
                        this.inner.set(new FluxConcatMap.WeakScalarSubscription<>(vr, this.inner));
                     } else {
                        this.active = true;
                        p.subscribe(this.inner);
                     }
                  }
               }

               if (WIP.decrementAndGet(this) == 0) {
                  break;
               }
            }
         }

      }
   }

   static final class ConcatMapImmediate<T, R> implements FluxConcatMap.FluxConcatMapSupport<T, R> {
      final CoreSubscriber<? super R> actual;
      final Context ctx;
      final FluxConcatMap.ConcatMapInner<R> inner;
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      final Supplier<? extends Queue<T>> queueSupplier;
      final int prefetch;
      final int limit;
      Subscription s;
      int consumed;
      volatile Queue<T> queue;
      volatile boolean done;
      volatile boolean cancelled;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxConcatMap.ConcatMapImmediate, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxConcatMap.ConcatMapImmediate.class, Throwable.class, "error"
      );
      volatile boolean active;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxConcatMap.ConcatMapImmediate> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxConcatMap.ConcatMapImmediate.class, "wip"
      );
      volatile int guard;
      static final AtomicIntegerFieldUpdater<FluxConcatMap.ConcatMapImmediate> GUARD = AtomicIntegerFieldUpdater.newUpdater(
         FluxConcatMap.ConcatMapImmediate.class, "guard"
      );
      int sourceMode;

      ConcatMapImmediate(
         CoreSubscriber<? super R> actual,
         Function<? super T, ? extends Publisher<? extends R>> mapper,
         Supplier<? extends Queue<T>> queueSupplier,
         int prefetch
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.mapper = mapper;
         this.queueSupplier = queueSupplier;
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
         this.inner = new FluxConcatMap.ConcatMapInner<>(this);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.CANCELLED) {
               return this.cancelled;
            } else if (key == Scannable.Attr.PREFETCH) {
               return this.prefetch;
            } else if (key == Scannable.Attr.BUFFERED) {
               return this.queue != null ? this.queue.size() : 0;
            } else if (key == Scannable.Attr.ERROR) {
               return this.error;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : FluxConcatMap.FluxConcatMapSupport.super.scanUnsafe(key);
            }
         } else {
            return this.done || this.error == Exceptions.TERMINATED;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> f = (Fuseable.QueueSubscription)s;
               int m = f.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = 1;
                  this.queue = f;
                  this.done = true;
                  this.actual.onSubscribe(this);
                  this.drain();
                  return;
               }

               if (m == 2) {
                  this.sourceMode = 2;
                  this.queue = f;
               } else {
                  this.queue = (Queue)this.queueSupplier.get();
               }
            } else {
               this.queue = (Queue)this.queueSupplier.get();
            }

            this.actual.onSubscribe(this);
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.drain();
         } else if (!this.queue.offer(t)) {
            this.onError(
               Operators.onOperatorError(
                  this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.ctx
               )
            );
            Operators.onDiscard(t, this.ctx);
         } else {
            this.drain();
         }

      }

      @Override
      public void onError(Throwable t) {
         if (Exceptions.addThrowable(ERROR, this, t)) {
            this.inner.cancel();
            if (GUARD.getAndIncrement(this) == 0) {
               t = Exceptions.terminate(ERROR, this);
               if (t != Exceptions.TERMINATED) {
                  this.actual.onError(t);
                  Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
               }
            }
         } else {
            Operators.onErrorDropped(t, this.ctx);
         }

      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain();
      }

      @Override
      public void innerNext(R value) {
         if (this.guard == 0 && GUARD.compareAndSet(this, 0, 1)) {
            this.actual.onNext(value);
            if (GUARD.compareAndSet(this, 1, 0)) {
               return;
            }

            Throwable e = Exceptions.terminate(ERROR, this);
            if (e != Exceptions.TERMINATED) {
               this.actual.onError(e);
            }
         }

      }

      @Override
      public void innerComplete() {
         this.active = false;
         this.drain();
      }

      @Override
      public void innerError(Throwable e) {
         e = Operators.onNextInnerError(e, this.currentContext(), this.s);
         if (e != null) {
            if (Exceptions.addThrowable(ERROR, this, e)) {
               this.s.cancel();
               if (GUARD.getAndIncrement(this) == 0) {
                  e = Exceptions.terminate(ERROR, this);
                  if (e != Exceptions.TERMINATED) {
                     this.actual.onError(e);
                     Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
                  }
               }
            } else {
               Operators.onErrorDropped(e, this.ctx);
            }
         } else {
            this.active = false;
            this.drain();
         }

      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.inner.request(n);
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.inner.cancel();
            this.s.cancel();
            Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            while(true) {
               if (this.cancelled) {
                  return;
               }

               if (!this.active) {
                  boolean d = this.done;

                  T v;
                  try {
                     v = (T)this.queue.poll();
                  } catch (Throwable var9) {
                     this.actual.onError(Operators.onOperatorError(this.s, var9, this.ctx));
                     return;
                  }

                  boolean empty = v == null;
                  if (d && empty) {
                     this.actual.onComplete();
                     return;
                  }

                  if (!empty) {
                     Publisher<? extends R> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.mapper.apply(v), "The mapper returned a null Publisher");
                     } catch (Throwable var11) {
                        Operators.onDiscard(v, this.ctx);
                        Throwable e_ = Operators.onNextError(v, var11, this.ctx, this.s);
                        if (e_ == null) {
                           continue;
                        }

                        this.actual.onError(Operators.onOperatorError(this.s, var11, v, this.ctx));
                        return;
                     }

                     if (this.sourceMode != 1) {
                        int c = this.consumed + 1;
                        if (c == this.limit) {
                           this.consumed = 0;
                           this.s.request((long)c);
                        } else {
                           this.consumed = c;
                        }
                     }

                     if (p instanceof Callable) {
                        Callable<R> callable = (Callable)p;

                        R vr;
                        try {
                           vr = (R)callable.call();
                        } catch (Throwable var10) {
                           Throwable e_ = Operators.onNextError(v, var10, this.ctx, this.s);
                           if (e_ == null) {
                              continue;
                           }

                           this.actual.onError(Operators.onOperatorError(this.s, var10, v, this.ctx));
                           Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
                           return;
                        }

                        if (vr == null) {
                           continue;
                        }

                        if (this.inner.isUnbounded()) {
                           if (this.guard == 0 && GUARD.compareAndSet(this, 0, 1)) {
                              this.actual.onNext(vr);
                              if (!GUARD.compareAndSet(this, 1, 0)) {
                                 Throwable e = Exceptions.terminate(ERROR, this);
                                 if (e != Exceptions.TERMINATED) {
                                    this.actual.onError(e);
                                 }

                                 return;
                              }
                           }
                           continue;
                        }

                        this.active = true;
                        this.inner.set(new FluxConcatMap.WeakScalarSubscription<>(vr, this.inner));
                     } else {
                        this.active = true;
                        p.subscribe(this.inner);
                     }
                  }
               }

               if (WIP.decrementAndGet(this) == 0) {
                  break;
               }
            }
         }

      }
   }

   static final class ConcatMapInner<R> extends Operators.MultiSubscriptionSubscriber<R, R> {
      final FluxConcatMap.FluxConcatMapSupport<?, R> parent;
      long produced;

      ConcatMapInner(FluxConcatMap.FluxConcatMapSupport<?, R> parent) {
         super(Operators.emptySubscriber());
         this.parent = parent;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void onNext(R t) {
         ++this.produced;
         this.parent.innerNext(t);
      }

      @Override
      public void onError(Throwable t) {
         long p = this.produced;
         if (p != 0L) {
            this.produced = 0L;
            this.produced(p);
         }

         this.parent.innerError(t);
      }

      @Override
      public void onComplete() {
         long p = this.produced;
         if (p != 0L) {
            this.produced = 0L;
            this.produced(p);
         }

         this.parent.innerComplete();
      }
   }

   static enum ErrorMode {
      IMMEDIATE,
      BOUNDARY,
      END;
   }

   interface FluxConcatMapSupport<I, T> extends InnerOperator<I, T> {
      void innerNext(T var1);

      void innerComplete();

      void innerError(Throwable var1);
   }

   static final class WeakScalarSubscription<T> implements Subscription {
      final CoreSubscriber<? super T> actual;
      final T value;
      boolean once;

      WeakScalarSubscription(T value, CoreSubscriber<? super T> actual) {
         this.value = value;
         this.actual = actual;
      }

      @Override
      public void request(long n) {
         if (n > 0L && !this.once) {
            this.once = true;
            Subscriber<? super T> a = this.actual;
            a.onNext(this.value);
            a.onComplete();
         }

      }

      @Override
      public void cancel() {
         Operators.onDiscard(this.value, this.actual.currentContext());
      }
   }
}
