package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoRepeatPredicate<T> extends FluxFromMonoOperator<T, T> {
   final BooleanSupplier predicate;

   MonoRepeatPredicate(Mono<? extends T> source, BooleanSupplier predicate) {
      super(source);
      this.predicate = (BooleanSupplier)Objects.requireNonNull(predicate, "predicate");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxRepeatPredicate.RepeatPredicateSubscriber<T> parent = new FluxRepeatPredicate.RepeatPredicateSubscriber<>(this.source, actual, this.predicate);
      actual.onSubscribe(parent);
      if (!parent.isCancelled()) {
         parent.resubscribe();
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
