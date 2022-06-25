package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class GroupedLiftFuseable<K, I, O> extends GroupedFlux<K, O> implements Scannable, Fuseable {
   final Operators.LiftFunction<I, O> liftFunction;
   final GroupedFlux<K, I> source;

   GroupedLiftFuseable(GroupedFlux<K, I> p, Operators.LiftFunction<I, O> liftFunction) {
      this.source = (GroupedFlux)Objects.requireNonNull(p, "source");
      this.liftFunction = liftFunction;
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Override
   public K key() {
      return this.source.key();
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.RUN_STYLE) {
         return Scannable.from(this.source).scanUnsafe(key);
      } else {
         return key == Scannable.Attr.LIFTER ? this.liftFunction.name : null;
      }
   }

   @Override
   public String stepName() {
      return this.source instanceof Scannable ? Scannable.from(this.source).stepName() : Scannable.super.stepName();
   }

   @Override
   public void subscribe(CoreSubscriber<? super O> actual) {
      CoreSubscriber<? super I> input = (CoreSubscriber)this.liftFunction.lifter.apply(this.source, actual);
      Objects.requireNonNull(input, "Lifted subscriber MUST NOT be null");
      if (actual instanceof Fuseable.QueueSubscription && !(input instanceof Fuseable.QueueSubscription)) {
         input = new FluxHide.SuppressFuseableSubscriber<>(input);
      }

      this.source.subscribe(input);
   }
}
