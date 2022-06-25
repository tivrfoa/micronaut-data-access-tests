package reactor.core.publisher;

import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxLiftFuseable<I, O> extends InternalFluxOperator<I, O> implements Fuseable {
   final Operators.LiftFunction<I, O> liftFunction;

   FluxLiftFuseable(Publisher<I> p, Operators.LiftFunction<I, O> liftFunction) {
      super(Flux.from(p));
      this.liftFunction = liftFunction;
   }

   @Override
   public String stepName() {
      return this.source instanceof Scannable ? Scannable.from(this.source).stepName() : super.stepName();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_STYLE) {
         return Scannable.from(this.source).scanUnsafe(key);
      } else {
         return key == Scannable.Attr.LIFTER ? this.liftFunction.name : super.scanUnsafe(key);
      }
   }

   @Override
   public CoreSubscriber<? super I> subscribeOrReturn(CoreSubscriber<? super O> actual) {
      CoreSubscriber<? super I> input = (CoreSubscriber)this.liftFunction.lifter.apply(this.source, actual);
      Objects.requireNonNull(input, "Lifted subscriber MUST NOT be null");
      if (actual instanceof Fuseable.QueueSubscription && !(input instanceof Fuseable.QueueSubscription)) {
         input = new FluxHide.SuppressFuseableSubscriber<>(input);
      }

      return input;
   }
}
