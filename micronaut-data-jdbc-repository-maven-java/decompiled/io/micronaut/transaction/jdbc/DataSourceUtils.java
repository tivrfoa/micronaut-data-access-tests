package io.micronaut.transaction.jdbc;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.jdbc.exceptions.CannotGetJdbcConnectionException;
import io.micronaut.transaction.support.TransactionSynchronization;
import io.micronaut.transaction.support.TransactionSynchronizationAdapter;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataSourceUtils {
   public static final int CONNECTION_SYNCHRONIZATION_ORDER = 1000;
   private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceUtils.class);

   public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
      try {
         return doGetConnection(dataSource, true);
      } catch (SQLException var2) {
         throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", var2);
      } catch (IllegalStateException var3) {
         throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection: " + var3.getMessage());
      }
   }

   public static Connection getConnection(DataSource dataSource, boolean allowCreate) throws CannotGetJdbcConnectionException {
      try {
         return doGetConnection(dataSource, allowCreate);
      } catch (SQLException var3) {
         throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", var3);
      } catch (IllegalStateException var4) {
         throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection: " + var4.getMessage());
      }
   }

   private static Connection doGetConnection(DataSource dataSource, boolean allowCreate) throws SQLException {
      Objects.requireNonNull(dataSource, "No DataSource specified");
      ConnectionHolder conHolder = (ConnectionHolder)TransactionSynchronizationManager.getResource(dataSource);
      if (conHolder == null || !conHolder.hasConnection() && !conHolder.isSynchronizedWithTransaction()) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fetching JDBC Connection from DataSource");
         }

         if (!allowCreate) {
            throw new CannotGetJdbcConnectionException("No current JDBC Connection found. Consider wrapping this call in transactional boundaries.");
         } else {
            Connection con = fetchConnection(dataSource);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
               try {
                  ConnectionHolder holderToUse = conHolder;
                  if (conHolder == null) {
                     holderToUse = new ConnectionHolder(con);
                  } else {
                     conHolder.setConnection(con);
                  }

                  holderToUse.requested();
                  TransactionSynchronizationManager.registerSynchronization(new DataSourceUtils.ConnectionSynchronization(holderToUse, dataSource));
                  holderToUse.setSynchronizedWithTransaction(true);
                  if (holderToUse != conHolder) {
                     TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
                  }
               } catch (RuntimeException var5) {
                  releaseConnection(con, dataSource);
                  throw var5;
               }
            }

            return con;
         }
      } else {
         conHolder.requested();
         if (!conHolder.hasConnection()) {
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Fetching resumed JDBC Connection from DataSource");
            }

            if (!allowCreate) {
               throw new CannotGetJdbcConnectionException("No current JDBC Connection found. Consider wrapping this call in transactional boundaries.");
            }

            conHolder.setConnection(fetchConnection(dataSource));
         }

         return conHolder.getConnection();
      }
   }

   private static Connection fetchConnection(DataSource dataSource) throws SQLException {
      Connection con = dataSource.getConnection();
      if (con == null) {
         throw new IllegalStateException("DataSource returned null from getConnection(): " + dataSource);
      } else {
         return con;
      }
   }

   @Nullable
   public static TransactionDefinition.Isolation prepareConnectionForTransaction(Connection con, @Nullable TransactionDefinition definition) throws SQLException {
      Objects.requireNonNull(con, "No Connection specified");
      if (definition != null && definition.isReadOnly()) {
         try {
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Setting JDBC Connection [" + con + "] read-only");
            }

            con.setReadOnly(true);
         } catch (RuntimeException | SQLException var5) {
            for(Throwable exToCheck = var5; exToCheck != null; exToCheck = exToCheck.getCause()) {
               if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
                  throw var5;
               }
            }

            LOGGER.debug("Could not set JDBC Connection read-only", var5);
         }
      }

      TransactionDefinition.Isolation previousIsolationLevel = null;
      if (definition != null) {
         TransactionDefinition.Isolation isolationLevel = definition.getIsolationLevel();
         if (isolationLevel != TransactionDefinition.Isolation.DEFAULT) {
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Changing isolation level of JDBC Connection [" + con + "] to " + isolationLevel);
            }

            int currentIsolation = con.getTransactionIsolation();
            if (currentIsolation != isolationLevel.getCode()) {
               previousIsolationLevel = TransactionDefinition.Isolation.valueOf(currentIsolation);
               con.setTransactionIsolation(isolationLevel.getCode());
            }
         }
      }

      return previousIsolationLevel;
   }

   public static void resetConnectionAfterTransaction(Connection con, @Nullable TransactionDefinition.Isolation previousIsolationLevel) {
      Objects.requireNonNull(con, "No Connection specified");

      try {
         if (previousIsolationLevel != null) {
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Resetting isolation level of JDBC Connection [" + con + "] to " + previousIsolationLevel);
            }

            con.setTransactionIsolation(previousIsolationLevel.getCode());
         }

         if (con.isReadOnly()) {
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
            }

            con.setReadOnly(false);
         }
      } catch (Throwable var3) {
         LOGGER.debug("Could not reset JDBC Connection after transaction", var3);
      }

   }

   public static boolean isConnectionTransactional(Connection con, @Nullable DataSource dataSource) {
      if (dataSource == null) {
         return false;
      } else {
         ConnectionHolder conHolder = (ConnectionHolder)TransactionSynchronizationManager.getResource(dataSource);
         return conHolder != null && connectionEquals(conHolder, con);
      }
   }

   public static void applyTransactionTimeout(Statement stmt, @Nullable DataSource dataSource) throws SQLException {
      applyTimeout(stmt, dataSource, -1);
   }

   public static void applyTimeout(Statement stmt, @Nullable DataSource dataSource, int timeout) throws SQLException {
      Objects.requireNonNull(stmt, "No Statement specified");
      ConnectionHolder holder = null;
      if (dataSource != null) {
         holder = (ConnectionHolder)TransactionSynchronizationManager.getResource(dataSource);
      }

      if (holder != null && holder.hasTimeout()) {
         stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
      } else if (timeout >= 0) {
         stmt.setQueryTimeout(timeout);
      }

   }

   public static void releaseConnection(@Nullable Connection con, @Nullable DataSource dataSource) {
      try {
         doReleaseConnection(con, dataSource);
      } catch (SQLException var3) {
         LOGGER.debug("Could not close JDBC Connection", var3);
      } catch (Throwable var4) {
         LOGGER.debug("Unexpected exception on closing JDBC Connection", var4);
      }

   }

   public static void doReleaseConnection(@Nullable Connection con, @Nullable DataSource dataSource) throws SQLException {
      if (con != null) {
         if (dataSource != null) {
            ConnectionHolder conHolder = (ConnectionHolder)TransactionSynchronizationManager.getResource(dataSource);
            if (conHolder != null && connectionEquals(conHolder, con)) {
               conHolder.released();
               return;
            }
         }

         doCloseConnection(con, dataSource);
      }
   }

   public static void doCloseConnection(Connection con, @Nullable DataSource dataSource) throws SQLException {
      con.close();
   }

   private static boolean connectionEquals(ConnectionHolder conHolder, Connection passedInCon) {
      if (!conHolder.hasConnection()) {
         return false;
      } else {
         Connection heldCon = conHolder.getConnection();
         return heldCon == passedInCon || heldCon.equals(passedInCon) || getTargetConnection(heldCon).equals(passedInCon);
      }
   }

   public static Connection getTargetConnection(Connection con) {
      return con;
   }

   private static int getConnectionSynchronizationOrder(DataSource dataSource) {
      int order = 1000;

      for(DataSource currDs = dataSource; currDs instanceof DelegatingDataSource; currDs = ((DelegatingDataSource)currDs).getTargetDataSource()) {
         --order;
      }

      return order;
   }

   private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {
      private final ConnectionHolder connectionHolder;
      private final DataSource dataSource;
      private int order;
      private boolean holderActive = true;

      public ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
         this.connectionHolder = connectionHolder;
         this.dataSource = dataSource;
         this.order = DataSourceUtils.getConnectionSynchronizationOrder(dataSource);
      }

      @Override
      public int getOrder() {
         return this.order;
      }

      @Override
      public void suspend() {
         if (this.holderActive) {
            TransactionSynchronizationManager.unbindResource(this.dataSource);
            if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
               DataSourceUtils.releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
               this.connectionHolder.setConnection(null);
            }
         }

      }

      @Override
      public void resume() {
         if (this.holderActive) {
            TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
         }

      }

      @Override
      public void beforeCompletion() {
         if (!this.connectionHolder.isOpen()) {
            TransactionSynchronizationManager.unbindResource(this.dataSource);
            this.holderActive = false;
            if (this.connectionHolder.hasConnection()) {
               DataSourceUtils.releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
            }
         }

      }

      @Override
      public void afterCompletion(@NonNull TransactionSynchronization.Status status) {
         if (this.holderActive) {
            TransactionSynchronizationManager.unbindResourceIfPossible(this.dataSource);
            this.holderActive = false;
            if (this.connectionHolder.hasConnection()) {
               DataSourceUtils.releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
               this.connectionHolder.setConnection(null);
            }
         }

         this.connectionHolder.reset();
      }
   }
}
