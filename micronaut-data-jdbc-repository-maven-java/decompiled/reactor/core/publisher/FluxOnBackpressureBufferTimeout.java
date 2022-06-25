package reactor.core.publisher;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxOnBackpressureBufferTimeout<O> extends InternalFluxOperator<O, O> {
   private static final Logger LOGGER = Loggers.getLogger(FluxOnBackpressureBufferTimeout.class);
   final Duration ttl;
   final Scheduler ttlScheduler;
   final int bufferSize;
   final Consumer<? super O> onBufferEviction;

   FluxOnBackpressureBufferTimeout(Flux<? extends O> source, Duration ttl, Scheduler ttlScheduler, int bufferSize, Consumer<? super O> onBufferEviction) {
      super(source);
      this.ttl = ttl;
      this.ttlScheduler = ttlScheduler;
      this.bufferSize = bufferSize;
      this.onBufferEviction = onBufferEviction;
   }

   @Override
   public CoreSubscriber<? super O> subscribeOrReturn(CoreSubscriber<? super O> actual) {
      return new FluxOnBackpressureBufferTimeout.BackpressureBufferTimeoutSubscriber<>(
         actual, this.ttl, this.ttlScheduler, this.bufferSize, this.onBufferEviction
      );
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.ttlScheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class BackpressureBufferTimeoutSubscriber<T> extends ArrayDeque<Object> implements InnerOperator<T, T>, Runnable {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final Duration ttl;
      final Scheduler ttlScheduler;
      final Scheduler.Worker worker;
      final int bufferSizeDouble;
      final Consumer<? super T> onBufferEviction;
      Subscription s;
      volatile boolean cancelled;
      volatile boolean done;
      Throwable error;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxOnBackpressureBufferTimeout.BackpressureBufferTimeoutSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxOnBackpressureBufferTimeout.BackpressureBufferTimeoutSubscriber.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxOnBackpressureBufferTimeout.BackpressureBufferTimeoutSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxOnBackpressureBufferTimeout.BackpressureBufferTimeoutSubscriber.class, "requested"
      );

      BackpressureBufferTimeoutSubscriber(
         CoreSubscriber<? super T> actual, Duration ttl, Scheduler ttlScheduler, int bufferSize, Consumer<? super T> onBufferEviction
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.onBufferEviction = (Consumer)Objects.requireNonNull(onBufferEviction, "buffer eviction callback must not be null");
         this.bufferSizeDouble = bufferSize << 1;
         this.ttl = ttl;
         this.ttlScheduler = (Scheduler)Objects.requireNonNull(ttlScheduler, "ttl Scheduler must not be null");
         this.worker = ttlScheduler.createWorker();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.CANCELLED) {
               return this.cancelled;
            } else if (key == Scannable.Attr.BUFFERED) {
               return this.size();
            } else if (key == Scannable.Attr.ERROR) {
               return this.error;
            } else if (key == Scannable.Attr.PREFETCH) {
               return Integer.MAX_VALUE;
            } else if (key == Scannable.Attr.DELAY_ERROR) {
               return false;
            } else if (key == Scannable.Attr.RUN_ON) {
               return this.ttlScheduler;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return this.done && this.isEmpty();
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
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
         this.cancelled = true;
         this.s.cancel();
         this.worker.dispose();
         if (WIP.getAndIncrement(this) == 0) {
            this.clearQueue();
         }

      }

      void clearQueue() {
         while(true) {
            T evicted;
            synchronized(this) {
               if (this.isEmpty()) {
                  return;
               }

               this.poll();
               evicted = (T)this.poll();
            }

            this.evict(evicted);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         T evicted = null;
         synchronized(this) {
            if (this.size() == this.bufferSizeDouble) {
               this.poll();
               evicted = (T)this.poll();
            }

            this.offer(this.ttlScheduler.now(TimeUnit.NANOSECONDS));
            this.offer(t);
         }

         this.evict(evicted);

         try {
            this.worker.schedule(this, this.ttl.toNanos(), TimeUnit.NANOSECONDS);
         } catch (RejectedExecutionException var5) {
            this.done = true;
            this.error = Operators.onRejectedExecution(var5, this, null, t, this.actual.currentContext());
         }

         this.drain();
      }

      @Override
      public void onError(Throwable t) {
         this.error = t;
         this.done = true;
         this.drain();
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain();
      }

      public void run() {
         while(!this.cancelled) {
            boolean d = this.done;
            T evicted = null;
            boolean empty;
            synchronized(this) {
               Long ts = (Long)this.peek();
               empty = ts == null;
               if (!empty) {
                  if (ts > this.ttlScheduler.now(TimeUnit.NANOSECONDS) - this.ttl.toNanos()) {
                     break;
                  }

                  this.poll();
                  evicted = (T)this.poll();
               }
            }

            this.evict(evicted);
            if (empty) {
               if (d) {
                  this.drain();
               }
               break;
            }
         }

      }

      void evict(@Nullable T evicted) {
         if (evicted != null) {
            try {
               this.onBufferEviction.accept(evicted);
            } catch (Throwable var3) {
               if (FluxOnBackpressureBufferTimeout.LOGGER.isDebugEnabled()) {
                  FluxOnBackpressureBufferTimeout.LOGGER
                     .debug("value [{}] couldn't be evicted due to a callback error. This error will be dropped: {}", evicted, var3);
               }

               Operators.onErrorDropped(var3, this.actual.currentContext());
            }

            Operators.onDiscard(evicted, this.actual.currentContext());
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;

            do {
               long r = this.requested;

               long e;
               for(e = 0L; e != r; ++e) {
                  if (this.cancelled) {
                     this.clearQueue();
                     return;
                  }

                  boolean d = this.done;
                  T v;
                  synchronized(this) {
                     if (this.poll() != null) {
                        v = (T)this.poll();
                     } else {
                        v = null;
                     }
                  }

                  boolean empty = v == null;
                  if (d && empty) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        this.actual.onError(ex);
                     } else {
                        this.actual.onComplete();
                     }

                     this.worker.dispose();
                     return;
                  }

                  if (empty) {
                     break;
                  }

                  this.actual.onNext(v);
               }

               if (e == r) {
                  if (this.cancelled) {
                     this.clearQueue();
                     return;
                  }

                  boolean d = this.done;
                  boolean empty;
                  synchronized(this) {
                     empty = this.isEmpty();
                  }

                  if (d && empty) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        this.actual.onError(ex);
                     } else {
                        this.actual.onComplete();
                     }

                     this.worker.dispose();
                     return;
                  }
               }

               if (e != 0L && r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }

               missed = WIP.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }
   }
}
