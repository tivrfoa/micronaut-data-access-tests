package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxDistinctFuseable<T, K, C> extends InternalFluxOperator<T, T> implements Fuseable {
   final Function<? super T, ? extends K> keyExtractor;
   final Supplier<C> collectionSupplier;
   final BiPredicate<C, K> distinctPredicate;
   final Consumer<C> cleanupCallback;

   FluxDistinctFuseable(
      Flux<? extends T> source,
      Function<? super T, ? extends K> keyExtractor,
      Supplier<C> collectionSupplier,
      BiPredicate<C, K> distinctPredicate,
      Consumer<C> cleanupCallback
   ) {
      super(source);
      this.keyExtractor = (Function)Objects.requireNonNull(keyExtractor, "keyExtractor");
      this.collectionSupplier = (Supplier)Objects.requireNonNull(collectionSupplier, "collectionSupplier");
      this.distinctPredicate = (BiPredicate)Objects.requireNonNull(distinctPredicate, "distinctPredicate");
      this.cleanupCallback = (Consumer)Objects.requireNonNull(cleanupCallback, "cleanupCallback");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      C collection = (C)Objects.requireNonNull(this.collectionSupplier.get(), "The collectionSupplier returned a null collection");
      return new FluxDistinct.DistinctFuseableSubscriber<>(actual, collection, this.keyExtractor, this.distinctPredicate, this.cleanupCallback);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
