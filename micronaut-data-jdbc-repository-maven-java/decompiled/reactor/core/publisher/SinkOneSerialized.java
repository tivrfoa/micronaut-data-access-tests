package reactor.core.publisher;

public class SinkOneSerialized<T> extends SinkEmptySerialized<T> implements InternalOneSink<T>, ContextHolder {
   final Sinks.One<T> sinkOne;

   public SinkOneSerialized(Sinks.One<T> sinkOne, ContextHolder contextHolder) {
      super(sinkOne, contextHolder);
      this.sinkOne = sinkOne;
   }

   @Override
   public Sinks.EmitResult tryEmitValue(T t) {
      Thread currentThread = Thread.currentThread();
      if (!this.tryAcquire(currentThread)) {
         return Sinks.EmitResult.FAIL_NON_SERIALIZED;
      } else {
         Sinks.EmitResult var3;
         try {
            var3 = this.sinkOne.tryEmitValue(t);
         } finally {
            if (WIP.decrementAndGet(this) == 0) {
               LOCKED_AT.compareAndSet(this, currentThread, null);
            }

         }

         return var3;
      }
   }
}
