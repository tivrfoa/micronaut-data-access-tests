package org.flywaydb.core.internal.database.cockroachdb;

import java.sql.SQLException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.util.SqlCallable;

public class CockroachDBRetryingStrategy implements DatabaseExecutionStrategy {
   private static final Log LOG = LogFactory.getLog(CockroachDBRetryingStrategy.class);
   private static final String DEADLOCK_OR_TIMEOUT_ERROR_CODE = "40001";
   private static final int MAX_RETRIES = 50;

   @Override
   public <T> T execute(SqlCallable<T> callable) throws SQLException {
      int retryCount = 0;

      while(true) {
         try {
            return callable.call();
         } catch (SQLException var4) {
            this.checkRetryOrThrow(var4, retryCount);
            ++retryCount;
         }
      }
   }

   void checkRetryOrThrow(SQLException e, int retryCount) throws SQLException {
      if ("40001".equals(e.getSQLState()) && retryCount < 50) {
         LOG.info("Retrying because of deadlock or timeout: " + e.getMessage());
      }

      throw e;
   }
}
