package reactor.core.publisher;

import java.util.Objects;
import java.util.stream.Stream;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class SinkManySerialized<T> extends SinksSpecs.AbstractSerializedSink implements InternalManySink<T>, Scannable {
   final Sinks.Many<T> sink;
   final ContextHolder contextHolder;

   SinkManySerialized(Sinks.Many<T> sink, ContextHolder contextHolder) {
      this.sink = sink;
      this.contextHolder = contextHolder;
   }

   @Override
   public int currentSubscriberCount() {
      return this.sink.currentSubscriberCount();
   }

   @Override
   public Flux<T> asFlux() {
      return this.sink.asFlux();
   }

   @Override
   public Context currentContext() {
      return this.contextHolder.currentContext();
   }

   public boolean isCancelled() {
      return Scannable.from(this.sink).scanOrDefault(Scannable.Attr.CANCELLED, (T)false);
   }

   @Override
   public final Sinks.EmitResult tryEmitComplete() {
      Thread currentThread = Thread.currentThread();
      if (!this.tryAcquire(currentThread)) {
         return Sinks.EmitResult.FAIL_NON_SERIALIZED;
      } else {
         Sinks.EmitResult var2;
         try {
            var2 = this.sink.tryEmitComplete();
         } finally {
            if (WIP.decrementAndGet(this) == 0) {
               LOCKED_AT.compareAndSet(this, currentThread, null);
            }

         }

         return var2;
      }
   }

   @Override
   public final Sinks.EmitResult tryEmitError(Throwable t) {
      Objects.requireNonNull(t, "t is null in sink.error(t)");
      Thread currentThread = Thread.currentThread();
      if (!this.tryAcquire(currentThread)) {
         return Sinks.EmitResult.FAIL_NON_SERIALIZED;
      } else {
         Sinks.EmitResult var3;
         try {
            var3 = this.sink.tryEmitError(t);
         } finally {
            if (WIP.decrementAndGet(this) == 0) {
               LOCKED_AT.compareAndSet(this, currentThread, null);
            }

         }

         return var3;
      }
   }

   @Override
   public final Sinks.EmitResult tryEmitNext(T t) {
      Objects.requireNonNull(t, "t is null in sink.next(t)");
      Thread currentThread = Thread.currentThread();
      if (!this.tryAcquire(currentThread)) {
         return Sinks.EmitResult.FAIL_NON_SERIALIZED;
      } else {
         Sinks.EmitResult var3;
         try {
            var3 = this.sink.tryEmitNext(t);
         } finally {
            if (WIP.decrementAndGet(this) == 0) {
               LOCKED_AT.compareAndSet(this, currentThread, null);
            }

         }

         return var3;
      }
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return this.sink.scanUnsafe(key);
   }

   @Override
   public Stream<? extends Scannable> inners() {
      return Scannable.from(this.sink).inners();
   }

   public String toString() {
      return this.sink.toString();
   }
}
