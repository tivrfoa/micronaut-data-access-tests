package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class FluxDeferContextual<T> extends Flux<T> implements SourceProducer<T> {
   final Function<ContextView, ? extends Publisher<? extends T>> contextualPublisherFactory;

   FluxDeferContextual(Function<ContextView, ? extends Publisher<? extends T>> contextualPublisherFactory) {
      this.contextualPublisherFactory = (Function)Objects.requireNonNull(contextualPublisherFactory, "contextualPublisherFactory");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Context ctx = actual.currentContext();

      Publisher<? extends T> p;
      try {
         p = (Publisher)Objects.requireNonNull(
            this.contextualPublisherFactory.apply(ctx.readOnly()), "The Publisher returned by the contextualPublisherFactory is null"
         );
      } catch (Throwable var5) {
         Operators.error(actual, Operators.onOperatorError(var5, ctx));
         return;
      }

      from(p).subscribe(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
