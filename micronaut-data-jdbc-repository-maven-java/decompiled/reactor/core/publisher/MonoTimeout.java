package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoTimeout<T, U, V> extends InternalMonoOperator<T, T> {
   final Publisher<U> firstTimeout;
   final Publisher<? extends T> other;
   final String timeoutDescription;
   static final Function NEVER = e -> Flux.never();

   MonoTimeout(Mono<? extends T> source, Publisher<U> firstTimeout, String timeoutDescription) {
      super(source);
      this.firstTimeout = (Publisher)Objects.requireNonNull(firstTimeout, "firstTimeout");
      this.other = null;
      this.timeoutDescription = timeoutDescription;
   }

   MonoTimeout(Mono<? extends T> source, Publisher<U> firstTimeout, Publisher<? extends T> other) {
      super(source);
      this.firstTimeout = (Publisher)Objects.requireNonNull(firstTimeout, "firstTimeout");
      this.other = (Publisher)Objects.requireNonNull(other, "other");
      this.timeoutDescription = null;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxTimeout.TimeoutMainSubscriber<>(
         Operators.serialize(actual), this.firstTimeout, NEVER, this.other, FluxTimeout.addNameToTimeoutDescription(this.source, this.timeoutDescription)
      );
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
