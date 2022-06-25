package org.flywaydb.core.internal.jdbc;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;

public class PlainExecutionTemplate implements ExecutionTemplate {
   private static final Log LOG = LogFactory.getLog(PlainExecutionTemplate.class);
   private final boolean skipErrorLog;

   public PlainExecutionTemplate() {
      this.skipErrorLog = false;
   }

   public PlainExecutionTemplate(boolean skipErrorLog) {
      this.skipErrorLog = skipErrorLog;
   }

   @Override
   public <T> T execute(Callable<T> callback) {
      try {
         LOG.debug("Performing operation in non-transactional context.");
         return (T)callback.call();
      } catch (Exception var3) {
         if (!this.skipErrorLog) {
            LOG.error("Failed to execute operation in non-transactional context. Please restore backups and roll back database and code!");
         }

         if (var3 instanceof SQLException) {
            throw new FlywaySqlException("Failed to execute operation.", (SQLException)var3);
         } else if (var3 instanceof RuntimeException) {
            throw (RuntimeException)var3;
         } else {
            throw new FlywayException(var3);
         }
      }
   }
}
