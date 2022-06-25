package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.concurrent.Queues;

final class FluxWindowTimeout<T> extends InternalFluxOperator<T, Flux<T>> {
   final int maxSize;
   final long timespan;
   final TimeUnit unit;
   final Scheduler timer;

   FluxWindowTimeout(Flux<T> source, int maxSize, long timespan, TimeUnit unit, Scheduler timer) {
      super(source);
      if (timespan <= 0L) {
         throw new IllegalArgumentException("Timeout period must be strictly positive");
      } else if (maxSize <= 0) {
         throw new IllegalArgumentException("maxSize must be strictly positive");
      } else {
         this.timer = (Scheduler)Objects.requireNonNull(timer, "Timer");
         this.timespan = timespan;
         this.unit = (TimeUnit)Objects.requireNonNull(unit, "unit");
         this.maxSize = maxSize;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Flux<T>> actual) {
      return new FluxWindowTimeout.WindowTimeoutSubscriber<>(actual, this.maxSize, this.timespan, this.unit, this.timer);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.timer;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class WindowTimeoutSubscriber<T> implements InnerOperator<T, Flux<T>> {
      final CoreSubscriber<? super Flux<T>> actual;
      final long timespan;
      final TimeUnit unit;
      final Scheduler scheduler;
      final int maxSize;
      final Scheduler.Worker worker;
      final Queue<Object> queue;
      Throwable error;
      volatile boolean done;
      volatile boolean cancelled;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxWindowTimeout.WindowTimeoutSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxWindowTimeout.WindowTimeoutSubscriber.class, "requested"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxWindowTimeout.WindowTimeoutSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxWindowTimeout.WindowTimeoutSubscriber.class, "wip"
      );
      int count;
      long producerIndex;
      Subscription s;
      Sinks.Many<T> window;
      volatile boolean terminated;
      volatile Disposable timer;
      static final AtomicReferenceFieldUpdater<FluxWindowTimeout.WindowTimeoutSubscriber, Disposable> TIMER = AtomicReferenceFieldUpdater.newUpdater(
         FluxWindowTimeout.WindowTimeoutSubscriber.class, Disposable.class, "timer"
      );

      WindowTimeoutSubscriber(CoreSubscriber<? super Flux<T>> actual, int maxSize, long timespan, TimeUnit unit, Scheduler scheduler) {
         this.actual = actual;
         this.queue = (Queue)Queues.unboundedMultiproducer().get();
         this.timespan = timespan;
         this.unit = unit;
         this.scheduler = scheduler;
         this.maxSize = maxSize;
         this.worker = scheduler.createWorker();
      }

      @Override
      public CoreSubscriber<? super Flux<T>> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         Sinks.Many<T> w = this.window;
         return w == null ? Stream.empty() : Stream.of(Scannable.from(w));
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.maxSize;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.worker;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            Subscriber<? super Flux<T>> a = this.actual;
            a.onSubscribe(this);
            if (this.cancelled) {
               return;
            }

            Sinks.Many<T> w = Sinks.unsafe().many().unicast().onBackpressureBuffer();
            this.window = w;
            long r = this.requested;
            if (r == 0L) {
               a.onError(Operators.onOperatorError(s, Exceptions.failWithOverflow(), this.actual.currentContext()));
               return;
            }

            a.onNext(w.asFlux());
            if (r != Long.MAX_VALUE) {
               REQUESTED.decrementAndGet(this);
            }

            if (OperatorDisposables.replace(TIMER, this, this.newPeriod())) {
               s.request(Long.MAX_VALUE);
            }
         }

      }

      Disposable newPeriod() {
         try {
            return this.worker
               .schedulePeriodically(
                  new FluxWindowTimeout.WindowTimeoutSubscriber.ConsumerIndexHolder(this.producerIndex, this), this.timespan, this.timespan, this.unit
               );
         } catch (Exception var2) {
            this.actual.onError(Operators.onRejectedExecution(var2, this.s, null, null, this.actual.currentContext()));
            return Disposables.disposed();
         }
      }

