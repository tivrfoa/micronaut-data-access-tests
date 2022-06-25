package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiConsumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class FluxHandle<T, R> extends InternalFluxOperator<T, R> {
   final BiConsumer<? super T, SynchronousSink<R>> handler;

   FluxHandle(Flux<? extends T> source, BiConsumer<? super T, SynchronousSink<R>> handler) {
      super(source);
      this.handler = (BiConsumer)Objects.requireNonNull(handler, "handler");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super R> cs = (Fuseable.ConditionalSubscriber)actual;
         return new FluxHandle.HandleConditionalSubscriber<>(cs, this.handler);
      } else {
         return new FluxHandle.HandleSubscriber<>(actual, this.handler);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class HandleConditionalSubscriber<T, R> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, R>, SynchronousSink<R> {
      final Fuseable.ConditionalSubscriber<? super R> actual;
      final BiConsumer<? super T, SynchronousSink<R>> handler;
      boolean done;
      boolean stop;
      Throwable error;
      R data;
      Subscription s;

      HandleConditionalSubscriber(Fuseable.ConditionalSubscriber<? super R> actual, BiConsumer<? super T, SynchronousSink<R>> handler) {
         this.actual = actual;
         this.handler = handler;
      }

      @Deprecated
      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Override
      public ContextView contextView() {
         return this.actual.currentContext();
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
            try {
               this.handler.accept(t, this);
            } catch (Throwable var4) {
               Throwable e_ = Operators.onNextError(t, var4, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.error = null;
                  this.s.request(1L);
               }

               return;
            }

            R v = this.data;
            this.data = null;
            if (v != null) {
               this.actual.onNext(v);
            }

            if (this.stop) {
               this.done = true;
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ != null) {
                     this.actual.onError(e_);
                  } else {
                     this.reset();
                     this.s.request(1L);
                  }
               } else {
                  this.s.cancel();
                  this.actual.onComplete();
               }
            } else if (v == null) {
               this.s.request(1L);
            }

         }
      }

      private void reset() {
         this.done = false;
         this.stop = false;
         this.error = null;
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return false;
         } else {
            try {
               this.handler.accept(t, this);
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
                  return true;
               }

               this.reset();
               return false;
            }

            R v = this.data;
            boolean emit = false;
            this.data = null;
            if (v != null) {
               emit = this.actual.tryOnNext(v);
            }

            if (this.stop) {
               this.done = true;
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ == null) {
                     this.reset();
                     return false;
                  }

                  this.actual.onError(e_);
               } else {
                  this.s.cancel();
                  this.actual.onComplete();
               }

               return true;
            } else {
               return emit;
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
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void complete() {
         if (this.stop) {
            throw new IllegalStateException("Cannot complete after a complete or error");
         } else {
            this.stop = true;
         }
      }

      @Override
      public void error(Throwable e) {
         if (this.stop) {
            throw new IllegalStateException("Cannot error after a complete or error");
         } else {
            this.error = (Throwable)Objects.requireNonNull(e, "error");
            this.stop = true;
         }
      }

      @Override
      public void next(R o) {
         if (this.data != null) {
            throw new IllegalStateException("Cannot emit more than one data");
         } else if (this.stop) {
            throw new IllegalStateException("Cannot emit after a complete or error");
         } else {
            this.data = (R)Objects.requireNonNull(o, "data");
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

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }

   static final class HandleSubscriber<T, R> implements InnerOperator<T, R>, Fuseable.ConditionalSubscriber<T>, SynchronousSink<R> {
      final CoreSubscriber<? super R> actual;
      final BiConsumer<? super T, SynchronousSink<R>> handler;
      boolean done;
      boolean stop;
      Throwable error;
      R data;
      Subscription s;

      HandleSubscriber(CoreSubscriber<? super R> actual, BiConsumer<? super T, SynchronousSink<R>> handler) {
         this.actual = actual;
         this.handler = handler;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Deprecated
      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Override
      public ContextView contextView() {
         return this.actual.currentContext();
      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            try {
               this.handler.accept(t, this);
            } catch (Throwable var4) {
               Throwable e_ = Operators.onNextError(t, var4, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.reset();
                  this.s.request(1L);
               }

               return;
            }

            R v = this.data;
            this.data = null;
            if (v != null) {
               this.actual.onNext(v);
            }

            if (this.stop) {
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ != null) {
                     this.onError(e_);
                  } else {
                     this.reset();
                     this.s.request(1L);
                  }
               } else {
                  this.s.cancel();
                  this.onComplete();
               }
            } else if (v == null) {
               this.s.request(1L);
            }

         }
      }

      private void reset() {
         this.done = false;
         this.stop = false;
         this.error = null;
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return false;
         } else {
            try {
               this.handler.accept(t, this);
            } catch (Throwable var4) {
               Throwable e_ = Operators.onNextError(t, var4, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
                  return true;
               }

               this.reset();
               return false;
            }

            R v = this.data;
            this.data = null;
            if (v != null) {
               this.actual.onNext(v);
            }

            if (this.stop) {
               if (this.error != null) {
                  Throwable e_ = Operators.onNextError(t, this.error, this.actual.currentContext(), this.s);
                  if (e_ == null) {
                     this.reset();
                     return false;
                  }

                  this.onError(e_);
               } else {
                  this.s.cancel();
                  this.onComplete();
               }

               return true;
            } else {
               return v != null;
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
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public void complete() {
         if (this.stop) {
            throw new IllegalStateException("Cannot complete after a complete or error");
         } else {
            this.stop = true;
         }
      }

      @Override
      public void error(Throwable e) {
         if (this.stop) {
            throw new IllegalStateException("Cannot error after a complete or error");
         } else {
            this.error = (Throwable)Objects.requireNonNull(e, "error");
            this.stop = true;
         }
      }

      @Override
      public void next(R o) {
         if (this.data != null) {
            throw new IllegalStateException("Cannot emit more than one data");
         } else if (this.stop) {
            throw new IllegalStateException("Cannot emit after a complete or error");
         } else {
            this.data = (R)Objects.requireNonNull(o, "data");
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }
   }
}
