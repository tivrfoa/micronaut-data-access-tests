package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxStream<T> extends Flux<T> implements Fuseable, SourceProducer<T> {
   final Supplier<? extends Stream<? extends T>> streamSupplier;

   FluxStream(Supplier<? extends Stream<? extends T>> streamSupplier) {
      this.streamSupplier = (Supplier)Objects.requireNonNull(streamSupplier, "streamSupplier");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Stream<? extends T> stream;
      try {
         stream = (Stream)Objects.requireNonNull(this.streamSupplier.get(), "The stream supplier returned a null Stream");
      } catch (Throwable var7) {
         Operators.error(actual, Operators.onOperatorError(var7, actual.currentContext()));
         return;
      }

      Iterator<? extends T> it;
      boolean knownToBeFinite;
      try {
         Spliterator<? extends T> spliterator = (Spliterator)Objects.requireNonNull(stream.spliterator(), "The stream returned a null Spliterator");
         knownToBeFinite = spliterator.hasCharacteristics(64);
         it = Spliterators.iterator(spliterator);
      } catch (Throwable var6) {
         Operators.error(actual, Operators.onOperatorError(var6, actual.currentContext()));
         return;
      }

      FluxIterable.subscribe(actual, it, knownToBeFinite, stream::close);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
