package reactor.core.publisher;

import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxSwitchIfEmpty<T> extends InternalFluxOperator<T, T> {
   final Publisher<? extends T> other;

   FluxSwitchIfEmpty(Flux<? extends T> source, Publisher<? extends T> other) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxSwitchIfEmpty.SwitchIfEmptySubscriber<T> parent = new FluxSwitchIfEmpty.SwitchIfEmptySubscriber<>(actual, this.other);
      actual.onSubscribe(parent);
      return parent;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class SwitchIfEmptySubscriber<T> extends Operators.MultiSubscriptionSubscriber<T, T> {
      final Publisher<? extends T> other;
      boolean once;

      SwitchIfEmptySubscriber(CoreSubscriber<? super T> actual, Publisher<? extends T> other) {
         super(actual);
         this.other = other;
      }

      @Override
      public void onNext(T t) {
         if (!this.once) {
            this.once = true;
         }

         this.actual.onNext(t);
      }

      @Override
      public void onComplete() {
         if (!this.once) {
            this.once = true;
            this.other.subscribe(this);
         } else {
            this.actual.onComplete();
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
