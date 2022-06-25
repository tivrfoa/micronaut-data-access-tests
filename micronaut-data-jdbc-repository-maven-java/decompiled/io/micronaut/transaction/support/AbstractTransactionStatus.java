package io.micronaut.transaction.support;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.SavepointManager;
import io.micronaut.transaction.TransactionStatus;
import io.micronaut.transaction.exceptions.NestedTransactionNotSupportedException;
import io.micronaut.transaction.exceptions.TransactionException;
import io.micronaut.transaction.exceptions.TransactionUsageException;

public abstract class AbstractTransactionStatus<T> implements TransactionStatus<T> {
   private boolean rollbackOnly = false;
   private boolean completed = false;
   @Nullable
   private Object savepoint;

   @Override
   public void setRollbackOnly() {
      this.rollbackOnly = true;
   }

   @Override
   public boolean isRollbackOnly() {
      return this.isLocalRollbackOnly() || this.isGlobalRollbackOnly();
   }

   public boolean isLocalRollbackOnly() {
      return this.rollbackOnly;
   }

   public boolean isGlobalRollbackOnly() {
      return false;
   }

   public void setCompleted() {
      this.completed = true;
   }

   @Override
   public boolean isCompleted() {
      return this.completed;
   }

   @Override
   public boolean hasSavepoint() {
      return this.savepoint != null;
   }

   protected void setSavepoint(@Nullable Object savepoint) {
      this.savepoint = savepoint;
   }

   @Nullable
   protected Object getSavepoint() {
      return this.savepoint;
   }

   public void createAndHoldSavepoint() throws TransactionException {
      this.setSavepoint(this.getSavepointManager().createSavepoint());
   }

   public void rollbackToHeldSavepoint() throws TransactionException {
      Object savepoint = this.getSavepoint();
      if (savepoint == null) {
         throw new TransactionUsageException("Cannot roll back to savepoint - no savepoint associated with current transaction");
      } else {
         this.getSavepointManager().rollbackToSavepoint(savepoint);
         this.getSavepointManager().releaseSavepoint(savepoint);
         this.setSavepoint(null);
      }
   }

   public void releaseHeldSavepoint() throws TransactionException {
      Object savepoint = this.getSavepoint();
      if (savepoint == null) {
         throw new TransactionUsageException("Cannot release savepoint - no savepoint associated with current transaction");
      } else {
         this.getSavepointManager().releaseSavepoint(savepoint);
         this.setSavepoint(null);
      }
   }

   @Override
   public Object createSavepoint() throws TransactionException {
      return this.getSavepointManager().createSavepoint();
   }

   @Override
   public void rollbackToSavepoint(Object savepoint) throws TransactionException {
      this.getSavepointManager().rollbackToSavepoint(savepoint);
   }

   @Override
   public void releaseSavepoint(Object savepoint) throws TransactionException {
      this.getSavepointManager().releaseSavepoint(savepoint);
   }

   @NonNull
   protected SavepointManager getSavepointManager() {
      throw new NestedTransactionNotSupportedException("This transaction does not support savepoints");
   }

   @Override
   public void flush() {
   }
}
