package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxDoFinally<T> extends InternalFluxOperator<T, T> {
   final Consumer<SignalType> onFinally;

   static <T> CoreSubscriber<T> createSubscriber(CoreSubscriber<? super T> s, Consumer<SignalType> onFinally, boolean fuseable) {
      if (fuseable) {
         return (CoreSubscriber<T>)(s instanceof Fuseable.ConditionalSubscriber
            ? new FluxDoFinally.DoFinallyFuseableConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)s, onFinally)
            : new FluxDoFinally.DoFinallyFuseableSubscriber<>(s, onFinally));
      } else {
         return (CoreSubscriber<T>)(s instanceof Fuseable.ConditionalSubscriber
            ? new FluxDoFinally.DoFinallyConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)s, onFinally)
            : new FluxDoFinally.DoFinallySubscriber<>(s, onFinally));
      }
   }

   FluxDoFinally(Flux<? extends T> source, Consumer<SignalType> onFinally) {
      super(source);
      this.onFinally = onFinally;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return createSubscriber(actual, this.onFinally, false);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DoFinallyConditionalSubscriber<T> extends FluxDoFinally.DoFinallySubscriber<T> implements Fuseable.ConditionalSubscriber<T> {
      DoFinallyConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, Consumer<SignalType> onFinally) {
         super(actual, onFinally);
      }

      @Override
      public boolean tryOnNext(T t) {
         return ((Fuseable.ConditionalSubscriber)this.actual).tryOnNext(t);
      }
   }

   static final class DoFinallyFuseableConditionalSubscriber<T>
      extends FluxDoFinally.DoFinallyFuseableSubscriber<T>
      implements Fuseable.ConditionalSubscriber<T> {
      DoFinallyFuseableConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, Consumer<SignalType> onFinally) {
         super(actual, onFinally);
      }

      @Override
      public boolean tryOnNext(T t) {
         return ((Fuseable.ConditionalSubscriber)this.actual).tryOnNext(t);
      }
   }

   static class DoFinallyFuseableSubscriber<T> extends FluxDoFinally.DoFinallySubscriber<T> implements Fuseable, Fuseable.QueueSubscription<T> {
      DoFinallyFuseableSubscriber(CoreSubscriber<? super T> actual, Consumer<SignalType> onFinally) {
         super(actual, onFinally);
      }

      @Override
      public int requestFusion(int mode) {
         Fuseable.QueueSubscription<T> qs = this.qs;
         if (qs != null && (mode & 4) == 0) {
            int m = qs.requestFusion(mode);
            if (m != 0) {
               this.syncFused = m == 1;
            }

            return m;
         } else {
            return 0;
         }
      }

      public void clear() {
         if (this.qs != null) {
            this.qs.clear();
         }

      }

      public boolean isEmpty() {
         return this.qs == null || this.qs.isEmpty();
      }

      @Nullable
      public T poll() {
         if (this.qs == null) {
            return null;
         } else {
            T v = (T)this.qs.poll();
            if (v == null && this.syncFused) {
               this.runFinally(SignalType.ON_COMPLETE);
            }

            return v;
         }
      }

      public int size() {
         return this.qs == null ? 0 : this.qs.size();
      }
   }

   static class DoFinallySubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Consumer<SignalType> onFinally;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxDoFinally.DoFinallySubscriber> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxDoFinally.DoFinallySubscriber.class, "once"
      );
      Fuseable.QueueSubscription<T> qs;
      Subscription s;
      boolean syncFused;

      DoFinallySubscriber(CoreSubscriber<? super T> actual, Consumer<SignalType> onFinally) {
         this.actual = actual;
         this.onFinally = onFinally;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.once == 1;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (s instanceof Fuseable.QueueSubscription) {
               this.qs = (Fuseable.QueueSubscription)s;
            }

            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         try {
            this.actual.onError(t);
         } finally {
            this.runFinally(SignalType.ON_ERROR);
         }

      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
         this.runFinally(SignalType.ON_COMPLETE);
      }

      @Override
      public void cancel() {
         this.s.cancel();
         this.runFinally(SignalType.CANCEL);
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      void runFinally(SignalType signalType) {
         if (ONCE.compareAndSet(this, 0, 1)) {
            try {
               this.onFinally.accept(signalType);
            } catch (Throwable var3) {
               Exceptions.throwIfFatal(var3);
               Operators.onErrorDropped(var3, this.actual.currentContext());
            }
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }
}
