package reactor.core.publisher;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxWindow<T> extends InternalFluxOperator<T, Flux<T>> {
   final int size;
   final int skip;
   final Supplier<? extends Queue<T>> processorQueueSupplier;
   final Supplier<? extends Queue<Sinks.Many<T>>> overflowQueueSupplier;

   FluxWindow(Flux<? extends T> source, int size, Supplier<? extends Queue<T>> processorQueueSupplier) {
      super(source);
      if (size <= 0) {
         throw new IllegalArgumentException("size > 0 required but it was " + size);
      } else {
         this.size = size;
         this.skip = size;
         this.processorQueueSupplier = (Supplier)Objects.requireNonNull(processorQueueSupplier, "processorQueueSupplier");
         this.overflowQueueSupplier = null;
      }
   }

   FluxWindow(
      Flux<? extends T> source,
      int size,
      int skip,
      Supplier<? extends Queue<T>> processorQueueSupplier,
      Supplier<? extends Queue<Sinks.Many<T>>> overflowQueueSupplier
   ) {
      super(source);
      if (size <= 0) {
         throw new IllegalArgumentException("size > 0 required but it was " + size);
      } else if (skip <= 0) {
         throw new IllegalArgumentException("skip > 0 required but it was " + skip);
      } else {
         this.size = size;
         this.skip = skip;
         this.processorQueueSupplier = (Supplier)Objects.requireNonNull(processorQueueSupplier, "processorQueueSupplier");
         this.overflowQueueSupplier = (Supplier)Objects.requireNonNull(overflowQueueSupplier, "overflowQueueSupplier");
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Flux<T>> actual) {
      if (this.skip == this.size) {
         return new FluxWindow.WindowExactSubscriber<>(actual, this.size, this.processorQueueSupplier);
      } else {
         return (CoreSubscriber<? super T>)(this.skip > this.size
            ? new FluxWindow.WindowSkipSubscriber<>(actual, this.size, this.skip, this.processorQueueSupplier)
            : new FluxWindow.WindowOverlapSubscriber<>(
               actual, this.size, this.skip, this.processorQueueSupplier, (Queue<Sinks.Many<? super T>>)this.overflowQueueSupplier.get()
            ));
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class WindowExactSubscriber<T> implements Disposable, InnerOperator<T, Flux<T>> {
      final CoreSubscriber<? super Flux<T>> actual;
      final Supplier<? extends Queue<T>> processorQueueSupplier;
      final int size;
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowExactSubscriber> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowExactSubscriber.class, "cancelled"
      );
      volatile int windowCount;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowExactSubscriber> WINDOW_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowExactSubscriber.class, "windowCount"
      );
      int index;
      Subscription s;
      Sinks.Many<T> window;
      boolean done;

      WindowExactSubscriber(CoreSubscriber<? super Flux<T>> actual, int size, Supplier<? extends Queue<T>> processorQueueSupplier) {
         this.actual = actual;
         this.size = size;
         this.processorQueueSupplier = processorQueueSupplier;
         WINDOW_COUNT.lazySet(this, 1);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            int i = this.index;
            Sinks.Many<T> w = this.window;
            if (this.cancelled == 0 && i == 0) {
               WINDOW_COUNT.getAndIncrement(this);
               w = Sinks.unsafe().many().unicast().onBackpressureBuffer((Queue<T>)this.processorQueueSupplier.get(), this);
               this.window = w;
               this.actual.onNext(w.asFlux());
            }

            ++i;
            w.emitNext(t, Sinks.EmitFailureHandler.FAIL_FAST);
            if (i == this.size) {
               this.index = 0;
               this.window = null;
               w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            } else {
               this.index = i;
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            Sinks.Many<T> w = this.window;
            if (w != null) {
               this.window = null;
               w.emitError(t, Sinks.EmitFailureHandler.FAIL_FAST);
            }

            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            Sinks.Many<T> w = this.window;
            if (w != null) {
               this.window = null;
               w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            }

            this.actual.onComplete();
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            long u = Operators.multiplyCap((long)this.size, n);
            this.s.request(u);
         }

      }

      @Override
      public void cancel() {
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            this.dispose();
         }

      }

      @Override
      public void dispose() {
         if (WINDOW_COUNT.decrementAndGet(this) == 0) {
            this.s.cancel();
         }

      }

      @Override
      public CoreSubscriber<? super Flux<T>> actual() {
         return this.actual;
      }

      @Override
      public boolean isDisposed() {
         return this.cancelled == 1 || this.done;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.size;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.window));
      }
   }

   static final class WindowOverlapSubscriber<T> extends ArrayDeque<Sinks.Many<T>> implements Disposable, InnerOperator<T, Flux<T>> {
      final CoreSubscriber<? super Flux<T>> actual;
      final Supplier<? extends Queue<T>> processorQueueSupplier;
      final Queue<Sinks.Many<T>> queue;
      final int size;
      final int skip;
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowOverlapSubscriber> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowOverlapSubscriber.class, "cancelled"
      );
      volatile int windowCount;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowOverlapSubscriber> WINDOW_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowOverlapSubscriber.class, "windowCount"
      );
      volatile int firstRequest;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowOverlapSubscriber> FIRST_REQUEST = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowOverlapSubscriber.class, "firstRequest"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxWindow.WindowOverlapSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxWindow.WindowOverlapSubscriber.class, "requested"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowOverlapSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowOverlapSubscriber.class, "wip"
      );
      int index;
      int produced;
      Subscription s;
      volatile boolean done;
      Throwable error;

      WindowOverlapSubscriber(
         CoreSubscriber<? super Flux<T>> actual, int size, int skip, Supplier<? extends Queue<T>> processorQueueSupplier, Queue<Sinks.Many<T>> overflowQueue
      ) {
         this.actual = actual;
         this.size = size;
         this.skip = skip;
         this.processorQueueSupplier = processorQueueSupplier;
         WINDOW_COUNT.lazySet(this, 1);
         this.queue = overflowQueue;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            int i = this.index;
            if (i == 0 && this.cancelled == 0) {
               WINDOW_COUNT.getAndIncrement(this);
               Sinks.Many<T> w = Sinks.unsafe().many().unicast().onBackpressureBuffer((Queue<T>)this.processorQueueSupplier.get(), this);
               this.offer(w);
               this.queue.offer(w);
               this.drain();
            }

            ++i;

            for(Sinks.Many<T> w : this) {
               w.emitNext(t, Sinks.EmitFailureHandler.FAIL_FAST);
            }

            int p = this.produced + 1;
            if (p == this.size) {
               this.produced = p - this.skip;
               Sinks.Many<T> w = (Sinks.Many)this.poll();
               if (w != null) {
                  w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
               }
            } else {
               this.produced = p;
            }

            if (i == this.skip) {
               this.index = 0;
            } else {
               this.index = i;
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;

            for(Sinks.Many<T> w : this) {
               w.emitError(t, Sinks.EmitFailureHandler.FAIL_FAST);
            }

            this.clear();
            this.error = t;
            this.drain();
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;

            for(Sinks.Many<T> w : this) {
               w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            }

            this.clear();
            this.drain();
         }
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Subscriber<? super Flux<T>> a = this.actual;
            Queue<Sinks.Many<T>> q = this.queue;
            int missed = 1;

            do {
               long r = this.requested;

               long e;
               for(e = 0L; e != r; ++e) {
                  boolean d = this.done;
                  Sinks.Many<T> t = (Sinks.Many)q.poll();
                  boolean empty = t == null;
                  if (this.checkTerminated(d, empty, a, q)) {
                     return;
                  }

                  if (empty) {
                     break;
                  }

                  a.onNext(t.asFlux());
               }

               if (e == r && this.checkTerminated(this.done, q.isEmpty(), a, q)) {
                  return;
               }

               if (e != 0L && r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }

               missed = WIP.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, Queue<?> q) {
         if (this.cancelled == 1) {
            q.clear();
            return true;
         } else {
            if (d) {
               Throwable e = this.error;
               if (e != null) {
                  q.clear();
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

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            if (this.firstRequest == 0 && FIRST_REQUEST.compareAndSet(this, 0, 1)) {
               long u = Operators.multiplyCap((long)this.skip, n - 1L);
               long v = Operators.addCap((long)this.size, u);
               this.s.request(v);
            } else {
               long u = Operators.multiplyCap((long)this.skip, n);
               this.s.request(u);
            }

            this.drain();
         }

      }

      @Override
      public void cancel() {
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            this.dispose();
         }

      }

      @Override
      public void dispose() {
         if (WINDOW_COUNT.decrementAndGet(this) == 0) {
            this.s.cancel();
         }

      }

      @Override
      public CoreSubscriber<? super Flux<T>> actual() {
         return this.actual;
      }

      @Override
      public boolean isDisposed() {
         return this.cancelled == 1 || this.done;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.size;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.LARGE_BUFFERED) {
            return (long)this.queue.size() + (long)this.size();
         } else if (key == Scannable.Attr.BUFFERED) {
            long realBuffered = (long)this.queue.size() + (long)this.size();
            return realBuffered < 2147483647L ? (int)realBuffered : Integer.MIN_VALUE;
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
         return Stream.of(this.toArray()).map(Scannable::from);
      }
   }

   static final class WindowSkipSubscriber<T> implements Disposable, InnerOperator<T, Flux<T>> {
      final CoreSubscriber<? super Flux<T>> actual;
      final Context ctx;
      final Supplier<? extends Queue<T>> processorQueueSupplier;
      final int size;
      final int skip;
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowSkipSubscriber> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowSkipSubscriber.class, "cancelled"
      );
      volatile int windowCount;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowSkipSubscriber> WINDOW_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowSkipSubscriber.class, "windowCount"
      );
      volatile int firstRequest;
      static final AtomicIntegerFieldUpdater<FluxWindow.WindowSkipSubscriber> FIRST_REQUEST = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindow.WindowSkipSubscriber.class, "firstRequest"
      );
      int index;
      Subscription s;
      Sinks.Many<T> window;
      boolean done;

      WindowSkipSubscriber(CoreSubscriber<? super Flux<T>> actual, int size, int skip, Supplier<? extends Queue<T>> processorQueueSupplier) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.size = size;
         this.skip = skip;
         this.processorQueueSupplier = processorQueueSupplier;
         WINDOW_COUNT.lazySet(this, 1);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.ctx);
         } else {
            int i = this.index;
            Sinks.Many<T> w = this.window;
            if (i == 0) {
               WINDOW_COUNT.getAndIncrement(this);
               w = Sinks.unsafe().many().unicast().onBackpressureBuffer((Queue<T>)this.processorQueueSupplier.get(), this);
               this.window = w;
               this.actual.onNext(w.asFlux());
            }

            ++i;
            if (w != null) {
               w.emitNext(t, Sinks.EmitFailureHandler.FAIL_FAST);
            } else {
               Operators.onDiscard(t, this.ctx);
            }

            if (i == this.size) {
               this.window = null;
               if (w != null) {
                  w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
               }
            }

            if (i == this.skip) {
               this.index = 0;
            } else {
               this.index = i;
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.done = true;
            Sinks.Many<T> w = this.window;
            if (w != null) {
               this.window = null;
               w.emitError(t, Sinks.EmitFailureHandler.FAIL_FAST);
            }

            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            Sinks.Many<T> w = this.window;
            if (w != null) {
               this.window = null;
               w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            }

            this.actual.onComplete();
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            if (this.firstRequest == 0 && FIRST_REQUEST.compareAndSet(this, 0, 1)) {
               long u = Operators.multiplyCap((long)this.size, n);
               long v = Operators.multiplyCap((long)(this.skip - this.size), n - 1L);
               long w = Operators.addCap(u, v);
               this.s.request(w);
            } else {
               long u = Operators.multiplyCap((long)this.skip, n);
               this.s.request(u);
            }
         }

      }

      @Override
      public void cancel() {
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            this.dispose();
         }

      }

      @Override
      public boolean isDisposed() {
         return this.cancelled == 1 || this.done;
      }

      @Override
      public void dispose() {
         if (WINDOW_COUNT.decrementAndGet(this) == 0) {
            this.s.cancel();
         }

      }

      @Override
      public CoreSubscriber<? super Flux<T>> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.size;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.window));
      }
   }
}
