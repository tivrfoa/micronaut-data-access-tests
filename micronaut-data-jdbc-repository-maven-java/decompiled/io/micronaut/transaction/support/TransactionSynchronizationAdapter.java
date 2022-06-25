package io.micronaut.transaction.support;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;

public abstract class TransactionSynchronizationAdapter implements TransactionSynchronization, Ordered {
   @Override
   public int getOrder() {
      return Integer.MAX_VALUE;
   }

   @Override
   public void suspend() {
   }

   @Override
   public void resume() {
   }

   @Override
   public void flush() {
   }

   @Override
   public void beforeCommit(boolean readOnly) {
   }

   @Override
   public void beforeCompletion() {
   }

   @Override
   public void afterCommit() {
   }

   @Override
   public void afterCompletion(@NonNull TransactionSynchronization.Status status) {
   }
}
