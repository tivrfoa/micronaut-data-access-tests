package reactor.core.publisher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoPeekTerminal<T> extends InternalMonoOperator<T, T> implements Fuseable {
   final BiConsumer<? super T, Throwable> onAfterTerminateCall;
   final Consumer<? super T> onSuccessCall;
   final Consumer<? super Throwable> onErrorCall;

   MonoPeekTerminal(
      Mono<? extends T> source,
      @Nullable Consumer<? super T> onSuccessCall,
      @Nullable Consumer<? super Throwable> onErrorCall,
      @Nullable BiConsumer<? super T, Throwable> onAfterTerminateCall
   ) {
      super(source);
      this.onAfterTerminateCall = onAfterTerminateCall;
      this.onSuccessCall = onSuccessCall;
      this.onErrorCall = onErrorCall;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return actual instanceof Fuseable.ConditionalSubscriber
         ? new MonoPeekTerminal.MonoTerminalPeekSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this)
         : new MonoPeekTerminal.MonoTerminalPeekSubscriber<>(actual, this);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MonoTerminalPeekSubscriber<T> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final Fuseable.ConditionalSubscriber<? super T> actualConditional;
      final MonoPeekTerminal<T> parent;
      Subscription s;
      @Nullable
      Fuseable.QueueSubscription<T> queueSubscription;
      int sourceMode;
      volatile boolean done;
      boolean valued;

      MonoTerminalPeekSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, MonoPeekTerminal<T> parent) {
         this.actualConditional = actual;
         this.actual = actual;
         this.parent = parent;
      }

      MonoTerminalPeekSubscriber(CoreSubscriber<? super T> actual, MonoPeekTerminal<T> parent) {
         this.actual = actual;
         this.actualConditional = null;
         this.parent = parent;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
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
         this.s = s;
         this.queueSubscription = Operators.as(s);
         this.actual.onSubscribe(this);
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

            this.valued = true;
            if (this.parent.onSuccessCall != null) {
               try {
                  this.parent.onSuccessCall.accept(t);
               } catch (Throwable var4) {
                  this.onError(Operators.onOperatorError(this.s, var4, t, this.actual.currentContext()));
                  return;
               }
            }

            this.actual.onNext(t);
            if (this.parent.onAfterTerminateCall != null) {
               try {
                  this.parent.onAfterTerminateCall.accept(t, null);
               } catch (Throwable var3) {
                  Operators.onErrorDropped(Operators.onOperatorError(this.s, var3, t, this.actual.currentContext()), this.actual.currentContext());
               }
            }
         }

      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return false;
         } else if (this.actualConditional == null) {
            this.onNext(t);
            return false;
         } else {
            this.valued = true;
            if (this.parent.onSuccessCall != null) {
               try {
                  this.parent.onSuccessCall.accept(t);
               } catch (Throwable var5) {
                  this.onError(Operators.onOperatorError(this.s, var5, t, this.actual.currentContext()));
                  return false;
               }
            }

            boolean r = this.actualConditional.tryOnNext(t);
            if (this.parent.onAfterTerminateCall != null) {
               try {
                  this.parent.onAfterTerminateCall.accept(t, null);
               } catch (Throwable var4) {
                  Operators.onErrorDropped(Operators.onOperatorError(this.s, var4, t, this.actual.currentContext()), this.actual.currentContext());
               }
            }

            return r;
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            Consumer<? super Throwable> onError = this.parent.onErrorCall;
            if (!this.valued && onError != null) {
               try {
                  onError.accept(t);
               } catch (Throwable var5) {
                  t = Operators.onOperatorError(null, var5, t, this.actual.currentContext());
               }
            }

            try {
               this.actual.onError(t);
            } catch (UnsupportedOperationException var6) {
               if (onError == null || !Exceptions.isErrorCallbackNotImplemented(var6) && var6.getCause() != t) {
                  throw var6;
               }
            }

            if (!this.valued && this.parent.onAfterTerminateCall != null) {
               try {
                  this.parent.onAfterTerminateCall.accept(null, t);
               } catch (Throwable var4) {
                  Operators.onErrorDropped(Operators.onOperatorError(var4, this.actual.currentContext()), this.actual.currentContext());
               }
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            if (this.sourceMode == 0 && !this.valued && this.parent.onSuccessCall != null) {
               try {
                  this.parent.onSuccessCall.accept(null);
               } catch (Throwable var3) {
                  this.onError(Operators.onOperatorError(this.s, var3, this.actual.currentContext()));
                  return;
               }
            }

            this.done = true;
            this.actual.onComplete();
            if (this.sourceMode == 0 && !this.valued && this.parent.onAfterTerminateCall != null) {
               try {
                  this.parent.onAfterTerminateCall.accept(null, null);
               } catch (Throwable var2) {
                  Operators.onErrorDropped(Operators.onOperatorError(var2, this.actual.currentContext()), this.actual.currentContext());
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
         assert this.queueSubscription != null;

         boolean d = this.done;
         T v = (T)this.queueSubscription.poll();
         if (!this.valued && (v != null || d || this.sourceMode == 1)) {
            this.valued = true;
            if (this.parent.onSuccessCall != null) {
               try {
                  this.parent.onSuccessCall.accept(v);
               } catch (Throwable var5) {
                  throw Exceptions.propagate(Operators.onOperatorError(this.s, var5, v, this.actual.currentContext()));
               }
            }

            if (this.parent.onAfterTerminateCall != null) {
               try {
                  this.parent.onAfterTerminateCall.accept(v, null);
               } catch (Throwable var4) {
                  Operators.onErrorDropped(Operators.onOperatorError(var4, this.actual.currentContext()), this.actual.currentContext());
               }
            }
         }

         return v;
      }

      public boolean isEmpty() {
         return this.queueSubscription == null || this.queueSubscription.isEmpty();
      }

      public void clear() {
         assert this.queueSubscription != null;

         this.queueSubscription.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         int m;
         if (this.queueSubscription == null) {
            m = 0;
         } else if ((requestedMode & 4) != 0) {
            m = 0;
         } else {
            m = this.queueSubscription.requestFusion(requestedMode);
         }

         this.sourceMode = m;
         return m;
      }

      public int size() {
         return this.queueSubscription == null ? 0 : this.queueSubscription.size();
      }
   }
}
