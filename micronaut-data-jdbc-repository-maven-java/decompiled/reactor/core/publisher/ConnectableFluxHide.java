package reactor.core.publisher;

import java.util.function.Consumer;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ConnectableFluxHide<T> extends InternalConnectableFluxOperator<T, T> implements Scannable {
   ConnectableFluxHide(ConnectableFlux<T> source) {
      super(source);
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public final CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return actual;
   }

   @Override
   public void connect(Consumer<? super Disposable> cancelSupport) {
      this.source.connect(cancelSupport);
   }
}
