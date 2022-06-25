package org.flywaydb.database.mysql;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

public class MySQLNamedLockTemplate {
   private static final Log LOG = LogFactory.getLog(MySQLNamedLockTemplate.class);
   private final JdbcTemplate jdbcTemplate;
   private final String lockName;

   MySQLNamedLockTemplate(JdbcTemplate jdbcTemplate, int discriminator) {
      this.jdbcTemplate = jdbcTemplate;
      this.lockName = "Flyway-" + discriminator;
   }

   public <T> T execute(Callable<T> callable) {
      Object e;
      try {
         this.lock();
         e = callable.call();
      } catch (SQLException var12) {
         throw new FlywaySqlException("Unable to acquire MySQL named lock: " + this.lockName, var12);
      } catch (Exception var13) {
         RuntimeException rethrow;
         if (var13 instanceof RuntimeException) {
            rethrow = (RuntimeException)var13;
         } else {
            rethrow = new FlywayException(var13);
         }

         throw rethrow;
      } finally {
         try {
            this.jdbcTemplate.execute("SELECT RELEASE_LOCK('" + this.lockName + "')");
         } catch (SQLException var11) {
            LOG.error("Unable to release MySQL named lock: " + this.lockName, var11);
         }

      }

      return (T)e;
   }

   private void lock() throws SQLException {
      while(!this.tryLock()) {
         try {
            Thread.sleep(100L);
         } catch (InterruptedException var2) {
            throw new FlywayException("Interrupted while attempting to acquire MySQL named lock: " + this.lockName, var2);
         }
      }

   }

   private boolean tryLock() throws SQLException {
      return this.jdbcTemplate.queryForInt("SELECT GET_LOCK(?,10)", this.lockName) == 1;
   }
}
