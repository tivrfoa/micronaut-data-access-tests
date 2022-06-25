package io.micronaut.transaction.interceptor;

import io.micronaut.aop.kotlin.KotlinInterceptedMethod;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import jakarta.inject.Singleton;
import kotlin.coroutines.CoroutineContext;

@Internal
@Singleton
@Requires(
   classes = {CoroutineContext.class}
)
public final class CoroutineTxHelper {
   public TransactionSynchronizationManager.TransactionSynchronizationState setupTxState(KotlinInterceptedMethod kotlinInterceptedMethod) {
      CoroutineContext existingContext = kotlinInterceptedMethod.getCoroutineContext();
      TxSynchronousContext txSynchronousContext = (TxSynchronousContext)existingContext.get(TxSynchronousContext.Key);
      if (txSynchronousContext != null) {
         return txSynchronousContext.getState();
      } else {
         TransactionSynchronizationManager.TransactionSynchronizationState txState = TransactionSynchronizationManager.getOrCreateState();
         kotlinInterceptedMethod.updateCoroutineContext(existingContext.plus(new TxSynchronousContext(txState)));
         return txState;
      }
   }
}
