package reactor.core.publisher;

import java.util.Objects;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoElementAt<T> extends MonoFromFluxOperator<T, T> implements Fuseable {
   final long index;
   final T defaultValue;

   MonoElementAt(Flux<? extends T> source, long index) {
      super(source);
      if (index < 0L) {
         throw new IndexOutOfBoundsException("index >= required but it was " + index);
      } else {
         this.index = index;
         this.defaultValue = null;
      }
   }

   MonoElementAt(Flux<? extends T> source, long index, T defaultValue) {
      super(source);
      if (index < 0L) {
         throw new IndexOutOfBoundsException("index >= required but it was " + index);
      } else {
         this.index = index;
         this.defaultValue = (T)Objects.requireNonNull(defaultValue, "defaultValue");
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoElementAt.ElementAtSubscriber<>(actual, this.index, this.defaultValue);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ElementAtSubscriber<T> extends Operators.MonoSubscriber<T, T> {
      final T defaultValue;
      long index;
      final long target;
      Subscription s;
      boolean done;

      ElementAtSubscriber(CoreSubscriber<? super T> actual, long index, T defaultValue) {
         super(actual);
         this.index = index;
         this.target = index;
         this.defaultValue = defaultValue;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         super.request(n);
         if (n > 0L) {
            this.s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void cancel() {
         super.cancel();
         this.s.cancel();
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
            long i = this.index;
            if (i == 0L) {
               this.done = true;
               this.s.cancel();
               this.actual.onNext(t);
               this.actual.onComplete();
            } else {
               this.index = i - 1L;
               Operators.onDiscard(t, this.actual.currentContext());
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
            if (this.defaultValue != null) {
               this.complete(this.defaultValue);
            } else {
               long count = this.target - this.index;
               this.actual
                  .onError(
                     Operators.onOperatorError(
                        new IndexOutOfBoundsException("source had " + count + " elements, expected at least " + (this.target + 1L)),
                        this.actual.currentContext()
                     )
                  );
            }

         }
      }
   }
}
