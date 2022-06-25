package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoRepeatWhen<T> extends FluxFromMonoOperator<T, T> {
   final Function<? super Flux<Long>, ? extends Publisher<?>> whenSourceFactory;

   MonoRepeatWhen(Mono<? extends T> source, Function<? super Flux<Long>, ? extends Publisher<?>> whenSourceFactory) {
      super(source);
      this.whenSourceFactory = (Function)Objects.requireNonNull(whenSourceFactory, "whenSourceFactory");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxRepeatWhen.RepeatWhenOtherSubscriber other = new FluxRepeatWhen.RepeatWhenOtherSubscriber();
      CoreSubscriber<T> serial = Operators.serialize(actual);
      FluxRepeatWhen.RepeatWhenMainSubscriber<T> main = new FluxRepeatWhen.RepeatWhenMainSubscriber<>(serial, other.completionSignal, this.source);
      other.main = main;
      serial.onSubscribe(main);

      Publisher<?> p;
      try {
         p = (Publisher)Objects.requireNonNull(this.whenSourceFactory.apply(other), "The whenSourceFactory returned a null Publisher");
      } catch (Throwable var7) {
         actual.onError(Operators.onOperatorError(var7, actual.currentContext()));
         return null;
      }

      p.subscribe(other);
      return !main.cancelled ? main : null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
