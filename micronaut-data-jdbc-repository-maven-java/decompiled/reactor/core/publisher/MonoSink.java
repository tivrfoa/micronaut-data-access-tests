package reactor.core.publisher;

import java.util.function.LongConsumer;
import reactor.core.Disposable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public interface MonoSink<T> {
   void success();

   void success(@Nullable T var1);

   void error(Throwable var1);

   @Deprecated
   Context currentContext();

   default ContextView contextView() {
      return this.currentContext();
   }

   MonoSink<T> onRequest(LongConsumer var1);

   MonoSink<T> onCancel(Disposable var1);

   MonoSink<T> onDispose(Disposable var1);
}