      @Override
      public void onNext(T t) {
         if (!this.terminated) {
            if (WIP.get(this) == 0 && WIP.compareAndSet(this, 0, 1)) {
               Sinks.Many<T> w = this.window;
               w.emitNext(t, Sinks.EmitFailureHandler.FAIL_FAST);
               int c = this.count + 1;
               if (c >= this.maxSize) {
                  ++this.producerIndex;
                  this.count = 0;
                  w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                  long r = this.requested;
                  if (r == 0L) {
                     this.window = null;
                     this.actual.onError(Operators.onOperatorError(this.s, Exceptions.failWithOverflow(), t, this.actual.currentContext()));
                     this.timer.dispose();
                     this.worker.dispose();
                     return;
                  }

                  w = Sinks.unsafe().many().unicast().onBackpressureBuffer();
                  this.window = w;
                  this.actual.onNext(w.asFlux());
                  if (r != Long.MAX_VALUE) {
                     REQUESTED.decrementAndGet(this);
                  }

                  Disposable tm = this.timer;
                  tm.dispose();
                  Disposable task = this.newPeriod();
                  if (!TIMER.compareAndSet(this, tm, task)) {
                     task.dispose();
                  }
               } else {
                  this.count = c;
               }

               if (WIP.decrementAndGet(this) == 0) {
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
         this.error = t;
         this.done = true;
         if (this.enter()) {
            this.drainLoop();
         }

         this.actual.onError(t);
         this.timer.dispose();
         this.worker.dispose();
      }

      @Override
      public void onComplete() {
         this.done = true;
         if (this.enter()) {
            this.drainLoop();
         }

         this.actual.onComplete();
         this.timer.dispose();
         this.worker.dispose();
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         this.cancelled = true;
      }

      void drainLoop() {
         Queue<Object> q = this.queue;
         Subscriber<? super Flux<T>> a = this.actual;
         Sinks.Many<T> w = this.window;
         int missed = 1;

         while(!this.terminated) {
            boolean d = this.done;
            Object o = q.poll();
            boolean empty = o == null;
            boolean isHolder = o instanceof FluxWindowTimeout.WindowTimeoutSubscriber.ConsumerIndexHolder;
            if (d && (empty || isHolder)) {
               this.window = null;
               q.clear();
               Throwable err = this.error;
               if (err != null) {
                  w.emitError(err, Sinks.EmitFailureHandler.FAIL_FAST);
               } else {
                  w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
               }

               this.timer.dispose();
               this.worker.dispose();
               return;
            }

            if (empty) {
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            } else if (isHolder) {
               w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
               this.count = 0;
               w = Sinks.unsafe().many().unicast().onBackpressureBuffer();
               this.window = w;
               long r = this.requested;
               if (r == 0L) {
                  this.window = null;
                  this.queue.clear();
                  a.onError(Operators.onOperatorError(this.s, Exceptions.failWithOverflow(), this.actual.currentContext()));
                  this.timer.dispose();
                  this.worker.dispose();
                  return;
               }

               a.onNext(w.asFlux());
               if (r != Long.MAX_VALUE) {
                  REQUESTED.decrementAndGet(this);
               }
            } else {
               w.emitNext((T)o, Sinks.EmitFailureHandler.FAIL_FAST);
               int c = this.count + 1;
               if (c >= this.maxSize) {
                  ++this.producerIndex;
                  this.count = 0;
                  w.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                  long r = this.requested;
                  if (r == 0L) {
                     this.window = null;
                     a.onError(Operators.onOperatorError(this.s, Exceptions.failWithOverflow(), o, this.actual.currentContext()));
                     this.timer.dispose();
                     this.worker.dispose();
                     return;
                  }

                  w = Sinks.unsafe().many().unicast().onBackpressureBuffer();
                  this.window = w;
                  this.actual.onNext(w.asFlux());
                  if (r != Long.MAX_VALUE) {
                     REQUESTED.decrementAndGet(this);
                  }

                  Disposable tm = this.timer;
                  tm.dispose();
                  Disposable task = this.newPeriod();
                  if (!TIMER.compareAndSet(this, tm, task)) {
                     task.dispose();
                  }
               } else {
                  this.count = c;
               }
            }
         }

         this.s.cancel();
         q.clear();
         this.timer.dispose();
         this.worker.dispose();
      }

      boolean enter() {
         return WIP.getAndIncrement(this) == 0;
      }

      static final class ConsumerIndexHolder implements Runnable {
         final long index;
         final FluxWindowTimeout.WindowTimeoutSubscriber<?> parent;

         ConsumerIndexHolder(long index, FluxWindowTimeout.WindowTimeoutSubscriber<?> parent) {
            this.index = index;
            this.parent = parent;
         }

         public void run() {
            FluxWindowTimeout.WindowTimeoutSubscriber<?> p = this.parent;
            if (!p.cancelled) {
               p.queue.offer(this);
            } else {
               p.terminated = true;
               p.timer.dispose();
               p.worker.dispose();
            }

            if (p.enter()) {
               p.drainLoop();
            }

         }
      }
   }
}
