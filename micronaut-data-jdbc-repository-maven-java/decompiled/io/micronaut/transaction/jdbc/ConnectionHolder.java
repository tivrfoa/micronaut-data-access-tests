package io.micronaut.transaction.jdbc;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.support.ResourceHolderSupport;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Objects;

public class ConnectionHolder extends ResourceHolderSupport {
   public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";
   @Nullable
   private ConnectionHandle connectionHandle;
   @Nullable
   private Connection currentConnection;
   private boolean transactionActive = false;
   @Nullable
   private Boolean savepointsSupported;
   private int savepointCounter = 0;

   public ConnectionHolder(ConnectionHandle connectionHandle) {
      Objects.requireNonNull(connectionHandle, "ConnectionHandle must not be null");
      this.connectionHandle = connectionHandle;
   }

   public ConnectionHolder(Connection connection) {
      this.connectionHandle = new SimpleConnectionHandle(connection);
   }

   public ConnectionHolder(Connection connection, boolean transactionActive) {
      this(connection);
      this.transactionActive = transactionActive;
   }

   @Nullable
   public ConnectionHandle getConnectionHandle() {
      return this.connectionHandle;
   }

   protected boolean hasConnection() {
      return this.connectionHandle != null;
   }

   protected void setTransactionActive(boolean transactionActive) {
      this.transactionActive = transactionActive;
   }

   protected boolean isTransactionActive() {
      return this.transactionActive;
   }

   protected void setConnection(@Nullable Connection connection) {
      if (this.currentConnection != null) {
         if (this.connectionHandle != null) {
            this.connectionHandle.releaseConnection(this.currentConnection);
         }

         this.currentConnection = null;
      }

      if (connection != null) {
         this.connectionHandle = new SimpleConnectionHandle(connection);
      } else {
         this.connectionHandle = null;
      }

   }

   public Connection getConnection() {
      Objects.requireNonNull(this.connectionHandle, "Active Connection is required");
      if (this.currentConnection == null) {
         this.currentConnection = this.connectionHandle.getConnection();
      }

      return this.currentConnection;
   }

   public boolean supportsSavepoints() throws SQLException {
      if (this.savepointsSupported == null) {
         this.savepointsSupported = this.getConnection().getMetaData().supportsSavepoints();
      }

      return this.savepointsSupported;
   }

   public Savepoint createSavepoint() throws SQLException {
      ++this.savepointCounter;
      return this.getConnection().setSavepoint("SAVEPOINT_" + this.savepointCounter);
   }

   @Override
   public void released() {
      super.released();
      if (!this.isOpen() && this.currentConnection != null) {
         if (this.connectionHandle != null) {
            this.connectionHandle.releaseConnection(this.currentConnection);
         }

         this.currentConnection = null;
      }

   }

   @Override
   public void clear() {
      super.clear();
      this.transactionActive = false;
      this.savepointsSupported = null;
      this.savepointCounter = 0;
   }
}
