package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionCallback;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.exceptions.TransactionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

@Internal
public abstract class AbstractSynchronousTransactionManager<T>
   extends AbstractSynchronousStateTransactionManager<T>
   implements SynchronousTransactionManager<T>,
   Serializable {
   @NotNull
   protected Object getTransactionStateKey() {
      return TransactionSynchronizationManager.DEFAULT_STATE_KEY;
   }

   @NonNull
   protected SynchronousTransactionState getState() {
      SynchronousTransactionState synchronousTransactionState = TransactionSynchronizationManager.getSynchronousTransactionState(this.getTransactionStateKey());
      if (synchronousTransactionState == null) {
         throw new IllegalStateException("Transaction state is not initialized!");
      } else {
         return synchronousTransactionState;
      }
   }

   @NonNull
   protected SynchronousTransactionState findOrCreateState() {
      return TransactionSynchronizationManager.getSynchronousTransactionStateOrCreate(this.getTransactionStateKey(), DefaultSynchronousTransactionState::new);
   }

   @Override
   protected void doDestroyState(SynchronousTransactionState state) {
      TransactionSynchronizationManager.unbindSynchronousTransactionState(this.getTransactionStateKey());
   }

   @Override
   public <R> R execute(@NonNull TransactionDefinition definition, @NonNull TransactionCallback<T, R> callback) {
      return this.execute(this.findOrCreateState(), definition, callback);
   }

   @Override
   public <R> R executeRead(@NonNull TransactionCallback<T, R> callback) {
      return this.executeRead(this.findOrCreateState(), callback);
   }

   @Override
   public <R> R executeWrite(@NonNull TransactionCallback<T, R> callback) {
      return this.executeWrite(this.findOrCreateState(), callback);
   }

   @NonNull
   @Override
   public final TransactionStatus<T> getTransaction(@Nullable TransactionDefinition definition) throws TransactionException {
      return this.getTransaction(this.findOrCreateState(), definition);
   }

   protected final DefaultTransactionStatus<T> prepareTransactionStatus(
      TransactionDefinition definition,
      @Nullable Object transaction,
      boolean newTransaction,
      boolean newSynchronization,
      boolean debug,
      @Nullable Object suspendedResources
   ) {
      return this.prepareTransactionStatus(this.getState(), definition, transaction, newTransaction, newSynchronization, debug, suspendedResources);
   }

   @Override
   protected DefaultTransactionStatus<T> newTransactionStatus(
      SynchronousTransactionState state,
      TransactionDefinition definition,
      Object transaction,
      boolean newTransaction,
      boolean newSynchronization,
      boolean debug,
      Object suspendedResources
   ) {
      return this.newTransactionStatus(definition, transaction, newTransaction, newSynchronization, debug, suspendedResources);
   }

   protected DefaultTransactionStatus<T> newTransactionStatus(
      TransactionDefinition definition,
      @Nullable Object transaction,
      boolean newTransaction,
      boolean newSynchronization,
      boolean debug,
      @Nullable Object suspendedResources
   ) {
      return super.newTransactionStatus(this.getState(), definition, transaction, newTransaction, newSynchronization, debug, suspendedResources);
   }

   @Override
   protected T getConnection(SynchronousTransactionState state, Object transaction) {
      return this.getConnection(transaction);
   }

   @Nullable
   protected abstract T getConnection(Object transaction);

   @Override
   protected void prepareSynchronization(SynchronousTransactionState state, DefaultTransactionStatus<T> status, TransactionDefinition definition) {
      this.prepareSynchronization(status, definition);
   }

   protected void prepareSynchronization(@NonNull DefaultTransactionStatus<T> status, @NonNull TransactionDefinition definition) {
      super.prepareSynchronization(this.getState(), status, definition);
   }

   @Nullable
   protected final AbstractSynchronousTransactionManager.SuspendedResourcesHolder suspend(@Nullable Object transaction) throws TransactionException {
      AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder holder = this.suspend(this.getState(), transaction);
      return holder == null
         ? null
         : new AbstractSynchronousTransactionManager.SuspendedResourcesHolder(
            holder.suspendedResources, holder.suspendedSynchronizations, holder.name, holder.readOnly, holder.isolationLevel, holder.wasActive
         );
   }

   protected final void resume(@Nullable Object transaction, @Nullable AbstractSynchronousTransactionManager.SuspendedResourcesHolder resourcesHolder) throws TransactionException {
      this.resume(
         this.getState(),
         transaction,
         resourcesHolder == null
            ? null
            : new AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder(
               resourcesHolder.suspendedResources,
               resourcesHolder.suspendedSynchronizations,
               resourcesHolder.name,
               resourcesHolder.readOnly,
               resourcesHolder.isolationLevel,
               resourcesHolder.wasActive
            )
      );
   }

   @Override
   public final void commit(TransactionStatus<T> status) throws TransactionException {
      this.commit(this.getState(), status);
   }

   @Override
   public final void rollback(TransactionStatus<T> status) throws TransactionException {
      this.rollback(this.getState(), status);
   }

   protected final void triggerBeforeCommit(DefaultTransactionStatus<T> status) {
      this.triggerBeforeCommit(this.getState(), status);
   }

   protected final void triggerBeforeCompletion(DefaultTransactionStatus<T> status) {
      this.triggerBeforeCompletion(this.getState(), status);
   }

   @Override
   protected Object doGetTransaction(@NonNull SynchronousTransactionState state) throws TransactionException {
      return this.doGetTransaction();
   }

   @Override
   protected boolean isExistingTransaction(@NonNull SynchronousTransactionState state, @NonNull Object transaction) throws TransactionException {
      return this.isExistingTransaction(transaction);
   }

   protected final void invokeAfterCompletion(List<TransactionSynchronization> synchronizations, TransactionSynchronization.Status completionStatus) {
      this.invokeAfterCompletion(this.getState(), synchronizations, completionStatus);
   }

   @NonNull
   protected abstract Object doGetTransaction() throws TransactionException;

   protected boolean isExistingTransaction(@NonNull Object transaction) throws TransactionException {
      return false;
   }

   @Override
   protected void registerAfterCompletionWithExistingTransaction(
      SynchronousTransactionState state, Object transaction, List<TransactionSynchronization> synchronizations
   ) throws TransactionException {
      this.registerAfterCompletionWithExistingTransaction(transaction, synchronizations);
   }

   protected void registerAfterCompletionWithExistingTransaction(Object transaction, List<TransactionSynchronization> synchronizations) throws TransactionException {
      super.registerAfterCompletionWithExistingTransaction(this.getState(), transaction, synchronizations);
   }

   @Override
   protected void doCleanupAfterCompletion(SynchronousTransactionState state, Object transaction) {
      this.doCleanupAfterCompletion(transaction);
   }

   protected void doCleanupAfterCompletion(Object transaction) {
   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      ois.defaultReadObject();
      this.logger = LoggerFactory.getLogger(this.getClass());
   }

   protected static final class SuspendedResourcesHolder {
      @Nullable
      private final Object suspendedResources;
      @Nullable
      private final List<TransactionSynchronization> suspendedSynchronizations;
      @Nullable
      private final String name;
      private final boolean readOnly;
      @Nullable
      private final TransactionDefinition.Isolation isolationLevel;
      private final boolean wasActive;

      private SuspendedResourcesHolder(
         @Nullable Object suspendedResources,
         List<TransactionSynchronization> suspendedSynchronizations,
         @Nullable String name,
         boolean readOnly,
         @Nullable TransactionDefinition.Isolation isolationLevel,
         boolean wasActive
      ) {
         this.suspendedResources = suspendedResources;
         this.suspendedSynchronizations = suspendedSynchronizations;
         this.name = name;
         this.readOnly = readOnly;
         this.isolationLevel = isolationLevel;
         this.wasActive = wasActive;
      }
   }
}
