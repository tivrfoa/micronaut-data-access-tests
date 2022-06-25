package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxZipIterable<T, U, R> extends InternalFluxOperator<T, R> {
   final Iterable<? extends U> other;
   final BiFunction<? super T, ? super U, ? extends R> zipper;

   FluxZipIterable(Flux<? extends T> source, Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
      super(source);
      this.other = (Iterable)Objects.requireNonNull(other, "other");
      this.zipper = (BiFunction)Objects.requireNonNull(zipper, "zipper");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      Iterator<? extends U> it = (Iterator)Objects.requireNonNull(this.other.iterator(), "The other iterable produced a null iterator");
      boolean b = it.hasNext();
      if (!b) {
         Operators.complete(actual);
         return null;
      } else {
         return new FluxZipIterable.ZipSubscriber<>(actual, it, this.zipper);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ZipSubscriber<T, U, R> implements InnerOperator<T, R> {
      final CoreSubscriber<? super R> actual;
      final Iterator<? extends U> it;
      final BiFunction<? super T, ? super U, ? extends R> zipper;
      Subscription s;
      boolean done;

      ZipSubscriber(CoreSubscriber<? super R> actual, Iterator<? extends U> it, BiFunction<? super T, ? super U, ? extends R> zipper) {
         this.actual = actual;
         this.it = it;
         this.zipper = zipper;
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
            U u;
            try {
               u = (U)this.it.next();
            } catch (Throwable var8) {
               this.done = true;
               this.actual.onError(Operators.onOperatorError(this.s, var8, t, this.actual.currentContext()));
               return;
            }

            R r;
            try {
               r = (R)Objects.requireNonNull(this.zipper.apply(t, u), "The zipper returned a null value");
            } catch (Throwable var7) {
               this.done = true;
               this.actual.onError(Operators.onOperatorError(this.s, var7, t, this.actual.currentContext()));
               return;
            }

            this.actual.onNext(r);

            boolean b;
            try {
               b = this.it.hasNext();
            } catch (Throwable var6) {
               this.done = true;
               this.actual.onError(Operators.onOperatorError(this.s, var6, t, this.actual.currentContext()));
               return;
            }

            if (!b) {
               this.done = true;
               this.s.cancel();
               this.actual.onComplete();
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
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }
   }
}
