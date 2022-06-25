package reactor.core.publisher;

import java.time.Duration;
import java.util.concurrent.Callable;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoCallableOnAssembly<T> extends InternalMonoOperator<T, T> implements Callable<T>, AssemblyOp {
   final FluxOnAssembly.AssemblySnapshot stacktrace;

   MonoCallableOnAssembly(Mono<? extends T> source, FluxOnAssembly.AssemblySnapshot stacktrace) {
      super(source);
      this.stacktrace = stacktrace;
   }

   @Nullable
   @Override
   public T block() {
      return this.block(Duration.ZERO);
   }

   @Nullable
   @Override
   public T block(Duration timeout) {
      try {
         return (T)((Callable)this.source).call();
      } catch (Throwable var3) {
         throw Exceptions.propagate(var3);
      }
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

   @Nullable
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
