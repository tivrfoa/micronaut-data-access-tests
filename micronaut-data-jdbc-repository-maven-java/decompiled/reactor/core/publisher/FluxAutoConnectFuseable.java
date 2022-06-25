package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxAutoConnectFuseable<T> extends Flux<T> implements Scannable, Fuseable {
   final ConnectableFlux<? extends T> source;
   final Consumer<? super Disposable> cancelSupport;
   volatile int remaining;
   static final AtomicIntegerFieldUpdater<FluxAutoConnectFuseable> REMAINING = AtomicIntegerFieldUpdater.newUpdater(FluxAutoConnectFuseable.class, "remaining");

   FluxAutoConnectFuseable(ConnectableFlux<? extends T> source, int n, Consumer<? super Disposable> cancelSupport) {
      if (n <= 0) {
         throw new IllegalArgumentException("n > required but it was " + n);
      } else {
         this.source = (ConnectableFlux)Objects.requireNonNull(source, "source");
         this.cancelSupport = (Consumer)Objects.requireNonNull(cancelSupport, "cancelSupport");
         REMAINING.lazySet(this, n);
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      this.source.subscribe(actual);
      if (this.remaining > 0 && REMAINING.decrementAndGet(this) == 0) {
         this.source.connect(this.cancelSupport);
      }

   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.CAPACITY) {
         return this.remaining;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }
}
