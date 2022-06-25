package reactor.core.publisher;

import java.util.Objects;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

@Deprecated
final class DelegateProcessor<IN, OUT> extends FluxProcessor<IN, OUT> {
   final Publisher<OUT> downstream;
   final Subscriber<IN> upstream;

   DelegateProcessor(Publisher<OUT> downstream, Subscriber<IN> upstream) {
      this.downstream = (Publisher)Objects.requireNonNull(downstream, "Downstream must not be null");
      this.upstream = (Subscriber)Objects.requireNonNull(upstream, "Upstream must not be null");
   }

   @Override
   public Context currentContext() {
      return this.upstream instanceof CoreSubscriber ? ((CoreSubscriber)this.upstream).currentContext() : Context.empty();
   }

   @Override
   public void onComplete() {
      this.upstream.onComplete();
   }

   @Override
   public void onError(Throwable t) {
      this.upstream.onError(t);
   }

   @Override
   public void onNext(IN in) {
      this.upstream.onNext(in);
   }

   @Override
   public void onSubscribe(Subscription s) {
      this.upstream.onSubscribe(s);
   }

   @Override
   public void subscribe(CoreSubscriber<? super OUT> actual) {
      Objects.requireNonNull(actual, "subscribe");
      this.downstream.subscribe(actual);
   }

   @Override
   public boolean isSerialized() {
      return this.upstream instanceof SerializedSubscriber || this.upstream instanceof FluxProcessor && ((FluxProcessor)this.upstream).isSerialized();
   }

   @Override
   public Stream<? extends Scannable> inners() {
      return Scannable.from(this.upstream).inners();
   }

   @Override
   public int getBufferSize() {
      return Scannable.from(this.upstream).scanOrDefault(Scannable.Attr.CAPACITY, super.getBufferSize());
   }

   @Nullable
   @Override
   public Throwable getError() {
      return Scannable.from(this.upstream).scanOrDefault(Scannable.Attr.ERROR, super.getError());
   }

   @Override
   public boolean isTerminated() {
      return Scannable.from(this.upstream).scanOrDefault(Scannable.Attr.TERMINATED, super.isTerminated());
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.PARENT ? this.downstream : Scannable.from(this.upstream).scanUnsafe(key);
   }
}
