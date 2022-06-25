package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelLift<I, O> extends ParallelFlux<O> implements Scannable {
   final Operators.LiftFunction<I, O> liftFunction;
   final ParallelFlux<I> source;

   ParallelLift(ParallelFlux<I> p, Operators.LiftFunction<I, O> liftFunction) {
      this.source = (ParallelFlux)Objects.requireNonNull(p, "source");
      this.liftFunction = liftFunction;
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
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
   public void subscribe(CoreSubscriber<? super O>[] s) {
      CoreSubscriber<? super I>[] subscribers = new CoreSubscriber[this.parallelism()];

      for(int i = 0; i < subscribers.length; ++i) {
         subscribers[i] = (CoreSubscriber)Objects.requireNonNull(this.liftFunction.lifter.apply(this.source, s[i]), "Lifted subscriber MUST NOT be null");
      }

      this.source.subscribe(subscribers);
   }
}
