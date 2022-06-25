package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.retry.Retry;

final class MonoRetryWhen<T> extends InternalMonoOperator<T, T> {
   final Retry whenSourceFactory;

   MonoRetryWhen(Mono<? extends T> source, Retry whenSourceFactory) {
      super(source);
      this.whenSourceFactory = (Retry)Objects.requireNonNull(whenSourceFactory, "whenSourceFactory");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxRetryWhen.subscribe(actual, this.whenSourceFactory, this.source);
      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
