package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;

final class FluxOnErrorResume<T> extends InternalFluxOperator<T, T> {
   final Function<? super Throwable, ? extends Publisher<? extends T>> nextFactory;

   FluxOnErrorResume(Flux<? extends T> source, Function<? super Throwable, ? extends Publisher<? extends T>> nextFactory) {
      super(source);
      this.nextFactory = (Function)Objects.requireNonNull(nextFactory, "nextFactory");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxOnErrorResume.ResumeSubscriber<>(actual, this.nextFactory);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ResumeSubscriber<T> extends Operators.MultiSubscriptionSubscriber<T, T> {
      final Function<? super Throwable, ? extends Publisher<? extends T>> nextFactory;
      boolean second;

      ResumeSubscriber(CoreSubscriber<? super T> actual, Function<? super Throwable, ? extends Publisher<? extends T>> nextFactory) {
         super(actual);
         this.nextFactory = nextFactory;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (!this.second) {
            this.actual.onSubscribe(this);
         }

         this.set(s);
      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
         if (!this.second) {
            this.producedOne();
         }

      }

      @Override
      public void onError(Throwable t) {
         if (!this.second) {
            this.second = true;

            Publisher<? extends T> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.nextFactory.apply(t), "The nextFactory returned a null Publisher");
            } catch (Throwable var5) {
               Throwable _e = Operators.onOperatorError(var5, this.actual.currentContext());
               _e = Exceptions.addSuppressed(_e, t);
               this.actual.onError(_e);
               return;
            }

            p.subscribe(this);
         } else {
            this.actual.onError(t);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
