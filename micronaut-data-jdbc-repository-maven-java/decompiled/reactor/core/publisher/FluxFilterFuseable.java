package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Predicate;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxFilterFuseable<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final Predicate<? super T> predicate;

   FluxFilterFuseable(Flux<? extends T> source, Predicate<? super T> predicate) {
      super(source);
      this.predicate = (Predicate)Objects.requireNonNull(predicate, "predicate");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxFilterFuseable.FilterFuseableConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this.predicate)
         : new FluxFilterFuseable.FilterFuseableSubscriber<>(actual, this.predicate));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FilterFuseableConditionalSubscriber<T> implements InnerOperator<T, T>, Fuseable.ConditionalSubscriber<T>, Fuseable.QueueSubscription<T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final Context ctx;
      final Predicate<? super T> predicate;
      Fuseable.QueueSubscription<T> s;
      boolean done;
      int sourceMode;

      FilterFuseableConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, Predicate<? super T> predicate) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.predicate = predicate;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
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
               Operators.onNextDropped(t, this.ctx);
               return;
            }

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

      @Nullable
      public T poll() {
         if (this.sourceMode == 2) {
            long dropped = 0L;

            while(true) {
               T v = (T)this.s.poll();

               try {
                  if (v == null || this.predicate.test(v)) {
                     if (dropped != 0L) {
                        this.request(dropped);
                     }

                     return v;
                  }

                  Operators.onDiscard(v, this.ctx);
                  ++dropped;
               } catch (Throwable var6) {
                  RuntimeException e_ = Operators.onNextPollError(v, var6, this.ctx);
                  Operators.onDiscard(v, this.ctx);
                  if (e_ != null) {
                     throw e_;
                  }
               }
            }
         } else {
            while(true) {
               T v = (T)this.s.poll();

               try {
                  if (v == null || this.predicate.test(v)) {
                     return v;
                  }

                  Operators.onDiscard(v, this.ctx);
               } catch (Throwable var7) {
                  RuntimeException e_ = Operators.onNextPollError(v, var7, this.ctx);
                  Operators.onDiscard(v, this.ctx);
                  if (e_ != null) {
                     throw e_;
                  }
               }
            }
         }
      }

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public void clear() {
         this.s.clear();
      }

      public int size() {
         return this.s.size();
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
   }

   static final class FilterFuseableSubscriber<T> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T>, Fuseable.ConditionalSubscriber<T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final Predicate<? super T> predicate;
      Fuseable.QueueSubscription<T> s;
      boolean done;
      int sourceMode;

      FilterFuseableSubscriber(CoreSubscriber<? super T> actual, Predicate<? super T> predicate) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.predicate = predicate;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
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
               Operators.onNextDropped(t, this.ctx);
               return;
            }

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
               this.actual.onNext(t);
               return true;
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

      @Nullable
      public T poll() {
         if (this.sourceMode == 2) {
            long dropped = 0L;

            while(true) {
               T v = (T)this.s.poll();

               try {
                  if (v == null || this.predicate.test(v)) {
                     if (dropped != 0L) {
                        this.request(dropped);
                     }

                     return v;
                  }

                  Operators.onDiscard(v, this.ctx);
                  ++dropped;
               } catch (Throwable var6) {
                  RuntimeException e_ = Operators.onNextPollError(v, var6, this.currentContext());
                  Operators.onDiscard(v, this.ctx);
                  if (e_ != null) {
                     throw e_;
                  }
               }
            }
         } else {
            while(true) {
               T v = (T)this.s.poll();

               try {
                  if (v == null || this.predicate.test(v)) {
                     return v;
                  }

                  Operators.onDiscard(v, this.ctx);
               } catch (Throwable var7) {
                  RuntimeException e_ = Operators.onNextPollError(v, var7, this.currentContext());
                  Operators.onDiscard(v, this.ctx);
                  if (e_ != null) {
                     throw e_;
                  }
               }
            }
         }
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
