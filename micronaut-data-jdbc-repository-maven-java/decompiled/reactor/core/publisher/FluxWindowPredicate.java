package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxWindowPredicate<T> extends InternalFluxOperator<T, Flux<T>> implements Fuseable {
   final Supplier<? extends Queue<T>> groupQueueSupplier;
   final Supplier<? extends Queue<Flux<T>>> mainQueueSupplier;
   final FluxBufferPredicate.Mode mode;
   final Predicate<? super T> predicate;
   final int prefetch;

   FluxWindowPredicate(
      Flux<? extends T> source,
      Supplier<? extends Queue<Flux<T>>> mainQueueSupplier,
      Supplier<? extends Queue<T>> groupQueueSupplier,
      int prefetch,
      Predicate<? super T> predicate,
      FluxBufferPredicate.Mode mode
   ) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.predicate = (Predicate)Objects.requireNonNull(predicate, "predicate");
         this.mainQueueSupplier = (Supplier)Objects.requireNonNull(mainQueueSupplier, "mainQueueSupplier");
         this.groupQueueSupplier = (Supplier)Objects.requireNonNull(groupQueueSupplier, "groupQueueSupplier");
         this.mode = mode;
         this.prefetch = prefetch;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Flux<T>> actual) {
      return new FluxWindowPredicate.WindowPredicateMain<>(
         actual, (Queue<Flux<? super T>>)this.mainQueueSupplier.get(), this.groupQueueSupplier, this.prefetch, this.predicate, this.mode
      );
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class WindowFlux<T> extends Flux<T> implements Fuseable, Fuseable.QueueSubscription<T>, InnerOperator<T, T> {
      final Queue<T> queue;
      volatile FluxWindowPredicate.WindowPredicateMain<T> parent;
      static final AtomicReferenceFieldUpdater<FluxWindowPredicate.WindowFlux, FluxWindowPredicate.WindowPredicateMain> PARENT = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowFlux.class, FluxWindowPredicate.WindowPredicateMain.class, "parent"
      );
      volatile boolean done;
      Throwable error;
      volatile CoreSubscriber<? super T> actual;
      static final AtomicReferenceFieldUpdater<FluxWindowPredicate.WindowFlux, CoreSubscriber> ACTUAL = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowFlux.class, CoreSubscriber.class, "actual"
      );
      volatile Context ctx = Context.empty();
      volatile boolean cancelled;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxWindowPredicate.WindowFlux> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowFlux.class, "once"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxWindowPredicate.WindowFlux> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowFlux.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxWindowPredicate.WindowFlux> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowFlux.class, "requested"
      );
      volatile boolean enableOperatorFusion;
      int produced;
      boolean deferred;

      WindowFlux(Queue<T> queue, FluxWindowPredicate.WindowPredicateMain<T> parent) {
         this.queue = queue;
         this.parent = parent;
         this.deferred = true;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      void propagateTerminate() {
         FluxWindowPredicate.WindowPredicateMain<T> r = this.parent;
         if (r != null && PARENT.compareAndSet(this, r, null)) {
            r.groupTerminated();
         }

      }

      void drainRegular(Subscriber<? super T> a) {
         int missed = 1;
         Queue<T> q = this.queue;

         do {
            long r = this.requested;

            long e;
            for(e = 0L; r != e; ++e) {
               boolean d = this.done;
               T t = (T)q.poll();
               boolean empty = t == null;
               if (this.checkTerminated(d, empty, a, q)) {
                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(t);
            }

            if (r == e && this.checkTerminated(this.done, q.isEmpty(), a, q)) {
               return;
            }

            if (e != 0L) {
               FluxWindowPredicate.WindowPredicateMain<T> main = this.parent;
               if (main != null) {
                  main.s.request(e);
               }

               if (r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      void drainFused(Subscriber<? super T> a) {
         int missed = 1;
         Queue<T> q = this.queue;

         while(!this.cancelled) {
            boolean d = this.done;
            a.onNext((T)null);
            if (d) {
               this.ctx = Context.empty();
               this.actual = null;
               Throwable ex = this.error;
               if (ex != null) {
                  a.onError(ex);
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         Operators.onDiscardQueueWithClear(q, this.ctx, null);
         this.ctx = Context.empty();
         this.actual = null;
      }

      void drain() {
         Subscriber<? super T> a = this.actual;
         if (a != null) {
            if (WIP.getAndIncrement(this) != 0) {
               return;
            }

            if (this.enableOperatorFusion) {
               this.drainFused(a);
            } else {
               this.drainRegular(a);
            }
         }

      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, Queue<?> q) {
         if (this.cancelled) {
            Operators.onDiscardQueueWithClear(q, this.ctx, null);
            this.ctx = Context.empty();
            this.actual = null;
            return true;
         } else if (d && empty) {
            Throwable e = this.error;
            this.ctx = Context.empty();
            this.actual = null;
            if (e != null) {
               a.onError(e);
            } else {
               a.onComplete();
            }

            return true;
         } else {
            return false;
         }
      }

      @Override
      public void onNext(T t) {
         Subscriber<? super T> a = this.actual;
         if (!this.queue.offer(t)) {
            this.onError(
               Operators.onOperatorError(this, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.ctx)
            );
         } else {
            if (this.enableOperatorFusion) {
               if (a != null) {
                  a.onNext((T)null);
               }
            } else {
               this.drain();
            }

         }
      }

      @Override
      public void onSubscribe(Subscription s) {
      }

      @Override
      public void onError(Throwable t) {
         this.error = t;
         this.done = true;
         this.propagateTerminate();
         this.drain();
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.propagateTerminate();
         this.drain();
      }

      @Override
      public void subscribe(CoreSubscriber<? super T> actual) {
         if (this.once == 0 && ONCE.compareAndSet(this, 0, 1)) {
            actual.onSubscribe(this);
            ACTUAL.lazySet(this, actual);
            this.ctx = actual.currentContext();
            this.drain();
         } else {
            actual.onError(new IllegalStateException("This processor allows only a single Subscriber"));
         }

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
            FluxWindowPredicate.WindowPredicateMain<T> r = this.parent;
            if (r != null && PARENT.compareAndSet(this, r, null)) {
               if (FluxWindowPredicate.WindowPredicateMain.WINDOW_COUNT.decrementAndGet(r) == 0) {
                  r.cancel();
               } else {
                  r.s.request(1L);
               }
            }

            if (!this.enableOperatorFusion && WIP.getAndIncrement(this) == 0) {
               Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
            }

         }
      }

      @Nullable
      public T poll() {
         T v = (T)this.queue.poll();
         if (v != null) {
            ++this.produced;
         } else {
            int p = this.produced;
            if (p != 0) {
               this.produced = 0;
               FluxWindowPredicate.WindowPredicateMain<T> main = this.parent;
               if (main != null) {
                  main.s.request((long)p);
               }
            }
         }

         return v;
      }

      public int size() {
         return this.queue.size();
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      public void clear() {
         Operators.onDiscardQueueWithClear(this.queue, this.ctx, null);
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0) {
            this.enableOperatorFusion = true;
            return 2;
         } else {
            return 0;
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue == null ? 0 : this.queue.size();
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }

   static final class WindowPredicateMain<T> implements Fuseable.QueueSubscription<Flux<T>>, InnerOperator<T, Flux<T>> {
      final CoreSubscriber<? super Flux<T>> actual;
      final Context ctx;
      final Supplier<? extends Queue<T>> groupQueueSupplier;
      final FluxBufferPredicate.Mode mode;
      final Predicate<? super T> predicate;
      final int prefetch;
      final Queue<Flux<T>> queue;
      FluxWindowPredicate.WindowFlux<T> window;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxWindowPredicate.WindowPredicateMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowPredicateMain.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxWindowPredicate.WindowPredicateMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowPredicateMain.class, "requested"
      );
      volatile boolean done;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxWindowPredicate.WindowPredicateMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowPredicateMain.class, Throwable.class, "error"
      );
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<FluxWindowPredicate.WindowPredicateMain> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowPredicateMain.class, "cancelled"
      );
      volatile int windowCount;
      static final AtomicIntegerFieldUpdater<FluxWindowPredicate.WindowPredicateMain> WINDOW_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowPredicate.WindowPredicateMain.class, "windowCount"
      );
      Subscription s;
      volatile boolean outputFused;

      WindowPredicateMain(
         CoreSubscriber<? super Flux<T>> actual,
         Queue<Flux<T>> queue,
         Supplier<? extends Queue<T>> groupQueueSupplier,
         int prefetch,
         Predicate<? super T> predicate,
         FluxBufferPredicate.Mode mode
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.queue = queue;
         this.groupQueueSupplier = groupQueueSupplier;
         this.prefetch = prefetch;
         this.predicate = predicate;
         this.mode = mode;
         WINDOW_COUNT.lazySet(this, 2);
         this.initializeWindow();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            if (this.cancelled == 0) {
               s.request(Operators.unboundedOrPrefetch(this.prefetch));
            }
         }

      }

      void initializeWindow() {
         FluxWindowPredicate.WindowFlux<T> g = new FluxWindowPredicate.WindowFlux<>((Queue<T>)this.groupQueueSupplier.get(), this);
         this.window = g;
      }

      @Nullable
      FluxWindowPredicate.WindowFlux<T> newWindowDeferred() {
         if (this.cancelled == 0) {
            WINDOW_COUNT.getAndIncrement(this);
            FluxWindowPredicate.WindowFlux<T> g = new FluxWindowPredicate.WindowFlux<>((Queue<T>)this.groupQueueSupplier.get(), this);
            this.window = g;
            return g;
         } else {
            return null;
         }
      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.ctx);
         } else {
            FluxWindowPredicate.WindowFlux<T> g = this.window;

            boolean match;
            try {
               match = this.predicate.test(t);
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this.s, var5, t, this.ctx));
               return;
            }

            if (this.handleDeferredWindow(g, t)) {
               this.drain();
               if (this.mode == FluxBufferPredicate.Mode.UNTIL && match) {
                  if (g.cancelled) {
                     Operators.onDiscard(t, this.ctx);
                     this.s.request(1L);
                  } else {
                     g.onNext(t);
                  }

                  g.onComplete();
                  this.newWindowDeferred();
                  this.s.request(1L);
               } else if (this.mode == FluxBufferPredicate.Mode.UNTIL_CUT_BEFORE && match) {
                  g.onComplete();
                  g = this.newWindowDeferred();
                  if (g != null) {
                     g.onNext(t);
                     this.handleDeferredWindow(g, t);
                     this.drain();
                  }
               } else if (this.mode == FluxBufferPredicate.Mode.WHILE && !match) {
                  g.onComplete();
                  this.newWindowDeferred();
                  Operators.onDiscard(t, this.ctx);
                  this.s.request(1L);
               } else if (g.cancelled) {
                  Operators.onDiscard(t, this.ctx);
                  this.s.request(1L);
               } else {
                  g.onNext(t);
               }

            }
         }
      }

      boolean handleDeferredWindow(@Nullable FluxWindowPredicate.WindowFlux<T> window, T signal) {
         if (window != null && window.deferred) {
            window.deferred = false;
            if (!this.queue.offer(window)) {
               this.onError(
                  Operators.onOperatorError(
                     this, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), signal, this.ctx
                  )
               );
               return false;
            }
         }

         return true;
      }

      @Override
      public void onError(Throwable t) {
         if (Exceptions.addThrowable(ERROR, this, t)) {
            this.done = true;
            this.cleanup();
            this.drain();
         } else {
            Operators.onErrorDropped(t, this.ctx);
         }

      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.cleanup();
            FluxWindowPredicate.WindowFlux<T> g = this.window;
            if (g != null) {
               g.onComplete();
            }

            this.window = null;
            this.done = true;
            WINDOW_COUNT.decrementAndGet(this);
            this.drain();
         }
      }

      void cleanup() {
         if (this.predicate instanceof Disposable) {
            ((Disposable)this.predicate).dispose();
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return this.window == null ? Stream.empty() : Stream.of(this.window);
      }

      @Override
      public CoreSubscriber<? super Flux<T>> actual() {
         return this.actual;
      }

      void signalAsyncError() {
         Throwable e = Exceptions.terminate(ERROR, this);
         this.windowCount = 0;
         FluxWindowPredicate.WindowFlux<T> g = this.window;
         if (g != null) {
            g.onError(e);
         }

         this.actual.onError(e);
         this.window = null;
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
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            if (WINDOW_COUNT.decrementAndGet(this) == 0) {
               this.s.cancel();
               this.cleanup();
            } else if (!this.outputFused && WIP.getAndIncrement(this) == 0) {
               FluxWindowPredicate.WindowFlux<T> w = this.window;

               Flux<T> g;
               while((g = (Flux)this.queue.poll()) != null) {
                  ((FluxWindowPredicate.WindowFlux)g).cancel();
               }

               if (w != null && w.deferred) {
                  w.cancel();
               }

               if (WIP.decrementAndGet(this) == 0) {
                  if (!this.done && WINDOW_COUNT.get(this) == 0) {
                     this.s.cancel();
                     this.cleanup();
                  } else {
                     CANCELLED.set(this, 2);
                  }

                  return;
               }

               CANCELLED.set(this, 2);
               this.drainLoop();
            }
         } else if (CANCELLED.get(this) == 2 && WINDOW_COUNT.get(this) == 0) {
            this.s.cancel();
            this.cleanup();
         }

      }

      void groupTerminated() {
         if (this.windowCount != 0) {
            this.window = null;
            if (WINDOW_COUNT.decrementAndGet(this) == 0) {
               this.s.cancel();
               this.cleanup();
            }

         }
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            if (this.outputFused) {
               this.drainFused();
            } else {
               this.drainLoop();
            }

         }
      }

      void drainFused() {
         int missed = 1;
         Subscriber<? super Flux<T>> a = this.actual;
         Queue<Flux<T>> q = this.queue;

         while(this.cancelled == 0) {
            boolean d = this.done;
            a.onNext(null);
            if (d) {
               Throwable ex = this.error;
               if (ex != null) {
                  this.signalAsyncError();
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         q.clear();
      }

      void drainLoop() {
         int missed = 1;
         Subscriber<? super Flux<T>> a = this.actual;
         Queue<Flux<T>> q = this.queue;

         do {
            long r = this.requested;

            long e;
            for(e = 0L; e != r; ++e) {
               boolean d = this.done;
               Flux<T> v = (Flux)q.poll();
               boolean empty = v == null;
               if (this.checkTerminated(d, empty, a, q)) {
                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(v);
            }

            if (e == r && this.checkTerminated(this.done, q.isEmpty(), a, q)) {
               return;
            }

            if (e != 0L) {
               this.s.request(e);
               if (r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, Queue<Flux<T>> q) {
         if (this.cancelled != 0) {
            q.clear();
            return true;
         } else {
            if (d) {
               Throwable e = this.error;
               if (e != null && e != Exceptions.TERMINATED) {
                  this.queue.clear();
                  this.signalAsyncError();
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

      @Nullable
      public Flux<T> poll() {
         return (Flux<T>)this.queue.poll();
      }

      public int size() {
         return this.queue.size();
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      public void clear() {
         this.queue.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0) {
            this.outputFused = true;
            return 2;
         } else {
            return 0;
         }
      }
   }
}
