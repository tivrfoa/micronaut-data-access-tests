package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionState;
import java.util.List;

@Internal
public interface SynchronousTransactionState extends TransactionState {
   boolean isSynchronizationActive();

   void initSynchronization() throws IllegalStateException;

   void registerSynchronization(@NonNull TransactionSynchronization synchronization);

   @NonNull
   List<TransactionSynchronization> getSynchronizations() throws IllegalStateException;

   void clearSynchronization() throws IllegalStateException;

   void setTransactionName(@Nullable String name);

   @Nullable
   String getTransactionName();

   void setTransactionReadOnly(boolean readOnly);

   boolean isTransactionReadOnly();

   void setTransactionIsolationLevel(@Nullable TransactionDefinition.Isolation isolationLevel);

   @Nullable
   TransactionDefinition.Isolation getTransactionIsolationLevel();

   void setActualTransactionActive(boolean active);

   boolean isActualTransactionActive();

   void clear();
}
