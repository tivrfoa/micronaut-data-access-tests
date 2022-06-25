package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionManager;
import io.micronaut.transaction.TransactionState;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.exceptions.TransactionException;

@Internal
interface SynchronousTransactionStateManager<T, S extends TransactionState> extends TransactionManager, TransactionStateOperations<T, S> {
   @NonNull
   TransactionStatus<T> getTransaction(@NonNull S state, @Nullable TransactionDefinition definition) throws TransactionException;

   void commit(@NonNull S state, TransactionStatus<T> status) throws TransactionException;

   void rollback(@NonNull S state, TransactionStatus<T> status) throws TransactionException;
}
