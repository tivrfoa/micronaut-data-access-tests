package reactor.core.publisher;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxBuffer<T, C extends Collection<? super T>> extends InternalFluxOperator<T, C> {
   final int size;
   final int skip;
   final Supplier<C> bufferSupplier;

   FluxBuffer(Flux<? extends T> source, int size, Supplier<C> bufferSupplier) {
      this(source, size, size, bufferSupplier);
   }

   FluxBuffer(Flux<? extends T> source, int size, int skip, Supplier<C> bufferSupplier) {
      super(source);
      if (size <= 0) {
         throw new IllegalArgumentException("size > 0 required but it was " + size);
      } else if (skip <= 0) {
         throw new IllegalArgumentException("skip > 0 required but it was " + skip);
      } else {
         this.size = size;
         this.skip = skip;
         this.bufferSupplier = (Supplier)Objects.requireNonNull(bufferSupplier, "bufferSupplier");
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super C> actual) {
      if (this.size == this.skip) {
         return new FluxBuffer.BufferExactSubscriber<>(actual, this.size, this.bufferSupplier);
      } else {
         return (CoreSubscriber<? super T>)(this.skip > this.size
            ? new FluxBuffer.BufferSkipSubscriber<>(actual, this.size, this.skip, this.bufferSupplier)
            : new FluxBuffer.BufferOverlappingSubscriber<>(actual, this.size, this.skip, this.bufferSupplier));
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class BufferExactSubscriber<T, C extends Collection<? super T>> implements InnerOperator<T, C> {
      final CoreSubscriber<? super C> actual;
      final Supplier<C> bufferSupplier;
      final int size;
      C buffer;
      Subscription s;
      boolean done;

      BufferExactSubscriber(CoreSubscriber<? super C> actual, int size, Supplier<C> bufferSupplier) {
         this.actual = actual;
         this.size = size;
         this.bufferSupplier = bufferSupplier;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            this.s.request(Operators.multiplyCap(n, (long)this.size));
         }

      }

      @Override
      public void cancel() {
         this.s.cancel();
         Operators.onDiscardMultiple(this.buffer, this.actual.currentContext());
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
            C b = this.buffer;
            if (b == null) {
               try {
                  b = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null buffer");
               } catch (Throwable var5) {
                  Context ctx = this.actual.currentContext();
                  this.onError(Operators.onOperatorError(this.s, var5, t, ctx));
                  Operators.onDiscard(t, ctx);
                  return;
               }

               this.buffer = b;
            }

            b.add(t);
            if (b.size() == this.size) {
               this.buffer = null;
               this.actual.onNext((T)b);
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
            Operators.onDiscardMultiple(this.buffer, this.actual.currentContext());
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            C b = this.buffer;
            if (b != null && !b.isEmpty()) {
               this.actual.onNext((T)b);
            }

            this.actual.onComplete();
         }
      }

      @Override
      public CoreSubscriber<? super C> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.BUFFERED) {
            C b = this.buffer;
            return b != null ? b.size() : 0;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.size;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.size;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }

   static final class BufferOverlappingSubscriber<T, C extends Collection<? super T>> extends ArrayDeque<C> implements BooleanSupplier, InnerOperator<T, C> {
      final CoreSubscriber<? super C> actual;
      final Supplier<C> bufferSupplier;
      final int size;
      final int skip;
      Subscription s;
      boolean done;
      long index;
      volatile boolean cancelled;
      long produced;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxBuffer.BufferOverlappingSubscriber> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxBuffer.BufferOverlappingSubscriber.class, "once"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxBuffer.BufferOverlappingSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxBuffer.BufferOverlappingSubscriber.class, "requested"
      );

      BufferOverlappingSubscriber(CoreSubscriber<? super C> actual, int size, int skip, Supplier<C> bufferSupplier) {
         this.actual = actual;
         this.size = size;
         this.skip = skip;
         this.bufferSupplier = bufferSupplier;
      }

      public boolean getAsBoolean() {
         return this.cancelled;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            if (!DrainUtils.postCompleteRequest(n, this.actual, this, REQUESTED, this, this)) {
               if (this.once == 0 && ONCE.compareAndSet(this, 0, 1)) {
                  long u = Operators.multiplyCap((long)this.skip, n - 1L);
                  long r = Operators.addCap((long)this.size, u);
                  this.s.request(r);
               } else {
                  long r = Operators.multiplyCap((long)this.skip, n);
                  this.s.request(r);
               }

            }
         }
      }

      @Override
      public void cancel() {
         this.cancelled = true;
         this.s.cancel();
         this.clear();
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
            long i = this.index;
            if (i % (long)this.skip == 0L) {
               C b;
               try {
                  b = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null buffer");
               } catch (Throwable var7) {
                  Context ctx = this.actual.currentContext();
                  this.onError(Operators.onOperatorError(this.s, var7, t, ctx));
                  Operators.onDiscard(t, ctx);
                  return;
               }

               this.offer(b);
            }

            C b = (C)this.peek();
            if (b != null && b.size() + 1 == this.size) {
               this.poll();
               b.add(t);
               this.actual.onNext((T)b);
               ++this.produced;
            }

            for(C b0 : this) {
               b0.add(t);
            }

            this.index = i + 1L;
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.clear();
            this.actual.onError(t);
         }
      }

      public void clear() {
         Context ctx = this.actual.currentContext();

         for(C b : this) {
            Operators.onDiscardMultiple(b, ctx);
         }

         super.clear();
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            long p = this.produced;
            if (p != 0L) {
               Operators.produced(REQUESTED, this, p);
            }

            DrainUtils.postComplete(this.actual, this, REQUESTED, this, this);
         }
      }

      @Override
      public CoreSubscriber<? super C> actual() {
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
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.size() * this.size;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.stream().mapToInt(Collection::size).sum();
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }

   static final class BufferSkipSubscriber<T, C extends Collection<? super T>> implements InnerOperator<T, C> {
      final CoreSubscriber<? super C> actual;
      final Context ctx;
      final Supplier<C> bufferSupplier;
      final int size;
      final int skip;
      C buffer;
      Subscription s;
      boolean done;
      long index;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxBuffer.BufferSkipSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxBuffer.BufferSkipSubscriber.class, "wip"
      );

      BufferSkipSubscriber(CoreSubscriber<? super C> actual, int size, int skip, Supplier<C> bufferSupplier) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.size = size;
         this.skip = skip;
         this.bufferSupplier = bufferSupplier;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
               long u = Operators.multiplyCap(n, (long)this.size);
               long v = Operators.multiplyCap((long)(this.skip - this.size), n - 1L);
               this.s.request(Operators.addCap(u, v));
            } else {
               this.s.request(Operators.multiplyCap((long)this.skip, n));
            }

         }
      }

      @Override
      public void cancel() {
         this.s.cancel();
         Operators.onDiscardMultiple(this.buffer, this.ctx);
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
            C b = this.buffer;
            long i = this.index;
            if (i % (long)this.skip == 0L) {
               try {
                  b = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null buffer");
               } catch (Throwable var6) {
                  this.onError(Operators.onOperatorError(this.s, var6, t, this.ctx));
                  Operators.onDiscard(t, this.ctx);
                  return;
               }

               this.buffer = b;
            }

            if (b != null) {
               b.add(t);
               if (b.size() == this.size) {
                  this.buffer = null;
                  this.actual.onNext((T)b);
               }
            } else {
               Operators.onDiscard(t, this.ctx);
            }

            this.index = i + 1L;
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.done = true;
            C b = this.buffer;
            this.buffer = null;
            this.actual.onError(t);
            Operators.onDiscardMultiple(b, this.ctx);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            C b = this.buffer;
            this.buffer = null;
            if (b != null) {
               this.actual.onNext((T)b);
            }

            this.actual.onComplete();
         }
      }

      @Override
      public CoreSubscriber<? super C> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.size;
         } else if (key == Scannable.Attr.BUFFERED) {
            C b = this.buffer;
            return b != null ? b.size() : 0;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.size;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }
}
