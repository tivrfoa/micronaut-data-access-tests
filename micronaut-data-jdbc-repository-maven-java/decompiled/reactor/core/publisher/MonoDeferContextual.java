package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class MonoDeferContextual<T> extends Mono<T> implements SourceProducer<T> {
   final Function<ContextView, ? extends Mono<? extends T>> contextualMonoFactory;

   MonoDeferContextual(Function<ContextView, ? extends Mono<? extends T>> contextualMonoFactory) {
      this.contextualMonoFactory = (Function)Objects.requireNonNull(contextualMonoFactory, "contextualMonoFactory");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Context ctx = actual.currentContext();

      Mono<? extends T> p;
      try {
         p = (Mono)Objects.requireNonNull(this.contextualMonoFactory.apply(ctx), "The Mono returned by the contextualMonoFactory is null");
      } catch (Throwable var5) {
         Operators.error(actual, Operators.onOperatorError(var5, ctx));
         return;
      }

      p.subscribe(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
