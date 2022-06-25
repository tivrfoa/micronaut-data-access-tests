package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class FluxWindowBoundary<T, U> extends InternalFluxOperator<T, Flux<T>> {
   final Publisher<U> other;
   final Supplier<? extends Queue<T>> processorQueueSupplier;

   FluxWindowBoundary(Flux<? extends T> source, Publisher<U> other, Supplier<? extends Queue<T>> processorQueueSupplier) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
      this.processorQueueSupplier = (Supplier)Objects.requireNonNull(processorQueueSupplier, "processorQueueSupplier");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Nullable
   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Flux<T>> actual) {
      FluxWindowBoundary.WindowBoundaryMain<T, U> main = new FluxWindowBoundary.WindowBoundaryMain<>(
         actual, this.processorQueueSupplier, (Queue<T>)this.processorQueueSupplier.get()
      );
      actual.onSubscribe(main);
      if (main.emit(main.window)) {
         this.other.subscribe(main.boundary);
         return main;
      } else {
         return null;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class WindowBoundaryMain<T, U> implements InnerOperator<T, Flux<T>>, Disposable {
      final Supplier<? extends Queue<T>> processorQueueSupplier;
      final FluxWindowBoundary.WindowBoundaryOther<U> boundary;
      final Queue<Object> queue;
      final CoreSubscriber<? super Flux<T>> actual;
      Sinks.Many<T> window;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxWindowBoundary.WindowBoundaryMain, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowBoundary.WindowBoundaryMain.class, Subscription.class, "s"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxWindowBoundary.WindowBoundaryMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxWindowBoundary.WindowBoundaryMain.class, "requested"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxWindowBoundary.WindowBoundaryMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowBoundary.WindowBoundaryMain.class, Throwable.class, "error"
      );
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<FluxWindowBoundary.WindowBoundaryMain> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowBoundary.WindowBoundaryMain.class, "cancelled"
      );
      volatile int windowCount;
      static final AtomicIntegerFieldUpdater<FluxWindowBoundary.WindowBoundaryMain> WINDOW_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowBoundary.WindowBoundaryMain.class, "windowCount"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxWindowBoundary.WindowBoundaryMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowBoundary.WindowBoundaryMain.class, "wip"
      );
      boolean done;
      static final Object BOUNDARY_MARKER = new Object();
      static final Object DONE = new Object();

      WindowBoundaryMain(CoreSubscriber<? super Flux<T>> actual, Supplier<? extends Queue<T>> processorQueueSupplier, Queue<T> processorQueue) {
         this.actual = actual;
         this.processorQueueSupplier = processorQueueSupplier;
         this.window = Sinks.unsafe().many().unicast().onBackpressureBuffer(processorQueue, this);
         WINDOW_COUNT.lazySet(this, 2);
         this.boundary = new FluxWindowBoundary.WindowBoundaryOther<>(this);
         this.queue = (Queue)Queues.unboundedMultiproducer().get();
      }

      @Override
      public final CoreSubscriber<? super Flux<T>> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.boundary, Scannable.from(this.window));
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            synchronized(this) {
               this.queue.offer(t);
            }

            this.drain();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.boundary.cancel();
            if (Exceptions.addThrowable(ERROR, this, t)) {
               this.drain();
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.boundary.cancel();
            synchronized(this) {
               this.queue.offer(DONE);
            }

            this.drain();
         }
      }

      @Override
      public void dispose() {
         if (WINDOW_COUNT.decrementAndGet(this) == 0) {
            this.cancelMain();
            this.boundary.cancel();
         }

      }

      @Override
      public boolean isDisposed() {
         return this.cancelled == 1 || this.done;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      void cancelMain() {
         Operators.terminate(S, this);
      }

      @Override
      public void cancel() {
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            this.dispose();
         }

      }

      void boundaryNext() {
         synchronized(this) {
            this.queue.offer(BOUNDARY_MARKER);
         }

         if (this.cancelled != 0) {
            this.boundary.cancel();
         }

         this.drain();
      }

      void boundaryError(Throwable e) {
         this.cancelMain();
         if (Exceptions.addThrowable(ERROR, this, e)) {
            this.drain();
         } else {
            Operators.onErrorDropped(e, this.actual.currentContext());
         }

      }

      void boundaryComplete() {
         this.cancelMain();
         synchronized(this) {
            this.queue.offer(DONE);
         }

         this.drain();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Subscriber<? super Flux<T>> a = this.actual;
            Queue<Object> q = this.queue;
            Sinks.Many<T> w = this.window;
            int missed = 1;

            while(this.error == null) {
               Object o = q.poll();
               if (o == null) {
                  missed = WIP.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               } else {
                  if (o == DONE) {
                     q.clear();
                     w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                     a.onComplete();
                     return;
                  }

                  if (o != BOUNDARY_MARKER) {
                     w.emitNext((T)o, Sinks.EmitFailureHandler.FAIL_FAST);
                  }

                  if (o == BOUNDARY_MARKER) {
                     w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                     if (this.cancelled == 0) {
                        if (this.requested == 0L) {
                           q.clear();
                           this.cancelMain();
                           this.boundary.cancel();
                           a.onError(Exceptions.failWithOverflow("Could not create new window due to lack of requests"));
                           return;
                        }

                        Queue<T> pq = (Queue)this.processorQueueSupplier.get();
                        WINDOW_COUNT.getAndIncrement(this);
                        w = Sinks.unsafe().many().unicast().onBackpressureBuffer(pq, this);
                        this.window = w;
                        a.onNext(w.asFlux());
                        if (this.requested != Long.MAX_VALUE) {
                           REQUESTED.decrementAndGet(this);
                        }
                     }
                  }
               }
            }

            q.clear();
            Throwable e = Exceptions.terminate(ERROR, this);
            if (e != Exceptions.TERMINATED) {
               w.emitError(e, Sinks.EmitFailureHandler.FAIL_FAST);
               a.onError(e);
            }

         }
      }

      boolean emit(Sinks.Many<T> w) {
         long r = this.requested;
         if (r != 0L) {
            this.actual.onNext(w.asFlux());
            if (r != Long.MAX_VALUE) {
               REQUESTED.decrementAndGet(this);
            }

            return true;
         } else {
            this.cancel();
            this.actual.onError(Exceptions.failWithOverflow("Could not emit buffer due to lack of requests"));
            return false;
         }
      }
   }

   static final class WindowBoundaryOther<U> extends Operators.DeferredSubscription implements InnerConsumer<U> {
      final FluxWindowBoundary.WindowBoundaryMain<?, U> main;

      WindowBoundaryOther(FluxWindowBoundary.WindowBoundaryMain<?, U> main) {
         this.main = main;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (this.set(s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void onNext(U t) {
         this.main.boundaryNext();
      }

      @Override
      public void onError(Throwable t) {
         this.main.boundaryError(t);
      }

      @Override
      public void onComplete() {
         this.main.boundaryComplete();
      }
   }
}
