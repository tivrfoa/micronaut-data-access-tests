package reactor.core.publisher;

import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoTakeUntilOther<T, U> extends InternalMonoOperator<T, T> {
   private final Publisher<U> other;

   MonoTakeUntilOther(Mono<? extends T> source, Publisher<U> other) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxTakeUntilOther.TakeUntilMainSubscriber<T> mainSubscriber = new FluxTakeUntilOther.TakeUntilMainSubscriber<>(actual);
      FluxTakeUntilOther.TakeUntilOtherSubscriber<U> otherSubscriber = new FluxTakeUntilOther.TakeUntilOtherSubscriber<>(mainSubscriber);
      this.other.subscribe(otherSubscriber);
      return mainSubscriber;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
