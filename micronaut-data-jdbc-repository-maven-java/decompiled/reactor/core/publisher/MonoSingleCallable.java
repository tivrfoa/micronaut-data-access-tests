package reactor.core.publisher;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Callable;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoSingleCallable<T> extends Mono<T> implements Callable<T>, SourceProducer<T> {
   final Callable<? extends T> callable;
   @Nullable
   final T defaultValue;

   MonoSingleCallable(Callable<? extends T> source) {
      this.callable = (Callable)Objects.requireNonNull(source, "source");
      this.defaultValue = null;
   }

   MonoSingleCallable(Callable<? extends T> source, T defaultValue) {
      this.callable = (Callable)Objects.requireNonNull(source, "source");
      this.defaultValue = (T)Objects.requireNonNull(defaultValue, "defaultValue");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Operators.MonoInnerProducerBase<T> sds = new Operators.MonoInnerProducerBase<>(actual);
      actual.onSubscribe(sds);
      if (!sds.isCancelled()) {
         try {
            T t = (T)this.callable.call();
            if (t == null && this.defaultValue == null) {
               actual.onError(new NoSuchElementException("Source was empty"));
            } else if (t == null) {
               sds.complete(this.defaultValue);
            } else {
               sds.complete(t);
            }
         } catch (Throwable var4) {
            actual.onError(Operators.onOperatorError(var4, actual.currentContext()));
         }

      }
   }

   @Override
   public T block() {
      return this.block(Duration.ZERO);
   }

   @Override
   public T block(Duration m) {
      T v;
      try {
         v = (T)this.callable.call();
      } catch (Throwable var4) {
         throw Exceptions.propagate(var4);
      }

      if (v == null && this.defaultValue == null) {
         throw new NoSuchElementException("Source was empty");
      } else {
         return (T)(v == null ? this.defaultValue : v);
      }
   }

   public T call() throws Exception {
      T v = (T)this.callable.call();
      if (v == null && this.defaultValue == null) {
         throw new NoSuchElementException("Source was empty");
      } else {
         return (T)(v == null ? this.defaultValue : v);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
