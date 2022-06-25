package reactor.core.publisher;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import org.reactivestreams.Subscription;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

interface SignalPeek<T> extends Scannable {
   @Nullable
   Consumer<? super Subscription> onSubscribeCall();

   @Nullable
   Consumer<? super T> onNextCall();

   @Nullable
   Consumer<? super Throwable> onErrorCall();

   @Nullable
   Runnable onCompleteCall();

   @Nullable
   Runnable onAfterTerminateCall();

   @Nullable
   LongConsumer onRequestCall();

   @Nullable
   Runnable onCancelCall();

   @Nullable
   default Consumer<? super T> onAfterNextCall() {
      return null;
   }

   @Nullable
   default Consumer<? super Context> onCurrentContextCall() {
      return null;
   }
}
