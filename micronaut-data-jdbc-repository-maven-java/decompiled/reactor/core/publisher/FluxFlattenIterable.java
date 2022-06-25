package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxFlattenIterable<T, R> extends InternalFluxOperator<T, R> implements Fuseable {
   final Function<? super T, ? extends Iterable<? extends R>> mapper;
   final int prefetch;
   final Supplier<Queue<T>> queueSupplier;

   FluxFlattenIterable(Flux<? extends T> source, Function<? super T, ? extends Iterable<? extends R>> mapper, int prefetch, Supplier<Queue<T>> queueSupplier) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
         this.prefetch = prefetch;
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) throws Exception {
      if (!(this.source instanceof Callable)) {
         return new FluxFlattenIterable.FlattenIterableSubscriber<>(actual, this.mapper, this.prefetch, this.queueSupplier);
      } else {
         T v = (T)((Callable)this.source).call();
         if (v == null) {
            Operators.complete(actual);
            return null;
         } else {
            Iterator<? extends R> it;
            boolean knownToBeFinite;
            try {
               Iterable<? extends R> iter = (Iterable)this.mapper.apply(v);
               it = iter.iterator();
               knownToBeFinite = FluxIterable.checkFinite(iter);
            } catch (Throwable var8) {
               Context ctx = actual.currentContext();
               Throwable e_ = Operators.onNextError(v, var8, ctx);
               Operators.onDiscard(v, ctx);
               if (e_ != null) {
                  Operators.error(actual, e_);
               } else {
                  Operators.complete(actual);
               }

               return null;
            }

            FluxIterable.subscribe(actual, it, knownToBeFinite);
            return null;
         }
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FlattenIterableSubscriber<T, R> implements InnerOperator<T, R>, Fuseable.QueueSubscription<R> {
      final CoreSubscriber<? super R> actual;
      final Function<? super T, ? extends Iterable<? extends R>> mapper;
      final int prefetch;
      final int limit;
      final Supplier<Queue<T>> queueSupplier;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxFlattenIterable.FlattenIterableSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxFlattenIterable.FlattenIterableSubscriber.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxFlattenIterable.FlattenIterableSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxFlattenIterable.FlattenIterableSubscriber.class, "requested"
      );
      Subscription s;
      Queue<T> queue;
      volatile boolean done;
      volatile boolean cancelled;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxFlattenIterable.FlattenIterableSubscriber, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxFlattenIterable.FlattenIterableSubscriber.class, Throwable.class, "error"
      );
      @Nullable
      Iterator<? extends R> current;
      boolean currentKnownToBeFinite;
      int consumed;
      int fusionMode;

      FlattenIterableSubscriber(
         CoreSubscriber<? super R> actual, Function<? super T, ? extends Iterable<? extends R>> mapper, int prefetch, Supplier<Queue<T>> queueSupplier
      ) {
         this.actual = actual;
         this.mapper = mapper;
         this.prefetch = prefetch;
         this.queueSupplier = queueSupplier;
         this.limit = Operators.unboundedOrLimit(prefetch);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> qs = (Fuseable.QueueSubscription)s;
               int m = qs.requestFusion(3);
               if (m == 1) {
                  this.fusionMode = m;
                  this.queue = qs;
                  this.done = true;
                  this.actual.onSubscribe(this);
                  return;
               }

               if (m == 2) {
                  this.fusionMode = m;
                  this.queue = qs;
                  this.actual.onSubscribe(this);
                  s.request(Operators.unboundedOrPrefetch(this.prefetch));
                  return;
               }
            }

            this.queue = (Queue)this.queueSupplier.get();
            this.actual.onSubscribe(this);
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.fusionMode != 2 && !this.queue.offer(t)) {
            Context ctx = this.actual.currentContext();
            this.onError(
               Operators.onOperatorError(this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), ctx)
            );
            Operators.onDiscard(t, ctx);
         } else {
            this.drain(t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (Exceptions.addThrowable(ERROR, this, t)) {
            this.done = true;
            this.drain((T)null);
         } else {
            Operators.onErrorDropped(t, this.actual.currentContext());
         }

      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain((T)null);
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain((T)null);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            if (WIP.getAndIncrement(this) == 0) {
               Context context = this.actual.currentContext();
               Operators.onDiscardQueueWithClear(this.queue, context, null);
               Operators.onDiscardMultiple(this.current, this.currentKnownToBeFinite, context);
            }
         }

      }

      final void resetCurrent() {
         this.current = null;
         this.currentKnownToBeFinite = false;
      }

      void drainAsync() {
         Subscriber<? super R> a = this.actual;
         Queue<T> q = this.queue;
         int missed = 1;
         Iterator<? extends R> it = this.current;
         boolean itFinite = this.currentKnownToBeFinite;

         while(true) {
            if (it == null) {
               if (this.cancelled) {
                  Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
                  return;
               }

               Throwable ex = this.error;
               if (ex != null) {
                  ex = Exceptions.terminate(ERROR, this);
                  this.resetCurrent();
                  Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
                  a.onError(ex);
                  return;
               }

               boolean d = this.done;

               T t;
               try {
                  t = (T)q.poll();
               } catch (Throwable var15) {
                  this.resetCurrent();
                  Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
                  a.onError(var15);
                  return;
               }

               boolean empty = t == null;
               if (d && empty) {
                  a.onComplete();
                  return;
               }

               if (!empty) {
                  boolean b;
                  try {
                     Iterable<? extends R> iterable = (Iterable)this.mapper.apply(t);
                     it = iterable.iterator();
                     itFinite = FluxIterable.checkFinite(iterable);
                     b = it.hasNext();
                  } catch (Throwable var16) {
                     it = null;
                     itFinite = false;
                     Context ctx = this.actual.currentContext();
                     Throwable e_ = Operators.onNextError(t, var16, ctx, this.s);
                     Operators.onDiscard(t, ctx);
                     if (e_ != null) {
                        this.onError(e_);
                     }
                     continue;
                  }

                  if (!b) {
                     it = null;
                     itFinite = false;
                     int c = this.consumed + 1;
                     if (c == this.limit) {
                        this.consumed = 0;
                        this.s.request((long)c);
                     } else {
                        this.consumed = c;
                     }
                     continue;
                  }
               }
            }

            if (it != null) {
               long r = this.requested;
               long e = 0L;

               while(e != r) {
                  if (this.cancelled) {
                     this.resetCurrent();
                     Context context = this.actual.currentContext();
                     Operators.onDiscardQueueWithClear(q, context, null);
                     Operators.onDiscardMultiple(it, itFinite, context);
                     return;
                  }

                  Throwable ex = this.error;
                  if (ex != null) {
                     ex = Exceptions.terminate(ERROR, this);
                     this.resetCurrent();
                     Context context = this.actual.currentContext();
                     Operators.onDiscardQueueWithClear(q, context, null);
                     Operators.onDiscardMultiple(it, itFinite, context);
                     a.onError(ex);
                     return;
                  }

                  R v;
                  try {
                     v = (R)Objects.requireNonNull(it.next(), "iterator returned null");
                  } catch (Throwable var18) {
                     this.onError(Operators.onOperatorError(this.s, var18, this.actual.currentContext()));
                     continue;
                  }

                  a.onNext(v);
                  if (this.cancelled) {
                     this.resetCurrent();
                     Context context = this.actual.currentContext();
                     Operators.onDiscardQueueWithClear(q, context, null);
                     Operators.onDiscardMultiple(it, itFinite, context);
                     return;
                  }

                  ++e;

                  boolean b;
                  try {
                     b = it.hasNext();
                  } catch (Throwable var17) {
                     this.onError(Operators.onOperatorError(this.s, var17, this.actual.currentContext()));
                     continue;
                  }

                  if (!b) {
                     int c = this.consumed + 1;
                     if (c == this.limit) {
                        this.consumed = 0;
                        this.s.request((long)c);
                     } else {
                        this.consumed = c;
                     }

                     it = null;
                     itFinite = false;
                     this.resetCurrent();
                     break;
                  }
               }

               if (e == r) {
                  if (this.cancelled) {
                     this.resetCurrent();
                     Context context = this.actual.currentContext();
                     Operators.onDiscardQueueWithClear(q, context, null);
                     Operators.onDiscardMultiple(it, itFinite, context);
                     return;
                  }

                  Throwable ex = this.error;
                  if (ex != null) {
                     ex = Exceptions.terminate(ERROR, this);
                     this.resetCurrent();
                     Context context = this.actual.currentContext();
                     Operators.onDiscardQueueWithClear(q, context, null);
                     Operators.onDiscardMultiple(it, itFinite, context);
                     a.onError(ex);
                     return;
                  }

                  boolean d = this.done;
                  boolean empty = q.isEmpty() && it == null;
                  if (d && empty) {
                     this.resetCurrent();
                     a.onComplete();
                     return;
                  }
               }

               if (e != 0L && r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }

               if (it == null) {
                  continue;
               }
            }

            this.current = it;
            this.currentKnownToBeFinite = itFinite;
            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }
      }

      void drainSync() {
         Subscriber<? super R> a = this.actual;
         int missed = 1;
         Iterator<? extends R> it = this.current;
         boolean itFinite = this.currentKnownToBeFinite;

         label128:
         while(true) {
            if (it == null) {
               if (this.cancelled) {
                  Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
                  return;
               }

               boolean d = this.done;
               Queue<T> q = this.queue;

               T t;
               try {
                  t = (T)q.poll();
               } catch (Throwable var16) {
                  this.resetCurrent();
                  Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
                  a.onError(var16);
                  return;
               }

               boolean empty = t == null;
               if (d && empty) {
                  a.onComplete();
                  return;
               }

               if (!empty) {
                  boolean b;
                  try {
                     Iterable<? extends R> iterable = (Iterable)this.mapper.apply(t);
                     it = iterable.iterator();
                     itFinite = FluxIterable.checkFinite(iterable);
                     b = it.hasNext();
                  } catch (Throwable var17) {
                     this.resetCurrent();
                     Context ctx = this.actual.currentContext();
                     Throwable e_ = Operators.onNextError(t, var17, ctx, this.s);
                     Operators.onDiscard(t, ctx);
                     if (e_ == null) {
                        continue;
                     }

                     a.onError(e_);
                     return;
                  }

                  if (!b) {
                     it = null;
                     itFinite = false;
                     continue;
                  }
               }
            }

            if (it != null) {
               long r = this.requested;
               long e = 0L;

               while(true) {
                  if (e != r) {
                     if (this.cancelled) {
                        this.resetCurrent();
                        Context context = this.actual.currentContext();
                        Operators.onDiscardQueueWithClear(this.queue, context, null);
                        Operators.onDiscardMultiple(it, itFinite, context);
                        return;
                     }

                     R v;
                     try {
                        v = (R)Objects.requireNonNull(it.next(), "iterator returned null");
                     } catch (Throwable var14) {
                        this.resetCurrent();
                        a.onError(Operators.onOperatorError(this.s, var14, this.actual.currentContext()));
                        return;
                     }

                     a.onNext(v);
                     if (this.cancelled) {
                        this.resetCurrent();
                        Context context = this.actual.currentContext();
                        Operators.onDiscardQueueWithClear(this.queue, context, null);
                        Operators.onDiscardMultiple(it, itFinite, context);
                        return;
                     }

                     ++e;

                     boolean b;
                     try {
                        b = it.hasNext();
                     } catch (Throwable var15) {
                        this.resetCurrent();
                        a.onError(Operators.onOperatorError(this.s, var15, this.actual.currentContext()));
                        return;
                     }

                     if (b) {
                        continue;
                     }

                     it = null;
                     itFinite = false;
                     this.resetCurrent();
                  }

                  if (e == r) {
                     if (this.cancelled) {
                        this.resetCurrent();
                        Context context = this.actual.currentContext();
                        Operators.onDiscardQueueWithClear(this.queue, context, null);
                        Operators.onDiscardMultiple(it, itFinite, context);
                        return;
                     }

                     boolean d = this.done;
                     boolean empty = this.queue.isEmpty() && it == null;
                     if (d && empty) {
                        this.resetCurrent();
                        a.onComplete();
                        return;
                     }
                  }

                  if (e != 0L && r != Long.MAX_VALUE) {
                     REQUESTED.addAndGet(this, -e);
                  }

                  if (it == null) {
                     continue label128;
                  }
                  break;
               }
            }

            this.current = it;
            this.currentKnownToBeFinite = itFinite;
            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }
      }

      void drain(@Nullable T dataSignal) {
         if (WIP.getAndIncrement(this) != 0) {
            if (dataSignal != null && this.cancelled) {
               Operators.onDiscard(dataSignal, this.actual.currentContext());
            }

         } else {
            if (this.fusionMode == 1) {
               this.drainSync();
            } else {
               this.drainAsync();
            }

         }
      }

      public void clear() {
         Context context = this.actual.currentContext();
         Operators.onDiscardMultiple(this.current, this.currentKnownToBeFinite, context);
         this.resetCurrent();
         Operators.onDiscardQueueWithClear(this.queue, context, null);
      }

      public boolean isEmpty() {
         Iterator<? extends R> it = this.current;
         if (it != null) {
            return !it.hasNext();
         } else {
            return this.queue.isEmpty();
         }
      }

      @Nullable
      public R poll() {
         Iterator<? extends R> it = this.current;

         while(true) {
            if (it == null) {
               T v = (T)this.queue.poll();
               if (v == null) {
                  return null;
               }

               boolean itFinite;
               try {
                  Iterable<? extends R> iterable = (Iterable)this.mapper.apply(v);
                  it = iterable.iterator();
                  itFinite = FluxIterable.checkFinite(iterable);
               } catch (Throwable var6) {
                  Operators.onDiscard(v, this.actual.currentContext());
                  throw var6;
               }

               if (!it.hasNext()) {
                  continue;
               }

               this.current = it;
               this.currentKnownToBeFinite = itFinite;
            } else if (!it.hasNext()) {
               it = null;
               continue;
            }

            R r = (R)Objects.requireNonNull(it.next(), "iterator returned null");
            if (!it.hasNext()) {
               this.resetCurrent();
            }

            return r;
         }
      }

      @Override
      public int requestFusion(int requestedMode) {
         return (requestedMode & 1) != 0 && this.fusionMode == 1 ? 1 : 0;
      }

      public int size() {
         return this.queue.size();
      }
   }
}
