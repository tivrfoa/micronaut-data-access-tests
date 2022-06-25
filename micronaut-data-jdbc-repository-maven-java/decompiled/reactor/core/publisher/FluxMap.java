package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxMap<T, R> extends InternalFluxOperator<T, R> {
   final Function<? super T, ? extends R> mapper;

   FluxMap(Flux<? extends T> source, Function<? super T, ? extends R> mapper) {
      super(source);
      this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super R> cs = (Fuseable.ConditionalSubscriber)actual;
         return new FluxMap.MapConditionalSubscriber<>(cs, this.mapper);
      } else {
         return new FluxMap.MapSubscriber<>(actual, this.mapper);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MapConditionalSubscriber<T, R> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, R> {
      final Fuseable.ConditionalSubscriber<? super R> actual;
      final Function<? super T, ? extends R> mapper;
      boolean done;
      Subscription s;

      MapConditionalSubscriber(Fuseable.ConditionalSubscriber<? super R> actual, Function<? super T, ? extends R> mapper) {
         this.actual = actual;
         this.mapper = mapper;
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
            R v;
            try {
               v = (R)this.mapper.apply(t);
               if (v == null) {
                  throw new NullPointerException("The mapper [" + this.mapper.getClass().getName() + "] returned a null value.");
               }
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.s.request(1L);
               }

               return;
            }

            this.actual.onNext(v);
         }
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return true;
         } else {
            try {
               R v = (R)this.mapper.apply(t);
               if (v == null) {
                  throw new NullPointerException("The mapper [" + this.mapper.getClass().getName() + "] returned a null value.");
               } else {
                  return this.actual.tryOnNext(v);
               }
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.done = true;
                  this.actual.onError(e_);
                  return true;
               } else {
                  return false;
               }
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

   static final class MapSubscriber<T, R> implements InnerOperator<T, R> {
      final CoreSubscriber<? super R> actual;
      final Function<? super T, ? extends R> mapper;
      boolean done;
      Subscription s;

      MapSubscriber(CoreSubscriber<? super R> actual, Function<? super T, ? extends R> mapper) {
         this.actual = actual;
         this.mapper = mapper;
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
            R v;
            try {
               v = (R)this.mapper.apply(t);
               if (v == null) {
                  throw new NullPointerException("The mapper [" + this.mapper.getClass().getName() + "] returned a null value.");
               }
            } catch (Throwable var5) {
               Throwable e_ = Operators.onNextError(t, var5, this.actual.currentContext(), this.s);
               if (e_ != null) {
                  this.onError(e_);
               } else {
                  this.s.request(1L);
               }

               return;
            }

            this.actual.onNext(v);
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
