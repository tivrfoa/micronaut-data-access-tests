package io.micronaut.transaction;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.exceptions.TransactionException;

public interface SynchronousTransactionManager<T> extends TransactionManager, TransactionOperations<T> {
   @NonNull
   TransactionStatus<T> getTransaction(@Nullable TransactionDefinition definition) throws TransactionException;

   void commit(TransactionStatus<T> status) throws TransactionException;

   void rollback(TransactionStatus<T> status) throws TransactionException;
}
