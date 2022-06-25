package io.micronaut.transaction.interceptor;

import io.micronaut.core.annotation.Internal;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import kotlin.Metadata;
import kotlin.coroutines.AbstractCoroutineContextElement;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.ThreadContextElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
@Metadata(
   mv = {1, 6, 0},
   k = 1,
   xi = 48,
   d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u0000 \u000e2\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00012\u00020\u0003:\u0001\u000eB\u000f\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0002¢\u0006\u0002\u0010\u0005J\u001a\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0002H\u0016J\u0012\u0010\r\u001a\u0004\u0018\u00010\u00022\u0006\u0010\n\u001a\u00020\u000bH\u0016R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0002¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u000f"},
   d2 = {"Lio/micronaut/transaction/interceptor/TxSynchronousContext;", "Lkotlinx/coroutines/ThreadContextElement;", "Lio/micronaut/transaction/support/TransactionSynchronizationManager$TransactionSynchronizationState;", "Lkotlin/coroutines/AbstractCoroutineContextElement;", "state", "(Lio/micronaut/transaction/support/TransactionSynchronizationManager$TransactionSynchronizationState;)V", "getState", "()Lio/micronaut/transaction/support/TransactionSynchronizationManager$TransactionSynchronizationState;", "restoreThreadContext", "", "context", "Lkotlin/coroutines/CoroutineContext;", "oldState", "updateThreadContext", "Key", "data-tx"}
)
public final class TxSynchronousContext
   extends AbstractCoroutineContextElement
   implements ThreadContextElement<TransactionSynchronizationManager.TransactionSynchronizationState> {
   @NotNull
   public static final TxSynchronousContext.Key Key = new TxSynchronousContext.Key(null);
   @Nullable
   private final TransactionSynchronizationManager.TransactionSynchronizationState state;

   public TxSynchronousContext(@Nullable TransactionSynchronizationManager.TransactionSynchronizationState state) {
      super(Key);
      this.state = state;
   }

   @Nullable
   public final TransactionSynchronizationManager.TransactionSynchronizationState getState() {
      return this.state;
   }

   public void restoreThreadContext(@NotNull CoroutineContext context, @Nullable TransactionSynchronizationManager.TransactionSynchronizationState oldState) {
      Intrinsics.checkNotNullParameter(context, "context");
      TransactionSynchronizationManager.setState(oldState);
   }

   @Nullable
   public TransactionSynchronizationManager.TransactionSynchronizationState updateThreadContext(@NotNull CoroutineContext context) {
      Intrinsics.checkNotNullParameter(context, "context");
      TransactionSynchronizationManager.TransactionSynchronizationState copyState = TransactionSynchronizationManager.getState();
      TransactionSynchronizationManager.setState(this.state);
      return copyState;
   }

   @Metadata(
      mv = {1, 6, 0},
      k = 1,
      xi = 48,
      d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0003¨\u0006\u0004"},
      d2 = {"Lio/micronaut/transaction/interceptor/TxSynchronousContext$Key;", "Lkotlin/coroutines/CoroutineContext$Key;", "Lio/micronaut/transaction/interceptor/TxSynchronousContext;", "()V", "data-tx"}
   )
   public static final class Key implements kotlin.coroutines.CoroutineContext.Key<TxSynchronousContext> {
      private Key() {
      }
   }
}
