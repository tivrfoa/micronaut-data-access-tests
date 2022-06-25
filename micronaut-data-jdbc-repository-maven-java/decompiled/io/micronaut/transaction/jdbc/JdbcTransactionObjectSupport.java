package io.micronaut.transaction.jdbc;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.SavepointManager;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.exceptions.CannotCreateTransactionException;
import io.micronaut.transaction.exceptions.NestedTransactionNotSupportedException;
import io.micronaut.transaction.exceptions.TransactionException;
import io.micronaut.transaction.exceptions.TransactionSystemException;
import io.micronaut.transaction.exceptions.TransactionUsageException;
import io.micronaut.transaction.support.SmartTransactionObject;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public abstract class JdbcTransactionObjectSupport implements SavepointManager, SmartTransactionObject {
   private static final Logger LOG = LoggerFactory.getLogger(JdbcTransactionObjectSupport.class);
   @Nullable
   private ConnectionHolder connectionHolder;
   @Nullable
   private TransactionDefinition.Isolation previousIsolationLevel;
   private boolean savepointAllowed = false;

   public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder) {
      this.connectionHolder = connectionHolder;
   }

   @NonNull
   public ConnectionHolder getConnectionHolder() {
      Objects.requireNonNull(this.connectionHolder, "No ConnectionHolder available");
      return this.connectionHolder;
   }

   public boolean hasConnectionHolder() {
      return this.connectionHolder != null;
   }

   public void setPreviousIsolationLevel(@Nullable TransactionDefinition.Isolation previousIsolationLevel) {
      this.previousIsolationLevel = previousIsolationLevel;
   }

   @Nullable
   public TransactionDefinition.Isolation getPreviousIsolationLevel() {
      return this.previousIsolationLevel;
   }

   public void setSavepointAllowed(boolean savepointAllowed) {
      this.savepointAllowed = savepointAllowed;
   }

   public boolean isSavepointAllowed() {
      return this.savepointAllowed;
   }

   @Override
   public void flush() {
   }

   @Override
   public Object createSavepoint() throws TransactionException {
      ConnectionHolder conHolder = this.getConnectionHolderForSavepoint();

      try {
         if (!conHolder.supportsSavepoints()) {
            throw new NestedTransactionNotSupportedException("Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
         } else if (conHolder.isRollbackOnly()) {
            throw new CannotCreateTransactionException("Cannot create savepoint for transaction which is already marked as rollback-only");
         } else {
            return conHolder.createSavepoint();
         }
      } catch (SQLException var3) {
         throw new CannotCreateTransactionException("Could not create JDBC savepoint", var3);
      }
   }

   @Override
   public void rollbackToSavepoint(Object savepoint) throws TransactionException {
      ConnectionHolder conHolder = this.getConnectionHolderForSavepoint();

      try {
         conHolder.getConnection().rollback((Savepoint)savepoint);
         conHolder.resetRollbackOnly();
      } catch (Throwable var4) {
         throw new TransactionSystemException("Could not roll back to JDBC savepoint", var4);
      }
   }

   @Override
   public void releaseSavepoint(Object savepoint) throws TransactionException {
      ConnectionHolder conHolder = this.getConnectionHolderForSavepoint();

      try {
         conHolder.getConnection().releaseSavepoint((Savepoint)savepoint);
      } catch (Throwable var4) {
         LOG.debug("Could not explicitly release JDBC savepoint", var4);
      }

   }

   protected ConnectionHolder getConnectionHolderForSavepoint() throws TransactionException {
      if (!this.isSavepointAllowed()) {
         throw new NestedTransactionNotSupportedException("Transaction manager does not allow nested transactions");
      } else if (!this.hasConnectionHolder()) {
         throw new TransactionUsageException("Cannot create nested transaction when not exposing a JDBC transaction");
      } else {
         return this.getConnectionHolder();
      }
   }
}
