package org.flywaydb.core.internal.database.postgresql;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.strategy.RetryStrategy;

public class PostgreSQLAdvisoryLockTemplate {
   private static final Log LOG = LogFactory.getLog(PostgreSQLAdvisoryLockTemplate.class);
   private static final long LOCK_MAGIC_NUM = 77431708279161L;
   private final JdbcTemplate jdbcTemplate;
   private final long lockNum;

   PostgreSQLAdvisoryLockTemplate(JdbcTemplate jdbcTemplate, int discriminator) {
      this.jdbcTemplate = jdbcTemplate;
      this.lockNum = 77431708279161L + (long)discriminator;
   }

   public <T> T execute(Callable<T> callable) {
      RuntimeException rethrow = null;

      Object e;
      try {
         this.lock();
         e = callable.call();
      } catch (SQLException var8) {
         rethrow = new FlywaySqlException("Unable to acquire PostgreSQL advisory lock", var8);
         throw rethrow;
      } catch (Exception var9) {
         if (var9 instanceof RuntimeException) {
            rethrow = (RuntimeException)var9;
         } else {
            rethrow = new FlywayException(var9);
         }

         throw rethrow;
      } finally {
         this.unlock(rethrow);
      }

      return (T)e;
   }

   private void lock() throws SQLException {
      RetryStrategy strategy = new RetryStrategy();
      strategy.doWithRetries(
         this::tryLock,
         "Interrupted while attempting to acquire PostgreSQL advisory lock",
         "Number of retries exceeded while attempting to acquire PostgreSQL advisory lock. Configure the number of retries with the 'lockRetryCount' configuration option: https://rd.gt/3A57jfk"
      );
   }

   private boolean tryLock() throws SQLException {
      List<Boolean> results = this.jdbcTemplate.query("SELECT pg_try_advisory_lock(" + this.lockNum + ")", rs -> rs.getBoolean("pg_try_advisory_lock"));
      return results.size() == 1 && results.get(0);
   }

   private void unlock(RuntimeException rethrow) throws FlywaySqlException {
      try {
         boolean unlocked = this.jdbcTemplate.queryForBoolean("SELECT pg_advisory_unlock(" + this.lockNum + ")");
         if (!unlocked) {
            if (rethrow == null) {
               throw new FlywayException("Unable to release PostgreSQL advisory lock");
            }

            LOG.error("Unable to release PostgreSQL advisory lock");
         }
      } catch (SQLException var3) {
         if (rethrow == null) {
            throw new FlywaySqlException("Unable to release PostgreSQL advisory lock", var3);
         }

         LOG.error("Unable to release PostgreSQL advisory lock", var3);
      }

   }
}
