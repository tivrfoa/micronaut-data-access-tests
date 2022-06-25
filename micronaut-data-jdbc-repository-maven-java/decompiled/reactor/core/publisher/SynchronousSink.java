package reactor.core.publisher;

import reactor.util.context.Context;
import reactor.util.context.ContextView;

public interface SynchronousSink<T> {
   void complete();

   @Deprecated
   Context currentContext();

   default ContextView contextView() {
      return this.currentContext();
   }

   void error(Throwable var1);

   void next(T var1);
}
