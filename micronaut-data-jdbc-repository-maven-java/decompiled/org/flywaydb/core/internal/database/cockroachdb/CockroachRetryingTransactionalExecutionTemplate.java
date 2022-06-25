package org.flywaydb.core.internal.database.cockroachdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.jdbc.TransactionalExecutionTemplate;

public class CockroachRetryingTransactionalExecutionTemplate extends TransactionalExecutionTemplate {
   private static final Log LOG = LogFactory.getLog(CockroachRetryingTransactionalExecutionTemplate.class);
   private static final String DEADLOCK_OR_TIMEOUT_ERROR_CODE = "40001";
   private static final int MAX_RETRIES = 50;

   CockroachRetryingTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
      super(connection, rollbackOnException);
   }

   @Override
   public <T> T execute(Callable<T> transactionCallback) {
      int retryCount = 0;

      while(true) {
         try {
            return (T)transactionCallback.call();
         } catch (SQLException var4) {
            if (!"40001".equals(var4.getSQLState()) || retryCount >= 50) {
               LOG.info("error: " + var4);
               throw new FlywayException(var4);
            }

            ++retryCount;
         } catch (RuntimeException var5) {
            throw var5;
         } catch (Exception var6) {
            throw new FlywayException(var6);
         }
      }
   }
}
