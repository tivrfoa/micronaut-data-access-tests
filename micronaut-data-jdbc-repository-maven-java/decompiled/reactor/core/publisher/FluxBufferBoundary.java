package reactor.core.publisher;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxBufferBoundary<T, U, C extends Collection<? super T>> extends InternalFluxOperator<T, C> {
   final Publisher<U> other;
   final Supplier<C> bufferSupplier;

   FluxBufferBoundary(Flux<? extends T> source, Publisher<U> other, Supplier<C> bufferSupplier) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
      this.bufferSupplier = (Supplier)Objects.requireNonNull(bufferSupplier, "bufferSupplier");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super C> actual) {
      C buffer = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null buffer");
      FluxBufferBoundary.BufferBoundaryMain<T, U, C> parent = new FluxBufferBoundary.BufferBoundaryMain<>(
         this.source instanceof FluxInterval ? actual : Operators.serialize(actual), buffer, this.bufferSupplier
      );
      actual.onSubscribe(parent);
      this.other.subscribe(parent.other);
      return parent;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class BufferBoundaryMain<T, U, C extends Collection<? super T>> implements InnerOperator<T, C> {
      final Supplier<C> bufferSupplier;
      final CoreSubscriber<? super C> actual;
      final Context ctx;
      final FluxBufferBoundary.BufferBoundaryOther<U> other;
      C buffer;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxBufferBoundary.BufferBoundaryMain, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxBufferBoundary.BufferBoundaryMain.class, Subscription.class, "s"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxBufferBoundary.BufferBoundaryMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxBufferBoundary.BufferBoundaryMain.class, "requested"
      );

      BufferBoundaryMain(CoreSubscriber<? super C> actual, C buffer, Supplier<C> bufferSupplier) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.buffer = buffer;
         this.bufferSupplier = bufferSupplier;
         this.other = new FluxBufferBoundary.BufferBoundaryOther<>(this);
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
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.CAPACITY) {
            C buffer = this.buffer;
            return buffer != null ? buffer.size() : 0;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         Operators.terminate(S, this);
         Operators.onDiscardMultiple(this.buffer, this.ctx);
         this.other.cancel();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         synchronized(this) {
            C b = this.buffer;
            if (b != null) {
               b.add(t);
               return;
            }
         }

         Operators.onNextDropped(t, this.ctx);
      }

      @Override
      public void onError(Throwable t) {
         if (Operators.terminate(S, this)) {
            C b;
            synchronized(this) {
               b = this.buffer;
               this.buffer = null;
            }

            this.other.cancel();
            this.actual.onError(t);
            Operators.onDiscardMultiple(b, this.ctx);
         } else {
            Operators.onErrorDropped(t, this.ctx);
         }
      }

      @Override
      public void onComplete() {
         if (Operators.terminate(S, this)) {
            C b;
            synchronized(this) {
               b = this.buffer;
               this.buffer = null;
            }

            this.other.cancel();
            if (!b.isEmpty()) {
               if (this.emit(b)) {
                  this.actual.onComplete();
               }
            } else {
               this.actual.onComplete();
            }
         }

      }

      void otherComplete() {
         Subscription s = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
         if (s != Operators.cancelledSubscription()) {
            C b;
            synchronized(this) {
               b = this.buffer;
               this.buffer = null;
            }

            if (s != null) {
               s.cancel();
            }

            if (b == null || b.isEmpty()) {
               this.actual.onComplete();
            } else if (this.emit(b)) {
               this.actual.onComplete();
            }
         }

      }

      void otherError(Throwable t) {
         Subscription s = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
         if (s != Operators.cancelledSubscription()) {
            C b;
            synchronized(this) {
               b = this.buffer;
               this.buffer = null;
            }

            if (s != null) {
               s.cancel();
            }

            this.actual.onError(t);
            Operators.onDiscardMultiple(b, this.ctx);
         } else {
            Operators.onErrorDropped(t, this.ctx);
         }
      }

      void otherNext() {
         C c;
         try {
            c = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null buffer");
         } catch (Throwable var6) {
            this.otherError(Operators.onOperatorError(this.other, var6, this.ctx));
            return;
         }

         C b;
         synchronized(this) {
            b = this.buffer;
            this.buffer = c;
         }

         if (b != null && !b.isEmpty()) {
            this.emit(b);
         }
      }

      boolean emit(C b) {
         long r = this.requested;
         if (r != 0L) {
            this.actual.onNext((T)b);
            if (r != Long.MAX_VALUE) {
               REQUESTED.decrementAndGet(this);
            }

            return true;
         } else {
            this.actual.onError(Operators.onOperatorError(this, Exceptions.failWithOverflow(), b, this.ctx));
            Operators.onDiscardMultiple(b, this.ctx);
            return false;
         }
      }
   }

   static final class BufferBoundaryOther<U> extends Operators.DeferredSubscription implements InnerConsumer<U> {
      final FluxBufferBoundary.BufferBoundaryMain<?, U, ?> main;

      BufferBoundaryOther(FluxBufferBoundary.BufferBoundaryMain<?, U, ?> main) {
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
         this.main.otherNext();
      }

      @Override
      public void onError(Throwable t) {
         this.main.otherError(t);
      }

      @Override
      public void onComplete() {
         this.main.otherComplete();
      }
   }
}
