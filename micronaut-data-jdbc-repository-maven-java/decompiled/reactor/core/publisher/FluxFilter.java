package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Predicate;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxFilter<T> extends InternalFluxOperator<T, T> {
   final Predicate<? super T> predicate;

   FluxFilter(Flux<? extends T> source, Predicate<? super T> predicate) {
      super(source);
      this.predicate = (Predicate)Objects.requireNonNull(predicate, "predicate");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxFilter.FilterConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this.predicate)
         : new FluxFilter.FilterSubscriber<>(actual, this.predicate));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FilterConditionalSubscriber<T> implements InnerOperator<T, T>, Fuseable.ConditionalSubscriber<T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final Context ctx;
      final Predicate<? super T> predicate;
      Subscription s;
      boolean done;

      FilterConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, Predicate<? super T> predicate) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.predicate = predicate;
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
            boolean b;
            try {
               b = this.predicate.test(t);
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.ctx, this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.s.request(1L);
               }

               Operators.onDiscard(t, this.ctx);
               return;
            }

            if (b) {
               this.actual.onNext(t);
            } else {
               this.s.request(1L);
               Operators.onDiscard(t, this.ctx);
            }

         }
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.ctx);
            return false;
         } else {
            boolean b;
            try {
               b = this.predicate.test(t);
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.ctx, this.s);
               if (e_ != null) {
                  this.onError(e_);
               }

               Operators.onDiscard(t, this.ctx);
               return false;
            }

            if (b) {
               return this.actual.tryOnNext(t);
            } else {
               Operators.onDiscard(t, this.ctx);
               return false;
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
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
      public CoreSubscriber<? super T> actual() {
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

   static final class FilterSubscriber<T> implements InnerOperator<T, T>, Fuseable.ConditionalSubscriber<T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final Predicate<? super T> predicate;
      Subscription s;
      boolean done;

      FilterSubscriber(CoreSubscriber<? super T> actual, Predicate<? super T> predicate) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.predicate = predicate;
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
            boolean b;
            try {
               b = this.predicate.test(t);
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.ctx, this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.s.request(1L);
               }

               Operators.onDiscard(t, this.ctx);
               return;
            }

            if (b) {
               this.actual.onNext(t);
            } else {
               Operators.onDiscard(t, this.ctx);
               this.s.request(1L);
            }

         }
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.ctx);
            return false;
         } else {
            boolean b;
            try {
               b = this.predicate.test(t);
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.ctx, this.s);
               if (e_ != null) {
                  this.onError(e_);
               }

               Operators.onDiscard(t, this.ctx);
               return false;
            }

            if (b) {
               this.actual.onNext(t);
            } else {
               Operators.onDiscard(t, this.ctx);
            }

            return b;
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
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
      public CoreSubscriber<? super T> actual() {
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
