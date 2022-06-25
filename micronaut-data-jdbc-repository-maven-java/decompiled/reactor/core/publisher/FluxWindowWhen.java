package reactor.core.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;

final class FluxWindowWhen<T, U, V> extends InternalFluxOperator<T, Flux<T>> {
   final Publisher<U> start;
   final Function<? super U, ? extends Publisher<V>> end;
   final Supplier<? extends Queue<T>> processorQueueSupplier;

   FluxWindowWhen(
      Flux<? extends T> source, Publisher<U> start, Function<? super U, ? extends Publisher<V>> end, Supplier<? extends Queue<T>> processorQueueSupplier
   ) {
      super(source);
      this.start = (Publisher)Objects.requireNonNull(start, "start");
      this.end = (Function)Objects.requireNonNull(end, "end");
      this.processorQueueSupplier = (Supplier)Objects.requireNonNull(processorQueueSupplier, "processorQueueSupplier");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Nullable
   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Flux<T>> actual) {
      FluxWindowWhen.WindowWhenMainSubscriber<T, U, V> main = new FluxWindowWhen.WindowWhenMainSubscriber<>(
         actual, this.start, this.end, this.processorQueueSupplier
      );
      actual.onSubscribe(main);
      if (main.cancelled) {
         return null;
      } else {
         FluxWindowWhen.WindowWhenOpenSubscriber<T, U> os = new FluxWindowWhen.WindowWhenOpenSubscriber<>(main);
         if (FluxWindowWhen.WindowWhenMainSubscriber.BOUNDARY.compareAndSet(main, null, os)) {
            FluxWindowWhen.WindowWhenMainSubscriber.OPEN_WINDOW_COUNT.incrementAndGet(main);
            this.start.subscribe(os);
            return main;
         } else {
            return null;
         }
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class WindowOperation<T, U> {
      final Sinks.Many<T> w;
      final U open;

      WindowOperation(@Nullable Sinks.Many<T> w, @Nullable U open) {
         this.w = w;
         this.open = open;
      }
   }

   static final class WindowWhenCloseSubscriber<T, V> implements Disposable, Subscriber<V> {
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<FluxWindowWhen.WindowWhenCloseSubscriber, Subscription> SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowWhen.WindowWhenCloseSubscriber.class, Subscription.class, "subscription"
      );
      final FluxWindowWhen.WindowWhenMainSubscriber<T, ?, V> parent;
      final Sinks.Many<T> w;
      boolean done;

      WindowWhenCloseSubscriber(FluxWindowWhen.WindowWhenMainSubscriber<T, ?, V> parent, Sinks.Many<T> w) {
         this.parent = parent;
         this.w = w;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            this.subscription.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void dispose() {
         Operators.terminate(SUBSCRIPTION, this);
      }

      @Override
      public boolean isDisposed() {
         return this.subscription == Operators.cancelledSubscription();
      }

      @Override
      public void onNext(V t) {
         if (!this.done) {
            this.done = true;
            this.dispose();
            this.parent.close(this);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.parent.actual.currentContext());
         } else {
            this.done = true;
            this.parent.error(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.parent.close(this);
         }
      }
   }

   static final class WindowWhenMainSubscriber<T, U, V> extends QueueDrainSubscriber<T, Object, Flux<T>> {
      final Publisher<U> open;
      final Function<? super U, ? extends Publisher<V>> close;
      final Supplier<? extends Queue<T>> processorQueueSupplier;
      final Disposable.Composite resources;
      Subscription s;
      volatile Disposable boundary;
      static final AtomicReferenceFieldUpdater<FluxWindowWhen.WindowWhenMainSubscriber, Disposable> BOUNDARY = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowWhen.WindowWhenMainSubscriber.class, Disposable.class, "boundary"
      );
      final List<Sinks.Many<T>> windows;
      volatile long openWindowCount;
      static final AtomicLongFieldUpdater<FluxWindowWhen.WindowWhenMainSubscriber> OPEN_WINDOW_COUNT = AtomicLongFieldUpdater.newUpdater(
         FluxWindowWhen.WindowWhenMainSubscriber.class, "openWindowCount"
      );

      WindowWhenMainSubscriber(
         CoreSubscriber<? super Flux<T>> actual,
         Publisher<U> open,
         Function<? super U, ? extends Publisher<V>> close,
         Supplier<? extends Queue<T>> processorQueueSupplier
      ) {
         super(actual, (Queue<Object>)Queues.unboundedMultiproducer().get());
         this.open = open;
         this.close = close;
         this.processorQueueSupplier = processorQueueSupplier;
         this.resources = Disposables.composite();
         this.windows = new ArrayList();
         OPEN_WINDOW_COUNT.lazySet(this, 1L);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            if (this.fastEnter()) {
               for(Sinks.Many<T> w : this.windows) {
                  w.emitNext(t, Sinks.EmitFailureHandler.FAIL_FAST);
               }

               if (this.leave(-1) == 0) {
                  return;
               }
            } else {
               this.queue.offer(t);
               if (!this.enter()) {
                  return;
               }
            }

            this.drainLoop();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.error = t;
            this.done = true;
            if (this.enter()) {
               this.drainLoop();
            }

            if (OPEN_WINDOW_COUNT.decrementAndGet(this) == 0L) {
               this.resources.dispose();
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            if (this.enter()) {
               this.drainLoop();
            }

            if (OPEN_WINDOW_COUNT.decrementAndGet(this) == 0L) {
               this.resources.dispose();
            }

         }
      }

      void error(Throwable t) {
         this.s.cancel();
         this.resources.dispose();
         OperatorDisposables.dispose(BOUNDARY, this);
         this.actual.onError(t);
      }

      @Override
      public void request(long n) {
         this.requested(n);
      }

      @Override
      public void cancel() {
         this.cancelled = true;
      }

      void dispose() {
         this.resources.dispose();
         OperatorDisposables.dispose(BOUNDARY, this);
      }

      void drainLoop() {
         Queue<Object> q = this.queue;
         Subscriber<? super Flux<T>> a = this.actual;
         List<Sinks.Many<T>> ws = this.windows;
         int missed = 1;

         while(true) {
            boolean d = this.done;
            Object o = q.poll();
            boolean empty = o == null;
            if (d && empty) {
               this.dispose();
               Throwable e = this.error;
               if (e != null) {
                  this.actual.onError(e);

                  for(Sinks.Many<T> w : ws) {
                     w.emitError(e, Sinks.EmitFailureHandler.FAIL_FAST);
                  }
               } else {
                  this.actual.onComplete();

                  for(Sinks.Many<T> w : ws) {
                     w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                  }
               }

               ws.clear();
               return;
            }

            if (empty) {
               missed = this.leave(-missed);
               if (missed == 0) {
                  return;
               }
            } else if (o instanceof FluxWindowWhen.WindowOperation) {
               FluxWindowWhen.WindowOperation<T, U> wo = (FluxWindowWhen.WindowOperation)o;
               Sinks.Many<T> w = wo.w;
               if (w != null) {
                  if (ws.remove(wo.w)) {
                     wo.w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                     if (OPEN_WINDOW_COUNT.decrementAndGet(this) == 0L) {
                        this.dispose();
                        return;
                     }
                  }
               } else if (!this.cancelled) {
                  w = Sinks.unsafe().many().unicast().onBackpressureBuffer((Queue<T>)this.processorQueueSupplier.get());
                  long r = this.requested();
                  if (r != 0L) {
                     ws.add(w);
                     a.onNext(w.asFlux());
                     if (r != Long.MAX_VALUE) {
                        this.produced(1L);
                     }

                     Publisher<V> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.close.apply(wo.open), "The publisher supplied is null");
                     } catch (Throwable var14) {
                        this.cancelled = true;
                        a.onError(var14);
                        continue;
                     }

                     FluxWindowWhen.WindowWhenCloseSubscriber<T, V> cl = new FluxWindowWhen.WindowWhenCloseSubscriber<>(this, w);
                     if (this.resources.add(cl)) {
                        OPEN_WINDOW_COUNT.getAndIncrement(this);
                        p.subscribe(cl);
                     }
                  } else {
                     this.cancelled = true;
                     a.onError(Exceptions.failWithOverflow("Could not deliver new window due to lack of requests"));
                  }
               }
            } else {
               for(Sinks.Many<T> w : ws) {
                  w.emitNext((T)o, Sinks.EmitFailureHandler.FAIL_FAST);
               }
            }
         }
      }

