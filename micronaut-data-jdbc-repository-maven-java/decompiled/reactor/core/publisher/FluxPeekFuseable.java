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

final class FluxPeekFuseable<T> extends InternalFluxOperator<T, T> implements Fuseable, SignalPeek<T> {
   final Consumer<? super Subscription> onSubscribeCall;
   final Consumer<? super T> onNextCall;
   final Consumer<? super Throwable> onErrorCall;
   final Runnable onCompleteCall;
   final Runnable onAfterTerminateCall;
   final LongConsumer onRequestCall;
   final Runnable onCancelCall;

   FluxPeekFuseable(
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
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxPeekFuseable.PeekFuseableConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this)
         : new FluxPeekFuseable.PeekFuseableSubscriber<>(actual, this));
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

   static final class PeekConditionalSubscriber<T> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final SignalPeek<T> parent;
      Subscription s;
      boolean done;

      PeekConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, SignalPeek<T> parent) {
         this.actual = actual;
         this.parent = parent;
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
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return false;
         } else {
            Consumer<? super T> nextHook = this.parent.onNextCall();
            if (nextHook != null) {
               try {
                  nextHook.accept(t);
               } catch (Throwable var5) {
                  Throwable e_ = Operators.onNextError(t, var5, this.actual.currentContext(), this.s);
                  if (e_ == null) {
                     return false;
                  }

                  this.onError(e_);
                  return true;
               }
            }

            return this.actual.tryOnNext(t);
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
               Exceptions.throwIfFatal(t);

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

   static final class PeekFuseableConditionalSubscriber<T> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final SignalPeek<T> parent;
      Fuseable.QueueSubscription<T> s;
      int sourceMode;
      volatile boolean done;

      PeekFuseableConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, SignalPeek<T> parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Override
      public Context currentContext() {
         Context c = this.actual.currentContext();
         Consumer<? super Context> contextHook = this.parent.onCurrentContextCall();
         if (!c.isEmpty() && contextHook != null) {
            contextHook.accept(c);
         }

         return c;
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

            this.s = (Fuseable.QueueSubscription)s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.actual.onNext((T)null);
         } else {
            if (this.done) {
               Operators.onNextDropped(t, this.actual.currentContext());
               return;
            }

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
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return false;
         } else {
            Consumer<? super T> nextHook = this.parent.onNextCall();
            if (nextHook != null) {
               try {
                  nextHook.accept(t);
               } catch (Throwable var5) {
                  Throwable e_ = Operators.onNextError(t, var5, this.actual.currentContext(), this.s);
                  if (e_ == null) {
                     return false;
                  }

                  this.onError(e_);
                  return true;
               }
            }

            return this.actual.tryOnNext(t);
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
               Exceptions.throwIfFatal(t);

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
            if (this.sourceMode == 2) {
               this.done = true;
               this.actual.onComplete();
            } else {
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
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      public T poll() {
         boolean d = this.done;

         T v;
         try {
            v = (T)this.s.poll();
         } catch (Throwable var10) {
            Throwable e = var10;
            Consumer<? super Throwable> errorHook = this.parent.onErrorCall();
            if (errorHook != null) {
               try {
                  errorHook.accept(e);
               } catch (Throwable var8) {
                  throw Exceptions.propagate(Operators.onOperatorError(this.s, var8, var10, this.actual.currentContext()));
               }
            }

            Runnable afterTerminateHook = this.parent.onAfterTerminateCall();
            if (afterTerminateHook != null) {
               try {
                  afterTerminateHook.run();
               } catch (Throwable var7) {
                  throw Exceptions.propagate(Operators.onOperatorError(this.s, var7, var10, this.actual.currentContext()));
               }
            }

            throw Exceptions.propagate(Operators.onOperatorError(this.s, var10, this.actual.currentContext()));
         }

         Consumer<? super T> nextHook = this.parent.onNextCall();
         if (v != null && nextHook != null) {
            try {
               nextHook.accept(v);
            } catch (Throwable var9) {
               Throwable e_ = Operators.onNextError(v, var9, this.actual.currentContext(), this.s);
               if (e_ == null) {
                  return this.poll();
               }

               throw Exceptions.propagate(e_);
            }
         }

         if (v == null && (d || this.sourceMode == 1)) {
            Runnable call = this.parent.onCompleteCall();
            if (call != null) {
               call.run();
            }

            call = this.parent.onAfterTerminateCall();
            if (call != null) {
               call.run();
            }
         }

         return v;
      }

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public void clear() {
         this.s.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 4) != 0) {
            return 0;
         } else {
            int m = this.s.requestFusion(requestedMode);
            this.sourceMode = m;
            return m;
         }
      }

      public int size() {
         return this.s.size();
      }
   }

   static final class PeekFuseableSubscriber<T> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final SignalPeek<T> parent;
      Fuseable.QueueSubscription<T> s;
      int sourceMode;
      volatile boolean done;

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

      PeekFuseableSubscriber(CoreSubscriber<? super T> actual, SignalPeek<T> parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Override
      public Context currentContext() {
         Context c = this.actual.currentContext();
         Consumer<? super Context> contextHook = this.parent.onCurrentContextCall();
         if (!c.isEmpty() && contextHook != null) {
            contextHook.accept(c);
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

            this.s = (Fuseable.QueueSubscription)s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.actual.onNext((T)null);
         } else {
            if (this.done) {
               Operators.onNextDropped(t, this.actual.currentContext());
               return;
            }

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
               Exceptions.throwIfFatal(t);

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
            if (this.sourceMode == 2) {
               this.done = true;
               this.actual.onComplete();
            } else {
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
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      public T poll() {
         boolean d = this.done;

         T v;
         try {
            v = (T)this.s.poll();
         } catch (Throwable var10) {
            Throwable e = var10;
            Consumer<? super Throwable> errorHook = this.parent.onErrorCall();
            if (errorHook != null) {
               try {
                  errorHook.accept(e);
               } catch (Throwable var8) {
                  throw Exceptions.propagate(Operators.onOperatorError(this.s, var8, var10, this.actual.currentContext()));
               }
            }

            Runnable afterTerminateHook = this.parent.onAfterTerminateCall();
            if (afterTerminateHook != null) {
               try {
                  afterTerminateHook.run();
               } catch (Throwable var7) {
                  throw Exceptions.propagate(Operators.onOperatorError(this.s, var7, var10, this.actual.currentContext()));
               }
            }

            throw Exceptions.propagate(Operators.onOperatorError(this.s, var10, this.actual.currentContext()));
         }

         Consumer<? super T> nextHook = this.parent.onNextCall();
         if (v != null && nextHook != null) {
            try {
               nextHook.accept(v);
            } catch (Throwable var9) {
               Throwable e_ = Operators.onNextError(v, var9, this.actual.currentContext(), this.s);
               if (e_ == null) {
                  return this.poll();
               }

               throw Exceptions.propagate(e_);
            }
         }

         if (v == null && (d || this.sourceMode == 1)) {
            Runnable call = this.parent.onCompleteCall();
            if (call != null) {
               call.run();
            }

            call = this.parent.onAfterTerminateCall();
            if (call != null) {
               call.run();
            }
         }

         return v;
      }

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public void clear() {
         this.s.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 4) != 0) {
            return 0;
         } else {
            int m = this.s.requestFusion(requestedMode);
            this.sourceMode = m;
            return m;
         }
      }

      public int size() {
         return this.s.size();
      }
   }
}
