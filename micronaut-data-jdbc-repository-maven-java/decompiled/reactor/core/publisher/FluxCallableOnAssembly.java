package reactor.core.publisher;

import java.util.concurrent.Callable;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxCallableOnAssembly<T> extends InternalFluxOperator<T, T> implements Fuseable, Callable<T>, AssemblyOp {
   final FluxOnAssembly.AssemblySnapshot stacktrace;

   FluxCallableOnAssembly(Flux<? extends T> source, FluxOnAssembly.AssemblySnapshot stacktrace) {
      super(source);
      this.stacktrace = stacktrace;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return FluxOnAssembly.wrapSubscriber(actual, this.source, this, this.stacktrace);
   }

   public T call() throws Exception {
      return (T)((Callable)this.source).call();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.ACTUAL_METADATA) {
         return !this.stacktrace.isCheckpoint;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   @Override
   public String stepName() {
      return this.stacktrace.operatorAssemblyInformation();
   }

   @Override
   public String toString() {
      return this.stacktrace.operatorAssemblyInformation();
   }
}
