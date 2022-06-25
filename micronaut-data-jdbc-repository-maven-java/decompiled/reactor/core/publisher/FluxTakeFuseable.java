package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxTakeFuseable<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final long n;

   FluxTakeFuseable(Flux<? extends T> source, long n) {
      super(source);
      if (n < 0L) {
         throw new IllegalArgumentException("n >= 0 required but it was " + n);
      } else {
         this.n = n;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxTake.TakeFuseableSubscriber<>(actual, this.n);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
