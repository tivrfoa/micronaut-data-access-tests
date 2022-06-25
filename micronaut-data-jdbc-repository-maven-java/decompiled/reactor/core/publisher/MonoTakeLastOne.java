package reactor.core.publisher;

import java.util.NoSuchElementException;
import java.util.Objects;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoTakeLastOne<T> extends MonoFromFluxOperator<T, T> implements Fuseable {
   final T defaultValue;

   MonoTakeLastOne(Flux<? extends T> source) {
      super(source);
      this.defaultValue = null;
   }

   MonoTakeLastOne(Flux<? extends T> source, T defaultValue) {
      super(source);
      this.defaultValue = (T)Objects.requireNonNull(defaultValue, "defaultValue");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoTakeLastOne.TakeLastOneSubscriber<>(actual, this.defaultValue, true);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class TakeLastOneSubscriber<T> extends Operators.MonoSubscriber<T, T> {
      final boolean mustEmit;
      final T defaultValue;
      Subscription s;

      TakeLastOneSubscriber(CoreSubscriber<? super T> actual, @Nullable T defaultValue, boolean mustEmit) {
         super(actual);
         this.defaultValue = defaultValue;
         this.mustEmit = mustEmit;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void onNext(T t) {
         T old = this.value;
         this.setValue(t);
         Operators.onDiscard(old, this.actual.currentContext());
      }

      @Override
      public void onComplete() {
         T v = this.value;
         if (v == null) {
            if (this.mustEmit) {
               if (this.defaultValue != null) {
                  this.complete(this.defaultValue);
               } else {
                  this.actual
                     .onError(
                        Operators.onOperatorError(new NoSuchElementException("Flux#last() didn't observe any onNext signal"), this.actual.currentContext())
                     );
               }
            } else {
               this.actual.onComplete();
            }

         } else {
            this.complete(v);
         }
      }

      @Override
      public void cancel() {
         super.cancel();
         this.s.cancel();
      }
   }
}
