package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionCallback;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.exceptions.IllegalTransactionStateException;
import io.micronaut.transaction.exceptions.InvalidTimeoutException;
import io.micronaut.transaction.exceptions.NestedTransactionNotSupportedException;
import io.micronaut.transaction.exceptions.TransactionException;
import io.micronaut.transaction.exceptions.TransactionSuspensionNotSupportedException;
import io.micronaut.transaction.exceptions.TransactionSystemException;
import io.micronaut.transaction.exceptions.UnexpectedRollbackException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public abstract class AbstractSynchronousStateTransactionManager<T> implements SynchronousTransactionStateManager<T, SynchronousTransactionState>, Serializable {
   protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
   private AbstractSynchronousStateTransactionManager.Synchronization transactionSynchronization = AbstractSynchronousStateTransactionManager.Synchronization.ALWAYS;
   private Duration defaultTimeout = TransactionDefinition.TIMEOUT_DEFAULT;
   private boolean nestedTransactionAllowed = false;
   private boolean validateExistingTransaction = false;
   private boolean globalRollbackOnParticipationFailure = true;
   private boolean failEarlyOnGlobalRollbackOnly = false;
   private boolean rollbackOnCommitFailure = false;

   public <R> R execute(@NonNull SynchronousTransactionState state, @NonNull TransactionDefinition definition, @NonNull TransactionCallback<T, R> callback) {
      Objects.requireNonNull(definition, "Definition should not be null");
      Objects.requireNonNull(callback, "Callback should not be null");
      TransactionStatus<T> status = this.getTransaction(state, definition);

      R result;
      try {
         result = callback.call(status);
      } catch (Error | RuntimeException var7) {
         this.rollbackOnException(state, status, var7);
         throw var7;
      } catch (Throwable var8) {
         this.rollbackOnException(state, status, var8);
         throw new UndeclaredThrowableException(var8, "TransactionCallback threw undeclared checked exception");
      }

      this.commit(state, status);
      return result;
   }

   public <R> R executeRead(@NonNull SynchronousTransactionState state, @NonNull TransactionCallback<T, R> callback) {
      return this.execute(state, TransactionDefinition.READ_ONLY, callback);
   }

   public <R> R executeWrite(@NonNull SynchronousTransactionState state, @NonNull TransactionCallback<T, R> callback) {
      return this.execute(state, TransactionDefinition.DEFAULT, callback);
   }

   public final void setTransactionSynchronization(@NonNull AbstractSynchronousStateTransactionManager.Synchronization transactionSynchronization) {
      if (transactionSynchronization != null) {
         this.transactionSynchronization = transactionSynchronization;
      }

   }

   @NonNull
   public final AbstractSynchronousStateTransactionManager.Synchronization getTransactionSynchronization() {
      return this.transactionSynchronization;
   }

   public final void setDefaultTimeout(@NonNull Duration defaultTimeout) {
      if (defaultTimeout != null && !defaultTimeout.isNegative()) {
         this.defaultTimeout = defaultTimeout;
      } else {
         throw new InvalidTimeoutException("Invalid default timeout", defaultTimeout);
      }
   }

   @NonNull
   public final Duration getDefaultTimeout() {
      return this.defaultTimeout;
   }

   public final void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
      this.nestedTransactionAllowed = nestedTransactionAllowed;
   }

   public final boolean isNestedTransactionAllowed() {
      return this.nestedTransactionAllowed;
   }

   public final void setValidateExistingTransaction(boolean validateExistingTransaction) {
      this.validateExistingTransaction = validateExistingTransaction;
   }

   public final boolean isValidateExistingTransaction() {
      return this.validateExistingTransaction;
   }

   public final void setGlobalRollbackOnParticipationFailure(boolean globalRollbackOnParticipationFailure) {
      this.globalRollbackOnParticipationFailure = globalRollbackOnParticipationFailure;
   }

   public final boolean isGlobalRollbackOnParticipationFailure() {
      return this.globalRollbackOnParticipationFailure;
   }

   public final void setFailEarlyOnGlobalRollbackOnly(boolean failEarlyOnGlobalRollbackOnly) {
      this.failEarlyOnGlobalRollbackOnly = failEarlyOnGlobalRollbackOnly;
   }

   public final boolean isFailEarlyOnGlobalRollbackOnly() {
      return this.failEarlyOnGlobalRollbackOnly;
   }

   public final void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
      this.rollbackOnCommitFailure = rollbackOnCommitFailure;
   }

   public final boolean isRollbackOnCommitFailure() {
      return this.rollbackOnCommitFailure;
   }

   @NonNull
   public final TransactionStatus<T> getTransaction(@NonNull SynchronousTransactionState state, @Nullable TransactionDefinition definition) throws TransactionException {
      definition = definition != null ? definition : TransactionDefinition.DEFAULT;
      boolean debugEnabled = this.logger.isDebugEnabled();
      Object transaction = this.doGetTransaction(state);
      if (this.isExistingTransaction(state, transaction)) {
         return this.handleExistingTransaction(state, definition, transaction, debugEnabled);
      } else if (definition.getTimeout().compareTo(TransactionDefinition.TIMEOUT_DEFAULT) < 0) {
         throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
      } else {
         TransactionDefinition.Propagation propagationBehavior = definition.getPropagationBehavior();
         switch(propagationBehavior) {
            case MANDATORY:
               throw new IllegalTransactionStateException("No existing transaction found for transaction marked with propagation 'mandatory'");
            case REQUIRED:
            case REQUIRES_NEW:
            case NESTED:
               AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder suspendedResources = this.suspend(state, null);
               if (debugEnabled) {
                  this.logger.debug("Creating new transaction with name [{}]: {}", definition.getName(), definition);
               }

               try {
                  boolean newSynchronization = this.getTransactionSynchronization() != AbstractSynchronousStateTransactionManager.Synchronization.NEVER;
                  DefaultTransactionStatus<T> status = this.newTransactionStatus(
                     state, definition, transaction, true, newSynchronization, debugEnabled, suspendedResources
                  );
                  this.doBegin(transaction, definition);
                  this.prepareSynchronization(state, status, definition);
                  return status;
               } catch (Error | RuntimeException var9) {
                  this.resume(state, null, suspendedResources);
                  throw var9;
               }
            default:
               if (definition.getIsolationLevel() != TransactionDefinition.Isolation.DEFAULT && this.logger.isWarnEnabled()) {
                  this.logger
                     .warn("Custom isolation level specified but no actual transaction initiated; isolation level will effectively be ignored: {}", definition);
               }

               boolean newSynchronization = this.getTransactionSynchronization() == AbstractSynchronousStateTransactionManager.Synchronization.ALWAYS;
               return this.prepareTransactionStatus(state, definition, null, true, newSynchronization, debugEnabled, null);
         }
      }
   }

   private TransactionStatus<T> handleExistingTransaction(
      SynchronousTransactionState state, TransactionDefinition definition, Object transaction, boolean debugEnabled
   ) throws TransactionException {
      TransactionDefinition.Propagation propagationBehavior = definition.getPropagationBehavior();
      switch(propagationBehavior) {
         case REQUIRES_NEW:
            return this.handleRequiresNewTransactionPropagation(state, definition, transaction, debugEnabled);
         case NESTED:
            return this.handleNestedTransactionPropagation(state, definition, transaction, debugEnabled);
         case NEVER:
            throw new IllegalTransactionStateException("Existing transaction found for transaction marked with propagation 'never'");
         case NOT_SUPPORTED:
            return this.handleNotSupportedTransactionPropagation(state, definition, transaction, debugEnabled);
         default:
            return this.handleDefaultTransactionPropagation(state, definition, transaction, debugEnabled);
      }
   }

   @NonNull
   private DefaultTransactionStatus<T> handleNotSupportedTransactionPropagation(
      SynchronousTransactionState state, TransactionDefinition definition, Object transaction, boolean debugEnabled
   ) {
      if (debugEnabled) {
         this.logger.debug("Suspending current transaction");
      }

      Object suspendedResources = this.suspend(state, transaction);
      boolean newSynchronization = this.getTransactionSynchronization() == AbstractSynchronousStateTransactionManager.Synchronization.ALWAYS;
      return this.prepareTransactionStatus(state, definition, null, false, newSynchronization, debugEnabled, suspendedResources);
   }

   @NonNull
   private DefaultTransactionStatus<T> handleDefaultTransactionPropagation(
      SynchronousTransactionState state, TransactionDefinition definition, Object transaction, boolean debugEnabled
   ) {
      if (debugEnabled) {
         this.logger.debug("Participating in existing transaction");
      }

      if (this.isValidateExistingTransaction()) {
         if (definition.getIsolationLevel() != TransactionDefinition.Isolation.DEFAULT) {
            TransactionDefinition.Isolation currentIsolationLevel = state.getTransactionIsolationLevel();
            if (currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel()) {
               throw new IllegalTransactionStateException(
                  "Participating transaction with definition ["
                     + definition
                     + "] specifies isolation level which is incompatible with existing transaction: "
                     + (currentIsolationLevel != null ? currentIsolationLevel.getCode() : "(unknown)")
               );
            }
         }

         if (!definition.isReadOnly() && state.isTransactionReadOnly()) {
            throw new IllegalTransactionStateException(
               "Participating transaction with definition [" + definition + "] is not marked as read-only but existing transaction is"
            );
         }
      }

      boolean defaultNewSynchronization = this.getTransactionSynchronization() != AbstractSynchronousStateTransactionManager.Synchronization.NEVER;
      return this.prepareTransactionStatus(state, definition, transaction, false, defaultNewSynchronization, debugEnabled, null);
   }

   @NonNull
   private DefaultTransactionStatus<T> handleNestedTransactionPropagation(
      SynchronousTransactionState state, TransactionDefinition definition, Object transaction, boolean debugEnabled
   ) {
      if (!this.isNestedTransactionAllowed()) {
         throw new NestedTransactionNotSupportedException(
            "Transaction manager does not allow nested transactions by default - specify 'nestedTransactionAllowed' property with value 'true'"
         );
      } else {
         if (debugEnabled) {
            this.logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
         }

         if (this.useSavepointForNestedTransaction()) {
            DefaultTransactionStatus<T> status = this.prepareTransactionStatus(state, definition, transaction, false, false, debugEnabled, null);
            status.createAndHoldSavepoint();
            return status;
         } else {
            boolean nestedNewSynchronization = this.getTransactionSynchronization() != AbstractSynchronousStateTransactionManager.Synchronization.NEVER;
            DefaultTransactionStatus<T> status = this.newTransactionStatus(state, definition, transaction, true, nestedNewSynchronization, debugEnabled, null);
            this.doBegin(transaction, definition);
            this.prepareSynchronization(state, status, definition);
            return status;
         }
      }
   }

   @NotNull
   private DefaultTransactionStatus<T> handleRequiresNewTransactionPropagation(
      SynchronousTransactionState state, TransactionDefinition definition, Object transaction, boolean debugEnabled
   ) {
      if (debugEnabled) {
         this.logger.debug("Suspending current transaction, creating new transaction with name [" + definition.getName() + "]");
      }

      AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder requiresNewSuspendedResources = this.suspend(state, transaction);

      try {
         boolean requiresNewIsNewSynchronization = this.getTransactionSynchronization() != AbstractSynchronousStateTransactionManager.Synchronization.NEVER;
         DefaultTransactionStatus<T> status = this.newTransactionStatus(
            state, definition, transaction, true, requiresNewIsNewSynchronization, debugEnabled, requiresNewSuspendedResources
         );
         this.doBegin(transaction, definition);
         this.prepareSynchronization(state, status, definition);
         return status;
      } catch (Error | RuntimeException var8) {
         this.resumeAfterBeginException(state, transaction, requiresNewSuspendedResources, var8);
         throw var8;
      }
   }

   protected final DefaultTransactionStatus<T> prepareTransactionStatus(
      @NonNull SynchronousTransactionState state,
      TransactionDefinition definition,
      @Nullable Object transaction,
      boolean newTransaction,
      boolean newSynchronization,
      boolean debug,
      @Nullable Object suspendedResources
   ) {
      DefaultTransactionStatus<T> status = this.newTransactionStatus(
         state, definition, transaction, newTransaction, newSynchronization, debug, suspendedResources
      );
      this.prepareSynchronization(state, status, definition);
      return status;
   }

   protected DefaultTransactionStatus<T> newTransactionStatus(
      @NonNull SynchronousTransactionState state,
      TransactionDefinition definition,
      @Nullable Object transaction,
      boolean newTransaction,
      boolean newSynchronization,
      boolean debug,
      @Nullable Object suspendedResources
   ) {
      boolean actualNewSynchronization = newSynchronization && !state.isSynchronizationActive();
      return new DefaultTransactionStatus<>(
         transaction,
         () -> this.getConnection(state, transaction),
         newTransaction,
         actualNewSynchronization,
         definition.isReadOnly(),
         debug,
         suspendedResources
      );
   }

   @Nullable
   protected abstract T getConnection(@NonNull SynchronousTransactionState state, Object transaction);

   protected void prepareSynchronization(
      @NonNull SynchronousTransactionState state, @NonNull DefaultTransactionStatus<T> status, @NonNull TransactionDefinition definition
   ) {
      if (status.isNewSynchronization()) {
         state.setActualTransactionActive(status.hasTransaction());
         state.setTransactionIsolationLevel(definition.getIsolationLevel() != TransactionDefinition.Isolation.DEFAULT ? definition.getIsolationLevel() : null);
         state.setTransactionReadOnly(definition.isReadOnly());
         state.setTransactionName(definition.getName());
         state.initSynchronization();
      }

   }

   protected Duration determineTimeout(TransactionDefinition definition) {
      return definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT ? definition.getTimeout() : this.getDefaultTimeout();
   }

   @Nullable
   protected final AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder suspend(
      @NonNull SynchronousTransactionState state, @Nullable Object transaction
   ) throws TransactionException {
      if (state.isSynchronizationActive()) {
         List<TransactionSynchronization> suspendedSynchronizations = this.doSuspendSynchronization(state);

         try {
            Object suspendedResources = null;
            if (transaction != null) {
               suspendedResources = this.doSuspend(transaction);
            }

            String name = state.getTransactionName();
            state.setTransactionName(null);
            boolean readOnly = state.isTransactionReadOnly();
            state.setTransactionReadOnly(false);
            TransactionDefinition.Isolation isolationLevel = state.getTransactionIsolationLevel();
            state.setTransactionIsolationLevel(null);
            boolean wasActive = state.isActualTransactionActive();
            state.setActualTransactionActive(false);
            return new AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder(
               suspendedResources, suspendedSynchronizations, name, readOnly, isolationLevel, wasActive
            );
         } catch (Error | RuntimeException var9) {
            this.doResumeSynchronization(state, suspendedSynchronizations);
            throw var9;
         }
      } else if (transaction != null) {
         Object suspendedResources = this.doSuspend(transaction);
         return new AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder(suspendedResources);
      } else {
         return null;
      }
   }

   protected final void resume(
      @NonNull SynchronousTransactionState state,
      @Nullable Object transaction,
      @Nullable AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder resourcesHolder
   ) throws TransactionException {
      if (resourcesHolder != null) {
         Object suspendedResources = resourcesHolder.suspendedResources;
         if (suspendedResources != null) {
            this.doResume(transaction, suspendedResources);
         }

         List<TransactionSynchronization> suspendedSynchronizations = resourcesHolder.suspendedSynchronizations;
         if (suspendedSynchronizations != null) {
            state.setActualTransactionActive(resourcesHolder.wasActive);
            state.setTransactionIsolationLevel(resourcesHolder.isolationLevel);
            state.setTransactionReadOnly(resourcesHolder.readOnly);
            state.setTransactionName(resourcesHolder.name);
            this.doResumeSynchronization(state, suspendedSynchronizations);
         }
      }

   }

   private void resumeAfterBeginException(
      @NonNull SynchronousTransactionState state,
      Object transaction,
      @Nullable AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder suspendedResources,
      Throwable beginEx
   ) {
      try {
         this.resume(state, transaction, suspendedResources);
      } catch (Error | RuntimeException var7) {
         String exMessage = "Inner transaction begin exception overridden by outer transaction resume exception";
         this.logger.error(exMessage, beginEx);
         throw var7;
      }
   }

   private List<TransactionSynchronization> doSuspendSynchronization(@NonNull SynchronousTransactionState state) {
      List<TransactionSynchronization> suspendedSynchronizations = state.getSynchronizations();

      for(TransactionSynchronization synchronization : suspendedSynchronizations) {
         synchronization.suspend();
      }

      state.clearSynchronization();
      return suspendedSynchronizations;
   }

   private void doResumeSynchronization(@NonNull SynchronousTransactionState state, List<TransactionSynchronization> suspendedSynchronizations) {
      state.initSynchronization();

      for(TransactionSynchronization synchronization : suspendedSynchronizations) {
         synchronization.resume();
         state.registerSynchronization(synchronization);
      }

   }

   public final void commit(@NonNull SynchronousTransactionState state, TransactionStatus<T> status) throws TransactionException {
      if (status.isCompleted()) {
         throw new IllegalTransactionStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
      } else {
         DefaultTransactionStatus<T> defStatus = (DefaultTransactionStatus)status;
         if (defStatus.isLocalRollbackOnly()) {
            if (defStatus.isDebug()) {
               this.logger.debug("Transactional code has requested rollback");
            }

            this.processRollback(state, defStatus, false);
         } else if (!this.shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly()) {
            if (defStatus.isDebug()) {
               this.logger.debug("Global transaction is marked as rollback-only but transactional code requested commit");
            }

            this.processRollback(state, defStatus, true);
         } else {
            this.processCommit(state, defStatus);
         }
      }
   }

   private void processCommit(@NonNull SynchronousTransactionState state, DefaultTransactionStatus<T> status) throws TransactionException {
      try {
         boolean beforeCompletionInvoked = false;

         try {
            boolean unexpectedRollback = false;
            this.prepareForCommit(status);
            this.triggerBeforeCommit(state, status);
            this.triggerBeforeCompletion(state, status);
            beforeCompletionInvoked = true;
            if (status.hasSavepoint()) {
               if (status.isDebug()) {
                  this.logger.debug("Releasing transaction savepoint");
               }

               unexpectedRollback = status.isGlobalRollbackOnly();
               status.releaseHeldSavepoint();
            } else if (status.isNewTransaction()) {
               if (status.isDebug()) {
                  this.logger.debug("Initiating transaction commit");
               }

               unexpectedRollback = status.isGlobalRollbackOnly();
               this.doCommit(status);
            } else if (this.isFailEarlyOnGlobalRollbackOnly()) {
               unexpectedRollback = status.isGlobalRollbackOnly();
            }

            if (unexpectedRollback) {
               throw new UnexpectedRollbackException("Transaction silently rolled back because it has been marked as rollback-only");
            }
         } catch (UnexpectedRollbackException var18) {
            this.triggerAfterCompletion(state, status, TransactionSynchronization.Status.ROLLED_BACK);
            throw var18;
         } catch (TransactionException var19) {
            if (this.isRollbackOnCommitFailure()) {
               this.doRollbackOnCommitException(state, status, var19);
            } else {
               this.triggerAfterCompletion(state, status, TransactionSynchronization.Status.UNKNOWN);
            }

            throw var19;
         } catch (Error | RuntimeException var20) {
            if (!beforeCompletionInvoked) {
               this.triggerBeforeCompletion(state, status);
            }

            this.doRollbackOnCommitException(state, status, var20);
            throw var20;
         }

         try {
            this.triggerAfterCommit(state, status);
         } finally {
            this.triggerAfterCompletion(state, status, TransactionSynchronization.Status.COMMITTED);
         }
      } finally {
         this.cleanupAfterCompletion(state, status);
      }

   }

   public final void rollback(@NonNull SynchronousTransactionState state, TransactionStatus<T> status) throws TransactionException {
      if (status.isCompleted()) {
         throw new IllegalTransactionStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");
      } else {
         DefaultTransactionStatus<T> defStatus = (DefaultTransactionStatus)status;
         this.processRollback(state, defStatus, false);
      }
   }

   private void processRollback(@NonNull SynchronousTransactionState state, DefaultTransactionStatus<T> status, boolean unexpected) {
      try {
         boolean unexpectedRollback = unexpected;

         try {
            this.triggerBeforeCompletion(state, status);
            if (status.hasSavepoint()) {
               if (status.isDebug()) {
                  this.logger.debug("Rolling back transaction to savepoint");
               }

               status.rollbackToHeldSavepoint();
            } else if (status.isNewTransaction()) {
               if (status.isDebug()) {
                  this.logger.debug("Initiating transaction rollback");
               }

               this.doRollback(status);
            } else {
               if (status.hasTransaction()) {
                  if (!status.isLocalRollbackOnly() && !this.isGlobalRollbackOnParticipationFailure()) {
                     if (status.isDebug()) {
                        this.logger.debug("Participating transaction failed - letting transaction originator decide on rollback");
                     }
                  } else {
                     if (status.isDebug()) {
                        this.logger.debug("Participating transaction failed - marking existing transaction as rollback-only");
                     }

                     this.doSetRollbackOnly(status);
                  }
               } else {
                  this.logger.debug("Should roll back transaction but cannot - no transaction available");
               }

               if (!this.isFailEarlyOnGlobalRollbackOnly()) {
                  unexpectedRollback = false;
               }
            }
         } catch (Error | RuntimeException var9) {
            this.triggerAfterCompletion(state, status, TransactionSynchronization.Status.UNKNOWN);
            throw var9;
         }

         this.triggerAfterCompletion(state, status, TransactionSynchronization.Status.ROLLED_BACK);
         if (unexpectedRollback) {
            throw new UnexpectedRollbackException("Transaction rolled back because it has been marked as rollback-only");
         }
      } finally {
         this.cleanupAfterCompletion(state, status);
      }

   }

   private void doRollbackOnCommitException(@NonNull SynchronousTransactionState state, @NonNull DefaultTransactionStatus<T> status, Throwable ex) throws TransactionException {
      try {
         if (status.isNewTransaction()) {
            if (status.isDebug()) {
               this.logger.debug("Initiating transaction rollback after commit exception", ex);
            }

            this.doRollback(status);
         } else if (status.hasTransaction() && this.isGlobalRollbackOnParticipationFailure()) {
            if (status.isDebug()) {
               this.logger.debug("Marking existing transaction as rollback-only after commit exception", ex);
            }

            this.doSetRollbackOnly(status);
         }
      } catch (Error | RuntimeException var5) {
         this.logger.error("Commit exception overridden by rollback exception", ex);
         this.triggerAfterCompletion(state, status, TransactionSynchronization.Status.UNKNOWN);
         throw var5;
      }

      this.triggerAfterCompletion(state, status, TransactionSynchronization.Status.ROLLED_BACK);
   }

   protected final void triggerBeforeCommit(@NonNull SynchronousTransactionState state, @NonNull DefaultTransactionStatus<T> status) {
      if (status.isNewSynchronization()) {
         if (status.isDebug()) {
            this.logger.trace("Triggering beforeCommit synchronization");
         }

         TransactionSynchronizationUtils.triggerBeforeCommit(state, status.isReadOnly());
      }

   }

   protected final void triggerBeforeCompletion(@NonNull SynchronousTransactionState state, @NonNull DefaultTransactionStatus<T> status) {
      if (status.isNewSynchronization()) {
         if (status.isDebug()) {
            this.logger.trace("Triggering beforeCompletion synchronization");
         }

         TransactionSynchronizationUtils.triggerBeforeCompletion(state);
      }

   }

   private void triggerAfterCommit(@NonNull SynchronousTransactionState state, @NonNull DefaultTransactionStatus<T> status) {
      if (status.isNewSynchronization()) {
         if (status.isDebug()) {
            this.logger.trace("Triggering afterCommit synchronization");
         }

         TransactionSynchronizationUtils.triggerAfterCommit(state);
      }

   }

   private void triggerAfterCompletion(
      @NonNull SynchronousTransactionState state, @NonNull DefaultTransactionStatus<T> status, TransactionSynchronization.Status completionStatus
   ) {
      if (status.isNewSynchronization()) {
         List<TransactionSynchronization> synchronizations = state.getSynchronizations();
         state.clearSynchronization();
         if (status.hasTransaction() && !status.isNewTransaction()) {
            if (!synchronizations.isEmpty()) {
               this.registerAfterCompletionWithExistingTransaction(state, status.getTransaction(), synchronizations);
            }
         } else {
            if (status.isDebug()) {
               this.logger.trace("Triggering afterCompletion synchronization");
            }

            this.invokeAfterCompletion(state, synchronizations, completionStatus);
         }
      }

   }

   protected final void invokeAfterCompletion(
      @NonNull SynchronousTransactionState state, List<TransactionSynchronization> synchronizations, TransactionSynchronization.Status completionStatus
   ) {
      TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, completionStatus);
   }

   private void cleanupAfterCompletion(@NonNull SynchronousTransactionState state, DefaultTransactionStatus<T> status) {
      status.setCompleted();
      if (status.isNewSynchronization()) {
         state.clear();
      }

      if (status.isNewTransaction()) {
         this.doCleanupAfterCompletion(state, status.getTransaction());
         this.doDestroyState(state);
      } else if (status.getSuspendedResources() != null) {
         if (status.isDebug()) {
            this.logger.debug("Resuming suspended transaction after completion of inner transaction");
         }

         Object transaction = status.hasTransaction() ? status.getTransaction() : null;
         this.resume(state, transaction, (AbstractSynchronousStateTransactionManager.SuspendedResourcesHolder)status.getSuspendedResources());
      }

   }

   @NonNull
   protected abstract Object doGetTransaction(@NonNull SynchronousTransactionState state) throws TransactionException;

   protected boolean isExistingTransaction(@NonNull SynchronousTransactionState state, @NonNull Object transaction) throws TransactionException {
      return false;
   }

   protected boolean useSavepointForNestedTransaction() {
      return true;
   }

   protected abstract void doBegin(@NonNull Object transaction, TransactionDefinition definition) throws TransactionException;

   @Nullable
   protected Object doSuspend(@NonNull Object transaction) throws TransactionException {
      throw new TransactionSuspensionNotSupportedException("Transaction manager [" + this.getClass().getName() + "] does not support transaction suspension");
   }

   protected void doResume(@Nullable Object transaction, @NonNull Object suspendedResources) throws TransactionException {
      throw new TransactionSuspensionNotSupportedException("Transaction manager [" + this.getClass().getName() + "] does not support transaction suspension");
   }

   protected boolean shouldCommitOnGlobalRollbackOnly() {
      return false;
   }

   protected void prepareForCommit(DefaultTransactionStatus<T> status) {
   }

   protected abstract void doCommit(DefaultTransactionStatus<T> status) throws TransactionException;

   protected abstract void doRollback(DefaultTransactionStatus<T> status) throws TransactionException;

   protected void doSetRollbackOnly(DefaultTransactionStatus<T> status) throws TransactionException {
      throw new IllegalTransactionStateException(
         "Participating in existing transactions is not supported - when 'isExistingTransaction' returns true, appropriate 'doSetRollbackOnly' behavior must be provided"
      );
   }

   protected void registerAfterCompletionWithExistingTransaction(
      @NonNull SynchronousTransactionState state, Object transaction, List<TransactionSynchronization> synchronizations
   ) throws TransactionException {
      this.logger
         .debug(
            "Cannot register Spring after-completion synchronization with existing transaction - processing Spring after-completion callbacks immediately, with outcome status 'unknown'"
         );
      this.invokeAfterCompletion(state, synchronizations, TransactionSynchronization.Status.UNKNOWN);
   }

   protected void doCleanupAfterCompletion(@NonNull SynchronousTransactionState state, Object transaction) {
   }

   protected void doDestroyState(@NonNull SynchronousTransactionState state) {
   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      ois.defaultReadObject();
      this.logger = LoggerFactory.getLogger(this.getClass());
   }

   private void rollbackOnException(@NonNull SynchronousTransactionState state, TransactionStatus<T> status, Throwable ex) throws TransactionException {
      this.logger.debug("Initiating transaction rollback on application exception", ex);

      try {
         this.rollback(state, status);
      } catch (TransactionSystemException var5) {
         this.logger.error("Application exception overridden by rollback exception", ex);
         var5.initApplicationException(ex);
         throw var5;
      } catch (Error | RuntimeException var6) {
         this.logger.error("Application exception overridden by rollback exception", ex);
         throw var6;
      }
   }

   protected static final class SuspendedResourcesHolder {
      @Nullable
      final Object suspendedResources;
      @Nullable
      List<TransactionSynchronization> suspendedSynchronizations;
      @Nullable
      String name;
      boolean readOnly;
      @Nullable
      TransactionDefinition.Isolation isolationLevel;
      boolean wasActive;

      private SuspendedResourcesHolder(Object suspendedResources) {
         this.suspendedResources = suspendedResources;
      }

      SuspendedResourcesHolder(
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

   static enum Synchronization {
      ALWAYS,
      ON_ACTUAL_TRANSACTION,
      NEVER;
   }
}