      void open(U b) {
         this.queue.offer(new FluxWindowWhen.WindowOperation(null, b));
         if (this.enter()) {
            this.drainLoop();
         }

      }

      void close(FluxWindowWhen.WindowWhenCloseSubscriber<T, V> w) {
         this.resources.remove(w);
         this.queue.offer(new FluxWindowWhen.WindowOperation<>(w.w, (U)null));
         if (this.enter()) {
            this.drainLoop();
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   static final class WindowWhenOpenSubscriber<T, U> implements Disposable, Subscriber<U> {
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<FluxWindowWhen.WindowWhenOpenSubscriber, Subscription> SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowWhen.WindowWhenOpenSubscriber.class, Subscription.class, "subscription"
      );
      final FluxWindowWhen.WindowWhenMainSubscriber<T, U, ?> parent;
      boolean done;

      WindowWhenOpenSubscriber(FluxWindowWhen.WindowWhenMainSubscriber<T, U, ?> parent) {
         this.parent = parent;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            this.subscription.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void dispose() {
         Operators.terminate(SUBSCRIPTION, this);
      }

      @Override
      public boolean isDisposed() {
         return this.subscription == Operators.cancelledSubscription();
      }

      @Override
      public void onNext(U t) {
         if (!this.done) {
            this.parent.open(t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.parent.actual.currentContext());
         } else {
            this.done = true;
            this.parent.error(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.parent.onComplete();
         }
      }
   }
}
