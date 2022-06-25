package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class SinkOneMulticast<O> extends SinkEmptyMulticast<O> implements InternalOneSink<O> {
   @Nullable
   O value;

   @Override
   public Sinks.EmitResult tryEmitEmpty() {
      return this.tryEmitValue((O)null);
   }

   @Override
   public Sinks.EmitResult tryEmitError(Throwable cause) {
      Objects.requireNonNull(cause, "onError cannot be null");
      SinkEmptyMulticast.Inner<O>[] prevSubscribers = (SinkEmptyMulticast.Inner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
      if (prevSubscribers == TERMINATED) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         this.error = cause;
         this.value = null;

         for(SinkEmptyMulticast.Inner<O> as : prevSubscribers) {
            as.error(cause);
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public Sinks.EmitResult tryEmitValue(@Nullable O value) {
      SinkEmptyMulticast.Inner<O>[] array = (SinkEmptyMulticast.Inner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
      if (array == TERMINATED) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         this.value = value;
         if (value == null) {
            for(SinkEmptyMulticast.Inner<O> as : array) {
               as.complete();
            }
         } else {
            for(SinkEmptyMulticast.Inner<O> as : array) {
               as.complete(value);
            }
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED) {
         return this.subscribers == TERMINATED;
      } else if (key == Scannable.Attr.ERROR) {
         return this.error;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super O> actual) {
      SinkOneMulticast.NextInner<O> as = new SinkOneMulticast.NextInner<>(actual, this);
      actual.onSubscribe(as);
      if (this.add(as)) {
         if (as.isCancelled()) {
            this.remove(as);
         }
      } else {
         Throwable ex = this.error;
         if (ex != null) {
            actual.onError(ex);
         } else {
            O v = this.value;
            if (v != null) {
               as.complete(v);
            } else {
               as.complete();
            }
         }
      }

   }

   @Nullable
   @Override
   public O block(Duration timeout) {
      return (O)(timeout.isNegative() ? super.block(Duration.ZERO) : super.block(timeout));
   }

   static final class NextInner<T> extends Operators.MonoInnerProducerBase<T> implements SinkEmptyMulticast.Inner<T> {
      final SinkOneMulticast<T> parent;

      NextInner(CoreSubscriber<? super T> actual, SinkOneMulticast<T> parent) {
         super(actual);
         this.parent = parent;
      }

      @Override
      protected void doOnCancel() {
         this.parent.remove(this);
      }

      @Override
      public void error(Throwable t) {
         if (!this.isCancelled()) {
            this.actual().onError(t);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
