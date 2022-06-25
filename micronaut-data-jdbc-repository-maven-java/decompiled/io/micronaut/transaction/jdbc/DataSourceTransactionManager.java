package io.micronaut.transaction.jdbc;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.exceptions.CannotCreateTransactionException;
import io.micronaut.transaction.exceptions.TransactionSystemException;
import io.micronaut.transaction.jdbc.exceptions.CannotGetJdbcConnectionException;
import io.micronaut.transaction.support.AbstractSynchronousTransactionManager;
import io.micronaut.transaction.support.DefaultTransactionStatus;
import io.micronaut.transaction.support.ResourceTransactionManager;
import io.micronaut.transaction.support.SynchronousTransactionState;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import io.micronaut.transaction.support.TransactionSynchronizationUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Objects;
import javax.sql.DataSource;

@EachBean(DataSource.class)
@TypeHint({DataSourceTransactionManager.class})
public class DataSourceTransactionManager
   extends AbstractSynchronousTransactionManager<Connection>
   implements ResourceTransactionManager<DataSource, Connection> {
   private final DataSource dataSource;
   private boolean enforceReadOnly = false;

   public DataSourceTransactionManager(@NonNull DataSource dataSource) {
      Objects.requireNonNull(dataSource, "DataSource cannot be null");
      this.setNestedTransactionAllowed(true);
      dataSource = DelegatingDataSource.unwrapDataSource(dataSource);
      this.dataSource = dataSource;
   }

   @NonNull
   @Override
   protected Object getTransactionStateKey() {
      return this.getDataSource();
   }

   @NonNull
   public DataSource getDataSource() {
      return this.dataSource;
   }

   public void setEnforceReadOnly(boolean enforceReadOnly) {
      this.enforceReadOnly = enforceReadOnly;
   }

   public boolean isEnforceReadOnly() {
      return this.enforceReadOnly;
   }

   public DataSource getResourceFactory() {
      return this.getDataSource();
   }

   protected Connection getConnection(Object transaction) {
      DataSourceTransactionManager.DataSourceTransactionObject dsto = (DataSourceTransactionManager.DataSourceTransactionObject)transaction;
      return dsto.getConnection();
   }

   @Override
   public boolean hasConnection() {
      ConnectionHolder conHolder = (ConnectionHolder)TransactionSynchronizationManager.getResource(this.dataSource);
      return conHolder != null && conHolder.hasConnection();
   }

   @Override
   protected Object doGetTransaction() {
      ConnectionHolder conHolder = (ConnectionHolder)TransactionSynchronizationManager.getResource(this.dataSource);
      DataSourceTransactionManager.DataSourceTransactionObject txObject = new DataSourceTransactionManager.DataSourceTransactionObject(conHolder, false);
      txObject.setSavepointAllowed(this.isNestedTransactionAllowed());
      return txObject;
   }

   @Override
   protected boolean isExistingTransaction(Object transaction) {
      DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)transaction;
      return txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive();
   }

   @Override
   protected void doBegin(Object transaction, TransactionDefinition definition) {
      DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)transaction;
      Connection con = null;

      try {
         if (!txObject.hasConnectionHolder() || txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
            Connection newCon = this.dataSource.getConnection();
            if (this.logger.isDebugEnabled()) {
               this.logger.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
            }

            txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
         }

         txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
         con = txObject.getConnectionHolder().getConnection();
         TransactionDefinition.Isolation previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
         txObject.setPreviousIsolationLevel(previousIsolationLevel);
         if (con.getAutoCommit()) {
            txObject.setMustRestoreAutoCommit(true);
            if (this.logger.isDebugEnabled()) {
               this.logger.debug("Switching JDBC Connection [" + con + "] to manual commit");
            }

            con.setAutoCommit(false);
         }

         this.prepareTransactionalConnection(con, definition);
         txObject.getConnectionHolder().setTransactionActive(true);
         Duration timeout = this.determineTimeout(definition);
         if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
            txObject.getConnectionHolder().setTimeout(timeout);
         }

         if (txObject.isNewConnectionHolder()) {
            TransactionSynchronizationManager.bindResource(this.dataSource, txObject.getConnectionHolder());
         }

      } catch (Throwable var7) {
         if (txObject.isNewConnectionHolder()) {
            DataSourceUtils.releaseConnection(con, this.dataSource);
            txObject.setConnectionHolder(null, false);
         }

         throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", var7);
      }
   }

   @Override
   protected Object doSuspend(Object transaction) {
      DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)transaction;
      txObject.setConnectionHolder(null);
      return TransactionSynchronizationManager.unbindResource(this.dataSource);
   }

   @Override
   protected void doResume(@Nullable Object transaction, Object suspendedResources) {
      TransactionSynchronizationManager.bindResource(this.dataSource, suspendedResources);
   }

   @Override
   protected void doCommit(DefaultTransactionStatus status) {
      DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)status.getTransaction();
      Connection con = txObject.getConnectionHolder().getConnection();
      if (status.isDebug()) {
         this.logger.debug("Committing JDBC transaction on Connection [" + con + "]");
      }

      try {
         con.commit();
      } catch (SQLException var5) {
         throw new TransactionSystemException("Could not commit JDBC transaction", var5);
      }
   }

   @Override
   protected void doRollback(DefaultTransactionStatus status) {
      DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)status.getTransaction();
      Connection con = txObject.getConnectionHolder().getConnection();
      if (status.isDebug()) {
         this.logger.debug("Rolling back JDBC transaction on Connection [" + con + "]");
      }

      try {
         con.rollback();
      } catch (SQLException var5) {
         throw new TransactionSystemException("Could not roll back JDBC transaction", var5);
      }
   }

   @Override
   protected void doSetRollbackOnly(DefaultTransactionStatus status) {
      DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)status.getTransaction();
      if (status.isDebug()) {
         this.logger.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "] rollback-only");
      }

      txObject.setRollbackOnly();
   }

   @Override
   protected void doCleanupAfterCompletion(Object transaction) {
      DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)transaction;
      if (txObject.isNewConnectionHolder()) {
         TransactionSynchronizationManager.unbindResource(this.dataSource);
      }

      Connection con = txObject.getConnectionHolder().getConnection();

      try {
         if (txObject.isMustRestoreAutoCommit()) {
            con.setAutoCommit(true);
         }

         DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
      } catch (Throwable var5) {
         this.logger.debug("Could not reset JDBC Connection after transaction", var5);
      }

      if (txObject.isNewConnectionHolder()) {
         if (this.logger.isDebugEnabled()) {
            this.logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
         }

         DataSourceUtils.releaseConnection(con, this.dataSource);
      }

      txObject.getConnectionHolder().clear();
   }

   protected void prepareTransactionalConnection(Connection con, TransactionDefinition definition) throws SQLException {
      if (this.isEnforceReadOnly() && definition.isReadOnly()) {
         Statement stmt = con.createStatement();
         Throwable var4 = null;

         try {
            stmt.executeUpdate("SET TRANSACTION READ ONLY");
         } catch (Throwable var13) {
            var4 = var13;
            throw var13;
         } finally {
            if (stmt != null) {
               if (var4 != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var12) {
                     var4.addSuppressed(var12);
                  }
               } else {
                  stmt.close();
               }
            }

         }
      }

   }

   @NonNull
   public Connection getConnection() {
      return DataSourceUtils.getConnection(this.dataSource, false);
   }

   private class DataSourceTransactionObject extends JdbcTransactionObjectSupport implements ConnectionHandle {
      private boolean newConnectionHolder;
      private boolean mustRestoreAutoCommit;

      public DataSourceTransactionObject(ConnectionHolder connectionHolder, boolean newConnectionHolder) {
         this.newConnectionHolder = newConnectionHolder;
         this.setConnectionHolder(connectionHolder);
      }

      public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder, boolean newConnectionHolder) {
         super.setConnectionHolder(connectionHolder);
         this.newConnectionHolder = newConnectionHolder;
      }

      public boolean isNewConnectionHolder() {
         return this.newConnectionHolder;
      }

      public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
         this.mustRestoreAutoCommit = mustRestoreAutoCommit;
      }

      public boolean isMustRestoreAutoCommit() {
         return this.mustRestoreAutoCommit;
      }

      public void setRollbackOnly() {
         this.getConnectionHolder().setRollbackOnly();
      }

      @Override
      public boolean isRollbackOnly() {
         return this.getConnectionHolder().isRollbackOnly();
      }

      @Override
      public void flush() {
         SynchronousTransactionState state = TransactionSynchronizationManager.getRequiredSynchronousTransactionState(
            DataSourceTransactionManager.this.dataSource
         );
         if (state.isSynchronizationActive()) {
            TransactionSynchronizationUtils.triggerFlush(state);
         }

      }

      @Override
      public Connection getConnection() {
         ConnectionHolder connectionHolder = this.getConnectionHolder();
         if (connectionHolder != null) {
            return connectionHolder.getConnection();
         } else {
            throw new CannotGetJdbcConnectionException("No JDBC Connection available");
         }
      }
   }
}
