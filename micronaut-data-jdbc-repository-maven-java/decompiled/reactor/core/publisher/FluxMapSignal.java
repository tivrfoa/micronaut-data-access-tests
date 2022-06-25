package reactor.core.publisher;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxMapSignal<T, R> extends InternalFluxOperator<T, R> {
   final Function<? super T, ? extends R> mapperNext;
   final Function<? super Throwable, ? extends R> mapperError;
   final Supplier<? extends R> mapperComplete;

   FluxMapSignal(
      Flux<? extends T> source,
      @Nullable Function<? super T, ? extends R> mapperNext,
      @Nullable Function<? super Throwable, ? extends R> mapperError,
      @Nullable Supplier<? extends R> mapperComplete
   ) {
      super(source);
      if (mapperNext == null && mapperError == null && mapperComplete == null) {
         throw new IllegalArgumentException("Map Signal needs at least one valid mapper");
      } else {
         this.mapperNext = mapperNext;
         this.mapperError = mapperError;
         this.mapperComplete = mapperComplete;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return new FluxMapSignal.FluxMapSignalSubscriber<>(actual, this.mapperNext, this.mapperError, this.mapperComplete);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FluxMapSignalSubscriber<T, R> extends AbstractQueue<R> implements InnerOperator<T, R>, BooleanSupplier {
      final CoreSubscriber<? super R> actual;
      final Function<? super T, ? extends R> mapperNext;
      final Function<? super Throwable, ? extends R> mapperError;
      final Supplier<? extends R> mapperComplete;
      boolean done;
      Subscription s;
      R value;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxMapSignal.FluxMapSignalSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxMapSignal.FluxMapSignalSubscriber.class, "requested"
      );
      volatile boolean cancelled;
      long produced;

      FluxMapSignalSubscriber(
         CoreSubscriber<? super R> actual,
         @Nullable Function<? super T, ? extends R> mapperNext,
         @Nullable Function<? super Throwable, ? extends R> mapperError,
         @Nullable Supplier<? extends R> mapperComplete
      ) {
         this.actual = actual;
         this.mapperNext = mapperNext;
         this.mapperError = mapperError;
         this.mapperComplete = mapperComplete;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            if (this.mapperNext == null) {
               s.request(Long.MAX_VALUE);
            }
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else if (this.mapperNext != null) {
            R v;
            try {
               v = (R)this.mapperNext.apply(t);
               if (v == null) {
                  throw new NullPointerException("The mapper [" + this.mapperNext.getClass().getName() + "] returned a null value.");
               }
            } catch (Throwable var4) {
               this.done = true;
               this.actual.onError(Operators.onOperatorError(this.s, var4, t, this.actual.currentContext()));
               return;
            }

            ++this.produced;
            this.actual.onNext(v);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            if (this.mapperError == null) {
               this.actual.onError(t);
            } else {
               R v;
               try {
                  v = (R)this.mapperError.apply(t);
                  if (v == null) {
                     throw new NullPointerException("The mapper [" + this.mapperError.getClass().getName() + "] returned a null value.");
                  }
               } catch (Throwable var5) {
                  this.done = true;
                  this.actual.onError(Operators.onOperatorError(this.s, var5, t, this.actual.currentContext()));
                  return;
               }

               this.value = v;
               long p = this.produced;
               if (p != 0L) {
                  Operators.addCap(REQUESTED, this, -p);
               }

               DrainUtils.postComplete(this.actual, this, REQUESTED, this, this);
            }
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            if (this.mapperComplete == null) {
               this.actual.onComplete();
            } else {
               R v;
               try {
                  v = (R)this.mapperComplete.get();
                  if (v == null) {
                     throw new NullPointerException("The mapper [" + this.mapperComplete.getClass().getName() + "] returned a null value.");
                  }
               } catch (Throwable var4) {
                  this.done = true;
                  this.actual.onError(Operators.onOperatorError(this.s, var4, this.actual.currentContext()));
                  return;
               }

               this.value = v;
               long p = this.produced;
               if (p != 0L) {
                  Operators.addCap(REQUESTED, this, -p);
               }

               DrainUtils.postComplete(this.actual, this, REQUESTED, this, this);
            }
         }
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && !DrainUtils.postCompleteRequest(n, this.actual, this, REQUESTED, this, this)) {
            this.s.request(n);
         }

      }

      public boolean offer(R e) {
         throw new UnsupportedOperationException();
      }

      @Nullable
      public R poll() {
         R v = this.value;
         if (v != null) {
            this.value = null;
            return v;
         } else {
            return null;
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.getAsBoolean();
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.size();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Nullable
      public R peek() {
         return this.value;
      }

      public boolean getAsBoolean() {
         return this.cancelled;
      }

      @Override
      public void cancel() {
         this.cancelled = true;
         this.s.cancel();
      }

      public Iterator<R> iterator() {
         throw new UnsupportedOperationException();
      }

      public int size() {
         return this.value == null ? 0 : 1;
      }
   }
}
