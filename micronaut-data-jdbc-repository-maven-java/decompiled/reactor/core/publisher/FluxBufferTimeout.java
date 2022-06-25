package reactor.core.publisher;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxBufferTimeout<T, C extends Collection<? super T>> extends InternalFluxOperator<T, C> {
   final int batchSize;
   final Supplier<C> bufferSupplier;
   final Scheduler timer;
   final long timespan;
   final TimeUnit unit;

   FluxBufferTimeout(Flux<T> source, int maxSize, long timespan, TimeUnit unit, Scheduler timer, Supplier<C> bufferSupplier) {
      super(source);
      if (timespan <= 0L) {
         throw new IllegalArgumentException("Timeout period must be strictly positive");
      } else if (maxSize <= 0) {
         throw new IllegalArgumentException("maxSize must be strictly positive");
      } else {
         this.timer = (Scheduler)Objects.requireNonNull(timer, "Timer");
         this.timespan = timespan;
         this.unit = (TimeUnit)Objects.requireNonNull(unit, "unit");
         this.batchSize = maxSize;
         this.bufferSupplier = (Supplier)Objects.requireNonNull(bufferSupplier, "bufferSupplier");
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super C> actual) {
      return new FluxBufferTimeout.BufferTimeoutSubscriber<>(
         Operators.serialize(actual), this.batchSize, this.timespan, this.unit, this.timer.createWorker(), this.bufferSupplier
      );
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.timer;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class BufferTimeoutSubscriber<T, C extends Collection<? super T>> implements InnerOperator<T, C> {
      final CoreSubscriber<? super C> actual;
      static final int NOT_TERMINATED = 0;
      static final int TERMINATED_WITH_SUCCESS = 1;
      static final int TERMINATED_WITH_ERROR = 2;
      static final int TERMINATED_WITH_CANCEL = 3;
      final int batchSize;
      final long timespan;
      final TimeUnit unit;
      final Scheduler.Worker timer;
      final Runnable flushTask;
      protected Subscription subscription;
      volatile int terminated = 0;
      static final AtomicIntegerFieldUpdater<FluxBufferTimeout.BufferTimeoutSubscriber> TERMINATED = AtomicIntegerFieldUpdater.newUpdater(
         FluxBufferTimeout.BufferTimeoutSubscriber.class, "terminated"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxBufferTimeout.BufferTimeoutSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxBufferTimeout.BufferTimeoutSubscriber.class, "requested"
      );
      volatile long outstanding;
      static final AtomicLongFieldUpdater<FluxBufferTimeout.BufferTimeoutSubscriber> OUTSTANDING = AtomicLongFieldUpdater.newUpdater(
         FluxBufferTimeout.BufferTimeoutSubscriber.class, "outstanding"
      );
      volatile int index = 0;
      static final AtomicIntegerFieldUpdater<FluxBufferTimeout.BufferTimeoutSubscriber> INDEX = AtomicIntegerFieldUpdater.newUpdater(
         FluxBufferTimeout.BufferTimeoutSubscriber.class, "index"
      );
      volatile Disposable timespanRegistration;
      final Supplier<C> bufferSupplier;
      volatile C values;

      BufferTimeoutSubscriber(CoreSubscriber<? super C> actual, int maxSize, long timespan, TimeUnit unit, Scheduler.Worker timer, Supplier<C> bufferSupplier) {
         this.actual = actual;
         this.timespan = timespan;
         this.unit = unit;
         this.timer = timer;
         this.flushTask = () -> {
            if (this.terminated == 0) {
               int index;
               do {
                  index = this.index;
                  if (index == 0) {
                     return;
                  }
               } while(!INDEX.compareAndSet(this, index, 0));

               this.flushCallback((T)null);
            }

         };
         this.batchSize = maxSize;
         this.bufferSupplier = bufferSupplier;
      }

      protected void doOnSubscribe() {
         this.values = (C)this.bufferSupplier.get();
      }

      void nextCallback(T value) {
         synchronized(this) {
            if (OUTSTANDING.decrementAndGet(this) < 0L) {
               this.actual.onError(Exceptions.failWithOverflow("Unrequested element received"));
               Context ctx = this.actual.currentContext();
               Operators.onDiscard(value, ctx);
               Operators.onDiscardMultiple(this.values, ctx);
            } else {
               C v = this.values;
               if (v == null) {
                  v = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null buffer");
                  this.values = v;
               }

               v.add(value);
            }
         }
      }

      void flushCallback(@Nullable T ev) {
         boolean flush = false;
         C v;
         synchronized(this) {
            v = this.values;
            if (v != null && !v.isEmpty()) {
               this.values = (C)this.bufferSupplier.get();
               flush = true;
            }
         }

         if (flush) {
            long r = this.requested;
            if (r != 0L) {
               if (r == Long.MAX_VALUE) {
                  this.actual.onNext((T)v);
                  return;
               }

               do {
                  long next = r - 1L;
                  if (REQUESTED.compareAndSet(this, r, next)) {
                     this.actual.onNext((T)v);
                     return;
                  }

                  r = this.requested;
               } while(r > 0L);
            }

            this.cancel();
            this.actual.onError(Exceptions.failWithOverflow("Could not emit buffer due to lack of requests"));
            Operators.onDiscardMultiple(v, this.actual.currentContext());
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.subscription;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.terminated == 3;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
               return this.requested;
            } else if (key == Scannable.Attr.CAPACITY) {
               return this.batchSize;
            } else if (key == Scannable.Attr.BUFFERED) {
               return this.batchSize - this.index;
            } else if (key == Scannable.Attr.RUN_ON) {
               return this.timer;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return this.terminated == 2 || this.terminated == 1;
         }
      }

      @Override
      public void onNext(T value) {
         int index;
         do {
            index = this.index + 1;
         } while(!INDEX.compareAndSet(this, index - 1, index));

         if (index == 1) {
            try {
               this.timespanRegistration = this.timer.schedule(this.flushTask, this.timespan, this.unit);
            } catch (RejectedExecutionException var5) {
               Context ctx = this.actual.currentContext();
               this.onError(Operators.onRejectedExecution(var5, this.subscription, null, value, ctx));
               Operators.onDiscard(value, ctx);
               return;
            }
         }

         this.nextCallback(value);
         if (this.index % this.batchSize == 0) {
            this.index = 0;
            if (this.timespanRegistration != null) {
               this.timespanRegistration.dispose();
               this.timespanRegistration = null;
            }

            this.flushCallback(value);
         }

      }

      void checkedComplete() {
         try {
            this.flushCallback((T)null);
         } finally {
            this.actual.onComplete();
         }

      }

      final boolean isCompleted() {
         return this.terminated == 1;
      }

      final boolean isFailed() {
         return this.terminated == 2;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            if (this.terminated != 0) {
               return;
            }

            if (this.batchSize != Integer.MAX_VALUE && n != Long.MAX_VALUE) {
               long requestLimit = Operators.multiplyCap(this.requested, (long)this.batchSize);
               if (requestLimit > this.outstanding) {
                  this.requestMore(requestLimit - this.outstanding);
               }
            } else {
               this.requestMore(Long.MAX_VALUE);
            }
         }

      }

      final void requestMore(long n) {
         Subscription s = this.subscription;
         if (s != null) {
            Operators.addCap(OUTSTANDING, this, n);
            s.request(n);
         }

      }

      @Override
      public CoreSubscriber<? super C> actual() {
         return this.actual;
      }

      @Override
      public void onComplete() {
         if (TERMINATED.compareAndSet(this, 0, 1)) {
            this.timer.dispose();
            this.checkedComplete();
         }

      }

      @Override
      public void onError(Throwable throwable) {
         if (TERMINATED.compareAndSet(this, 0, 2)) {
            this.timer.dispose();
            Context ctx = this.actual.currentContext();
            synchronized(this) {
               C v = this.values;
               if (v != null) {
                  Operators.onDiscardMultiple(v, ctx);
                  v.clear();
                  this.values = null;
               }
            }

            this.actual.onError(throwable);
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.subscription, s)) {
            this.subscription = s;
            this.doOnSubscribe();
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void cancel() {
         if (TERMINATED.compareAndSet(this, 0, 3)) {
            this.timer.dispose();
            Subscription s = this.subscription;
            if (s != null) {
               this.subscription = null;
               s.cancel();
            }

            C v = this.values;
            if (v != null) {
               Operators.onDiscardMultiple(v, this.actual.currentContext());
               v.clear();
            }
         }

      }
   }
}
