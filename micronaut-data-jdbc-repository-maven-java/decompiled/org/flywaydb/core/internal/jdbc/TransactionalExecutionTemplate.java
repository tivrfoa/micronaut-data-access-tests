package org.flywaydb.core.internal.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;

public class TransactionalExecutionTemplate implements ExecutionTemplate {
   private static final Log LOG = LogFactory.getLog(TransactionalExecutionTemplate.class);
   private final Connection connection;
   private final boolean rollbackOnException;

   @Override
   public <T> T execute(Callable<T> callback) {
      boolean oldAutocommit = true;

      Object var19;
      try {
         oldAutocommit = this.connection.getAutoCommit();
         this.connection.setAutoCommit(false);
         T result = (T)callback.call();
         this.connection.commit();
         var19 = result;
      } catch (Exception var17) {
         RuntimeException rethrow;
         if (var17 instanceof SQLException) {
            rethrow = new FlywaySqlException("Unable to commit transaction", (SQLException)var17);
         } else if (var17 instanceof RuntimeException) {
            rethrow = (RuntimeException)var17;
         } else {
            rethrow = new FlywayException(var17);
         }

         if (this.rollbackOnException) {
            try {
               LOG.debug("Rolling back transaction...");
               this.connection.rollback();
               LOG.debug("Transaction rolled back");
            } catch (SQLException var16) {
               LOG.error("Unable to rollback transaction", var16);
            }
         } else {
            try {
               this.connection.commit();
            } catch (SQLException var15) {
               LOG.error("Unable to commit transaction", var15);
            }
         }

         throw rethrow;
      } finally {
         try {
            this.connection.setAutoCommit(oldAutocommit);
         } catch (SQLException var14) {
            LOG.error("Unable to restore autocommit to original value for connection", var14);
         }

      }

      return (T)var19;
   }

   public TransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
      this.connection = connection;
      this.rollbackOnException = rollbackOnException;
   }
}
