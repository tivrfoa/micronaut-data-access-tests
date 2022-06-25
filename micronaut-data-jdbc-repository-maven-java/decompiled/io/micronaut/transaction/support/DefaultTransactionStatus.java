package io.micronaut.transaction.support;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.SavepointManager;
import io.micronaut.transaction.exceptions.NestedTransactionNotSupportedException;
import java.util.function.Supplier;

public class DefaultTransactionStatus<T> extends AbstractTransactionStatus<T> {
   @Nullable
   private final Object transaction;
   private final boolean newTransaction;
   private final boolean newSynchronization;
   private final boolean readOnly;
   private final boolean debug;
   @Nullable
   private final Object suspendedResources;
   private final Supplier<T> connectionSupplier;

   public DefaultTransactionStatus(
      @Nullable Object transaction,
      @NonNull Supplier<T> connectionSupplier,
      boolean newTransaction,
      boolean newSynchronization,
      boolean readOnly,
      boolean debug,
      @Nullable Object suspendedResources
   ) {
      this.transaction = transaction;
      this.connectionSupplier = connectionSupplier;
      this.newTransaction = newTransaction;
      this.newSynchronization = newSynchronization;
      this.readOnly = readOnly;
      this.debug = debug;
      this.suspendedResources = suspendedResources;
   }

   @NonNull
   @Override
   public T getConnection() {
      return (T)this.connectionSupplier.get();
   }

   @Override
   public Object getTransaction() {
      if (this.transaction == null) {
         throw new IllegalStateException("No transaction active");
      } else {
         return this.transaction;
      }
   }

   public boolean hasTransaction() {
      return this.transaction != null;
   }

   @Override
   public boolean isNewTransaction() {
      return this.hasTransaction() && this.newTransaction;
   }

   public boolean isNewSynchronization() {
      return this.newSynchronization;
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public boolean isDebug() {
      return this.debug;
   }

   @Nullable
   public Object getSuspendedResources() {
      return this.suspendedResources;
   }

   @Override
   public boolean isGlobalRollbackOnly() {
      return this.transaction instanceof SmartTransactionObject && ((SmartTransactionObject)this.transaction).isRollbackOnly();
   }

   @Override
   protected SavepointManager getSavepointManager() {
      Object transaction = this.transaction;
      if (!(transaction instanceof SavepointManager)) {
         throw new NestedTransactionNotSupportedException("Transaction object [" + this.transaction + "] does not support savepoints");
      } else {
         return (SavepointManager)transaction;
      }
   }

   public boolean isTransactionSavepointManager() {
      return this.transaction instanceof SavepointManager;
   }

   @Override
   public void flush() {
      if (this.transaction instanceof SmartTransactionObject) {
         ((SmartTransactionObject)this.transaction).flush();
      }

   }
}
