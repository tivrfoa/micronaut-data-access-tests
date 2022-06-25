package reactor.core.publisher;

import java.util.function.LongConsumer;
import reactor.core.Disposable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public interface FluxSink<T> {
   FluxSink<T> next(T var1);

   void complete();

   void error(Throwable var1);

   @Deprecated
   Context currentContext();

   default ContextView contextView() {
      return this.currentContext();
   }

   long requestedFromDownstream();

   boolean isCancelled();

   FluxSink<T> onRequest(LongConsumer var1);

   FluxSink<T> onCancel(Disposable var1);

   FluxSink<T> onDispose(Disposable var1);

   public static enum OverflowStrategy {
      IGNORE,
      ERROR,
      DROP,
      LATEST,
      BUFFER;
   }
}
