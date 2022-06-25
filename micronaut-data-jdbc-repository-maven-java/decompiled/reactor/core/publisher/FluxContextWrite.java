package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxContextWrite<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final Function<Context, Context> doOnContext;

   FluxContextWrite(Flux<? extends T> source, Function<Context, Context> doOnContext) {
      super(source);
      this.doOnContext = (Function)Objects.requireNonNull(doOnContext, "doOnContext");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      Context c = (Context)this.doOnContext.apply(actual.currentContext());
      return new FluxContextWrite.ContextWriteSubscriber<>(actual, c);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ContextWriteSubscriber<T> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final Fuseable.ConditionalSubscriber<? super T> actualConditional;
      final Context context;
      Fuseable.QueueSubscription<T> qs;
      Subscription s;

      ContextWriteSubscriber(CoreSubscriber<? super T> actual, Context context) {
         this.actual = actual;
         this.context = context;
         if (actual instanceof Fuseable.ConditionalSubscriber) {
            this.actualConditional = (Fuseable.ConditionalSubscriber)actual;
         } else {
            this.actualConditional = null;
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Context currentContext() {
         return this.context;
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
      public boolean tryOnNext(T t) {
         if (this.actualConditional != null) {
            return this.actualConditional.tryOnNext(t);
         } else {
            this.actual.onNext(t);
            return true;
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

      @Override
      public int requestFusion(int requestedMode) {
         return this.qs == null ? 0 : this.qs.requestFusion(requestedMode);
      }

      @Nullable
      public T poll() {
         return (T)(this.qs != null ? this.qs.poll() : null);
      }

      public boolean isEmpty() {
         return this.qs == null || this.qs.isEmpty();
      }

      public void clear() {
         if (this.qs != null) {
            this.qs.clear();
         }

      }

      public int size() {
         return this.qs != null ? this.qs.size() : 0;
      }
   }
}
