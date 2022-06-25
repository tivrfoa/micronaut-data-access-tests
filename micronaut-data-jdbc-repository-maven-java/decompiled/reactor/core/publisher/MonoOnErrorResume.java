package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoOnErrorResume<T> extends InternalMonoOperator<T, T> {
   final Function<? super Throwable, ? extends Publisher<? extends T>> nextFactory;

   MonoOnErrorResume(Mono<? extends T> source, Function<? super Throwable, ? extends Mono<? extends T>> nextFactory) {
      super(source);
      this.nextFactory = (Function)Objects.requireNonNull(nextFactory, "nextFactory");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxOnErrorResume.ResumeSubscriber<>(actual, this.nextFactory);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
