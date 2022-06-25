package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class MonoMapFuseable<T, R> extends InternalMonoOperator<T, R> implements Fuseable {
   final Function<? super T, ? extends R> mapper;

   MonoMapFuseable(Mono<? extends T> source, Function<? super T, ? extends R> mapper) {
      super(source);
      this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super R> cs = (Fuseable.ConditionalSubscriber)actual;
         return new FluxMapFuseable.MapFuseableConditionalSubscriber<>(cs, this.mapper);
      } else {
         return new FluxMapFuseable.MapFuseableSubscriber<>(actual, this.mapper);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
