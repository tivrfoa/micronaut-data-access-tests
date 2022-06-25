package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiConsumer;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class MonoHandleFuseable<T, R> extends InternalMonoOperator<T, R> implements Fuseable {
   final BiConsumer<? super T, SynchronousSink<R>> handler;

   MonoHandleFuseable(Mono<? extends T> source, BiConsumer<? super T, SynchronousSink<R>> handler) {
      super(source);
      this.handler = (BiConsumer)Objects.requireNonNull(handler, "handler");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return new FluxHandleFuseable.HandleFuseableSubscriber<>(actual, this.handler);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
