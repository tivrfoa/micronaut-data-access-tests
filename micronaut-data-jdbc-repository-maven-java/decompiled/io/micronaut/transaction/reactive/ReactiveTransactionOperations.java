package io.micronaut.transaction.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.transaction.TransactionDefinition;
import org.reactivestreams.Publisher;

public interface ReactiveTransactionOperations<C> {
   @NonNull
   <T> Publisher<T> withTransaction(@NonNull TransactionDefinition definition, @NonNull ReactiveTransactionOperations.TransactionalCallback<C, T> handler);

   @NonNull
   default <T> Publisher<T> withTransaction(@NonNull ReactiveTransactionOperations.TransactionalCallback<C, T> handler) {
      return this.withTransaction(TransactionDefinition.DEFAULT, handler);
   }

   @FunctionalInterface
   public interface TransactionalCallback<C, T> {
      Publisher<T> doInTransaction(ReactiveTransactionStatus<C> status) throws Exception;
   }
}
