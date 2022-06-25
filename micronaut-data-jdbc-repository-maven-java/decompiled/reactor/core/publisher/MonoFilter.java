package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Predicate;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class MonoFilter<T> extends InternalMonoOperator<T, T> {
   final Predicate<? super T> predicate;

   MonoFilter(Mono<? extends T> source, Predicate<? super T> predicate) {
      super(source);
      this.predicate = (Predicate)Objects.requireNonNull(predicate, "predicate");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxFilter.FilterConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this.predicate)
         : new FluxFilter.FilterSubscriber<>(actual, this.predicate));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
