package reactor.core.publisher;

import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

final class FluxElapsed<T> extends InternalFluxOperator<T, Tuple2<Long, T>> implements Fuseable {
   final Scheduler scheduler;

   FluxElapsed(Flux<T> source, Scheduler scheduler) {
      super(source);
      this.scheduler = scheduler;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Tuple2<Long, T>> actual) {
      return new FluxElapsed.ElapsedSubscriber<>(actual, this.scheduler);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   static final class ElapsedSubscriber<T> implements InnerOperator<T, Tuple2<Long, T>>, Fuseable.QueueSubscription<Tuple2<Long, T>> {
      final CoreSubscriber<? super Tuple2<Long, T>> actual;
      final Scheduler scheduler;
      Subscription s;
      Fuseable.QueueSubscription<T> qs;
      long lastTime;

      ElapsedSubscriber(CoreSubscriber<? super Tuple2<Long, T>> actual, Scheduler scheduler) {
         this.actual = actual;
         this.scheduler = scheduler;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.scheduler;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.lastTime = this.scheduler.now(TimeUnit.MILLISECONDS);
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public CoreSubscriber<? super Tuple2<Long, T>> actual() {
         return this.actual;
      }

      @Override
      public void onNext(T t) {
         if (t == null) {
            this.actual.onNext(null);
         } else {
            this.actual.onNext(this.snapshot(t));
         }
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
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
      public int requestFusion(int requestedMode) {
         Fuseable.QueueSubscription<T> qs = Operators.as(this.s);
         if (qs != null) {
            this.qs = qs;
            return qs.requestFusion(requestedMode);
         } else {
            return 0;
         }
      }

      Tuple2<Long, T> snapshot(T data) {
         long now = this.scheduler.now(TimeUnit.MILLISECONDS);
         long last = this.lastTime;
         this.lastTime = now;
         long delta = now - last;
         return Tuples.of(delta, data);
      }

      @Nullable
      public Tuple2<Long, T> poll() {
         T data = (T)this.qs.poll();
         return data != null ? this.snapshot(data) : null;
      }

      public int size() {
         return this.qs.size();
      }

      public boolean isEmpty() {
         return this.qs.isEmpty();
      }

      public void clear() {
         this.qs.clear();
      }
   }
}
