package reactor.core.publisher;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxPeek<T> extends InternalFluxOperator<T, T> implements SignalPeek<T> {
   final Consumer<? super Subscription> onSubscribeCall;
   final Consumer<? super T> onNextCall;
   final Consumer<? super Throwable> onErrorCall;
   final Runnable onCompleteCall;
   final Runnable onAfterTerminateCall;
   final LongConsumer onRequestCall;
   final Runnable onCancelCall;

   FluxPeek(
      Flux<? extends T> source,
      @Nullable Consumer<? super Subscription> onSubscribeCall,
      @Nullable Consumer<? super T> onNextCall,
      @Nullable Consumer<? super Throwable> onErrorCall,
      @Nullable Runnable onCompleteCall,
      @Nullable Runnable onAfterTerminateCall,
      @Nullable LongConsumer onRequestCall,
      @Nullable Runnable onCancelCall
   ) {
      super(source);
      this.onSubscribeCall = onSubscribeCall;
      this.onNextCall = onNextCall;
      this.onErrorCall = onErrorCall;
      this.onCompleteCall = onCompleteCall;
      this.onAfterTerminateCall = onAfterTerminateCall;
      this.onRequestCall = onRequestCall;
      this.onCancelCall = onCancelCall;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<T> s2 = (Fuseable.ConditionalSubscriber)actual;
         return new FluxPeekFuseable.PeekConditionalSubscriber<>(s2, this);
      } else {
         return new FluxPeek.PeekSubscriber<>(actual, this);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   @Nullable
   @Override
   public Consumer<? super Subscription> onSubscribeCall() {
      return this.onSubscribeCall;
   }

   @Nullable
   @Override
   public Consumer<? super T> onNextCall() {
      return this.onNextCall;
   }

   @Nullable
   @Override
   public Consumer<? super Throwable> onErrorCall() {
      return this.onErrorCall;
   }

   @Nullable
   @Override
   public Runnable onCompleteCall() {
      return this.onCompleteCall;
   }

   @Nullable
   @Override
   public Runnable onAfterTerminateCall() {
      return this.onAfterTerminateCall;
   }

   @Nullable
   @Override
   public LongConsumer onRequestCall() {
      return this.onRequestCall;
   }

   @Nullable
   @Override
   public Runnable onCancelCall() {
      return this.onCancelCall;
   }

   static <T> void afterCompleteWithFailure(SignalPeek<T> parent, Throwable callbackFailure, Context context) {
      Exceptions.throwIfFatal(callbackFailure);
      Throwable _e = Operators.onOperatorError(callbackFailure, context);
      Operators.onErrorDropped(_e, context);
   }

   static <T> void afterErrorWithFailure(SignalPeek<T> parent, Throwable callbackFailure, Throwable originalError, Context context) {
      Exceptions.throwIfFatal(callbackFailure);
      Throwable _e = Operators.onOperatorError(null, callbackFailure, originalError, context);
      Operators.onErrorDropped(_e, context);
   }

   static final class PeekSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final SignalPeek<T> parent;
      Subscription s;
      boolean done;

      PeekSubscriber(CoreSubscriber<? super T> actual, SignalPeek<T> parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Context currentContext() {
         Context c = this.actual.currentContext();
         if (!c.isEmpty() && this.parent.onCurrentContextCall() != null) {
            this.parent.onCurrentContextCall().accept(c);
         }

         return c;
      }

      @Override
      public void request(long n) {
         LongConsumer requestHook = this.parent.onRequestCall();
         if (requestHook != null) {
            try {
               requestHook.accept(n);
            } catch (Throwable var5) {
               Operators.onOperatorError(var5, this.actual.currentContext());
            }
         }

         this.s.request(n);
      }

      @Override
      public void cancel() {
         Runnable cancelHook = this.parent.onCancelCall();
         if (cancelHook != null) {
            try {
               cancelHook.run();
            } catch (Throwable var3) {
               this.onError(Operators.onOperatorError(this.s, var3, this.actual.currentContext()));
               return;
            }
         }

         this.s.cancel();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            Consumer<? super Subscription> subscribeHook = this.parent.onSubscribeCall();
            if (subscribeHook != null) {
               try {
                  subscribeHook.accept(s);
               } catch (Throwable var4) {
                  Operators.error(this.actual, Operators.onOperatorError(s, var4, this.actual.currentContext()));
                  return;
               }
            }

            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            Consumer<? super T> nextHook = this.parent.onNextCall();
            if (nextHook != null) {
               try {
                  nextHook.accept(t);
               } catch (Throwable var5) {
                  Throwable e_ = Operators.onNextError(t, var5, this.actual.currentContext(), this.s);
                  if (e_ == null) {
                     this.request(1L);
                     return;
                  }

                  this.onError(e_);
                  return;
               }
            }

            this.actual.onNext(t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            Consumer<? super Throwable> errorHook = this.parent.onErrorCall();
            if (errorHook != null) {
               try {
                  errorHook.accept(t);
               } catch (Throwable var6) {
                  t = Operators.onOperatorError(null, var6, t, this.actual.currentContext());
               }
            }

            try {
               this.actual.onError(t);
            } catch (UnsupportedOperationException var7) {
               if (errorHook == null || !Exceptions.isErrorCallbackNotImplemented(var7) && var7.getCause() != t) {
                  throw var7;
               }
            }

            Runnable afterTerminateHook = this.parent.onAfterTerminateCall();
            if (afterTerminateHook != null) {
               try {
                  afterTerminateHook.run();
               } catch (Throwable var5) {
                  FluxPeek.afterErrorWithFailure(this.parent, var5, t, this.actual.currentContext());
               }
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            Runnable completeHook = this.parent.onCompleteCall();
            if (completeHook != null) {
               try {
                  completeHook.run();
               } catch (Throwable var5) {
                  this.onError(Operators.onOperatorError(this.s, var5, this.actual.currentContext()));
                  return;
               }
            }

            this.done = true;
            this.actual.onComplete();
            Runnable afterTerminateHook = this.parent.onAfterTerminateCall();
            if (afterTerminateHook != null) {
               try {
                  afterTerminateHook.run();
               } catch (Throwable var4) {
                  FluxPeek.afterCompleteWithFailure(this.parent, var4, this.actual.currentContext());
               }
            }

         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }
}
