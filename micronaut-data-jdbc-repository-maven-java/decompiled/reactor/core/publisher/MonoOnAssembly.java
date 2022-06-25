package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class MonoOnAssembly<T> extends InternalMonoOperator<T, T> implements Fuseable, AssemblyOp {
   final FluxOnAssembly.AssemblySnapshot stacktrace;

   MonoOnAssembly(Mono<? extends T> source, FluxOnAssembly.AssemblySnapshot stacktrace) {
      super(source);
      this.stacktrace = stacktrace;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super T> cs = (Fuseable.ConditionalSubscriber)actual;
         return new FluxOnAssembly.OnAssemblyConditionalSubscriber<>(cs, this.stacktrace, this.source, this);
      } else {
         return new FluxOnAssembly.OnAssemblySubscriber<>(actual, this.stacktrace, this.source, this);
      }
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
