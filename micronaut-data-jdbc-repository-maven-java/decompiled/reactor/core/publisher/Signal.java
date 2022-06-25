package reactor.core.publisher;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public interface Signal<T> extends Supplier<T>, Consumer<Subscriber<? super T>> {
   static <T> Signal<T> complete() {
      return ImmutableSignal.onComplete();
   }

   static <T> Signal<T> complete(Context context) {
      return (Signal<T>)(context.isEmpty() ? ImmutableSignal.onComplete() : new ImmutableSignal<>(context, SignalType.ON_COMPLETE, (T)null, null, null));
   }

   static <T> Signal<T> error(Throwable e) {
      return error(e, Context.empty());
   }

   static <T> Signal<T> error(Throwable e, Context context) {
      return new ImmutableSignal<>(context, SignalType.ON_ERROR, (T)null, e, null);
   }

   static <T> Signal<T> next(T t) {
      return next(t, Context.empty());
   }

   static <T> Signal<T> next(T t, Context context) {
      return new ImmutableSignal<>(context, SignalType.ON_NEXT, t, null, null);
   }

   static <T> Signal<T> subscribe(Subscription subscription) {
      return subscribe(subscription, Context.empty());
   }

   static <T> Signal<T> subscribe(Subscription subscription, Context context) {
      return new ImmutableSignal<>(context, SignalType.ON_SUBSCRIBE, (T)null, null, subscription);
   }

   static boolean isComplete(Object o) {
      return o == ImmutableSignal.onComplete() || o instanceof Signal && ((Signal)o).getType() == SignalType.ON_COMPLETE;
   }

   static boolean isError(Object o) {
      return o instanceof Signal && ((Signal)o).getType() == SignalType.ON_ERROR;
   }

   @Nullable
   Throwable getThrowable();

   @Nullable
   Subscription getSubscription();

   @Nullable
   T get();

   default boolean hasValue() {
      return this.isOnNext() && this.get() != null;
   }

   default boolean hasError() {
      return this.isOnError() && this.getThrowable() != null;
   }

   SignalType getType();

   @Deprecated
   default Context getContext() {
      return Context.of(this.getContextView());
   }

   ContextView getContextView();

   default boolean isOnError() {
      return this.getType() == SignalType.ON_ERROR;
   }

   default boolean isOnComplete() {
      return this.getType() == SignalType.ON_COMPLETE;
   }

   default boolean isOnSubscribe() {
      return this.getType() == SignalType.ON_SUBSCRIBE;
   }

   default boolean isOnNext() {
      return this.getType() == SignalType.ON_NEXT;
   }

   default void accept(Subscriber<? super T> observer) {
      if (this.isOnNext()) {
         observer.onNext(this.get());
      } else if (this.isOnComplete()) {
         observer.onComplete();
      } else if (this.isOnError()) {
         observer.onError(this.getThrowable());
      } else if (this.isOnSubscribe()) {
         observer.onSubscribe(this.getSubscription());
      }

   }
}
