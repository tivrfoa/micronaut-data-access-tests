package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class FluxDoOnEach<T> extends InternalFluxOperator<T, T> {
   final Consumer<? super Signal<T>> onSignal;

   FluxDoOnEach(Flux<? extends T> source, Consumer<? super Signal<T>> onSignal) {
      super(source);
      this.onSignal = (Consumer)Objects.requireNonNull(onSignal, "onSignal");
   }

   static <T> FluxDoOnEach.DoOnEachSubscriber<T> createSubscriber(
      CoreSubscriber<? super T> actual, Consumer<? super Signal<T>> onSignal, boolean fuseable, boolean isMono
   ) {
      if (fuseable) {
         return (FluxDoOnEach.DoOnEachSubscriber<T>)(actual instanceof Fuseable.ConditionalSubscriber
            ? new FluxDoOnEach.DoOnEachFuseableConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, onSignal, isMono)
            : new FluxDoOnEach.DoOnEachFuseableSubscriber<>(actual, onSignal, isMono));
      } else {
         return (FluxDoOnEach.DoOnEachSubscriber<T>)(actual instanceof Fuseable.ConditionalSubscriber
            ? new FluxDoOnEach.DoOnEachConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, onSignal, isMono)
            : new FluxDoOnEach.DoOnEachSubscriber<>(actual, onSignal, isMono));
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return createSubscriber(actual, this.onSignal, false, false);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DoOnEachConditionalSubscriber<T> extends FluxDoOnEach.DoOnEachSubscriber<T> implements Fuseable.ConditionalSubscriber<T> {
      DoOnEachConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, Consumer<? super Signal<T>> onSignal, boolean isMono) {
         super(actual, onSignal, isMono);
      }

      @Override
      public boolean tryOnNext(T t) {
         boolean result = ((Fuseable.ConditionalSubscriber)this.actual).tryOnNext(t);
         if (result) {
            this.t = t;
            this.onSignal.accept(this);
         }

         return result;
      }
   }

   static final class DoOnEachFuseableConditionalSubscriber<T> extends FluxDoOnEach.DoOnEachFuseableSubscriber<T> implements Fuseable.ConditionalSubscriber<T> {
      DoOnEachFuseableConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, Consumer<? super Signal<T>> onSignal, boolean isMono) {
         super(actual, onSignal, isMono);
      }

      @Override
      public boolean tryOnNext(T t) {
         boolean result = ((Fuseable.ConditionalSubscriber)this.actual).tryOnNext(t);
         if (result) {
            this.t = t;
            this.onSignal.accept(this);
         }

         return result;
      }
   }

   static class DoOnEachFuseableSubscriber<T> extends FluxDoOnEach.DoOnEachSubscriber<T> implements Fuseable, Fuseable.QueueSubscription<T> {
      boolean syncFused;

      DoOnEachFuseableSubscriber(CoreSubscriber<? super T> actual, Consumer<? super Signal<T>> onSignal, boolean isMono) {
         super(actual, onSignal, isMono);
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
         this.qs.clear();
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
               this.state = 3;

               try {
                  this.onSignal.accept(Signal.complete(this.cachedContext));
               } catch (Throwable var3) {
                  throw var3;
               }
            } else if (v != null) {
               this.t = v;
               this.onSignal.accept(this);
            }

            return v;
         }
      }

      public int size() {
         return this.qs == null ? 0 : this.qs.size();
      }
   }

   static class DoOnEachSubscriber<T> implements InnerOperator<T, T>, Signal<T> {
      static final short STATE_FLUX_START = 0;
      static final short STATE_MONO_START = 1;
      static final short STATE_SKIP_HANDLER = 2;
      static final short STATE_DONE = 3;
      final CoreSubscriber<? super T> actual;
      final Context cachedContext;
      final Consumer<? super Signal<T>> onSignal;
      T t;
      Subscription s;
      @Nullable
      Fuseable.QueueSubscription<T> qs;
      short state;

      DoOnEachSubscriber(CoreSubscriber<? super T> actual, Consumer<? super Signal<T>> onSignal, boolean monoFlavor) {
         this.actual = actual;
         this.cachedContext = actual.currentContext();
         this.onSignal = onSignal;
         this.state = (short)(monoFlavor ? 1 : 0);
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.qs = Operators.as(s);
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public Context currentContext() {
         return this.cachedContext;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.state == 3;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onNext(T t) {
         if (this.state == 3) {
            Operators.onNextDropped(t, this.cachedContext);
         } else {
            try {
               this.t = t;
               this.onSignal.accept(this);
            } catch (Throwable var4) {
               this.onError(Operators.onOperatorError(this.s, var4, t, this.cachedContext));
               return;
            }

            if (this.state == 1) {
               this.state = 2;

               try {
                  this.onSignal.accept(Signal.complete(this.cachedContext));
               } catch (Throwable var3) {
                  this.state = 1;
                  this.onError(Operators.onOperatorError(this.s, var3, this.cachedContext));
                  return;
               }
            }

            this.actual.onNext(t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.state == 3) {
            Operators.onErrorDropped(t, this.cachedContext);
         } else {
            boolean applyHandler = this.state < 2;
            this.state = 3;
            if (applyHandler) {
               try {
                  this.onSignal.accept(Signal.error(t, this.cachedContext));
               } catch (Throwable var4) {
                  t = Operators.onOperatorError(null, var4, t, this.cachedContext);
               }
            }

            try {
               this.actual.onError(t);
            } catch (UnsupportedOperationException var5) {
               if (!Exceptions.isErrorCallbackNotImplemented(var5) && var5.getCause() != t) {
                  throw var5;
               }
            }

         }
      }

      @Override
      public void onComplete() {
         if (this.state != 3) {
            short oldState = this.state;
            this.state = 3;
            if (oldState < 2) {
               try {
                  this.onSignal.accept(Signal.complete(this.cachedContext));
               } catch (Throwable var3) {
                  this.state = oldState;
                  this.onError(Operators.onOperatorError(this.s, var3, this.cachedContext));
                  return;
               }
            }

            this.actual.onComplete();
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Throwable getThrowable() {
         return null;
      }

      @Nullable
      @Override
      public Subscription getSubscription() {
         return null;
      }

      @Nullable
      @Override
      public T get() {
         return this.t;
      }

      @Override
      public ContextView getContextView() {
         return this.cachedContext;
      }

      @Override
      public SignalType getType() {
         return SignalType.ON_NEXT;
      }

      public String toString() {
         return "doOnEach_onNext(" + this.t + ")";
      }
   }
}
