package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.context.Context;

final class MonoContextWrite<T> extends InternalMonoOperator<T, T> implements Fuseable {
   final Function<Context, Context> doOnContext;

   MonoContextWrite(Mono<? extends T> source, Function<Context, Context> doOnContext) {
      super(source);
      this.doOnContext = (Function)Objects.requireNonNull(doOnContext, "doOnContext");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      Context c = (Context)this.doOnContext.apply(actual.currentContext());
      return new FluxContextWrite.ContextWriteSubscriber<>(actual, c);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
