package org.flywaydb.core.internal.database;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

public class InsertRowLock {
   private static final Log LOG = LogFactory.getLog(InsertRowLock.class);
   private static final Random random = new Random();
   private static final int NUM_THREADS = 2;
   private final String tableLockString = this.getNextRandomString();
   private final JdbcTemplate jdbcTemplate;
   private final int lockTimeoutMins;
   private final ScheduledExecutorService executor;
   private ScheduledFuture<?> scheduledFuture;

   public InsertRowLock(JdbcTemplate jdbcTemplate, int lockTimeoutMins) {
      this.jdbcTemplate = jdbcTemplate;
      this.lockTimeoutMins = lockTimeoutMins;
      this.executor = this.createScheduledExecutor();
   }

   public void doLock(String insertStatementTemplate, String updateLockStatement, String deleteExpiredLockStatement, String booleanTrue) throws SQLException {
      int retryCount = 0;

      while(true) {
         try {
            this.jdbcTemplate.execute(this.generateDeleteExpiredLockStatement(deleteExpiredLockStatement));
            if (this.insertLockingRow(insertStatementTemplate, booleanTrue)) {
               this.scheduledFuture = this.startLockWatchingThread(String.format(updateLockStatement.replace("?", "%s"), this.tableLockString));
               return;
            }

            if (retryCount < 50) {
               ++retryCount;
               LOG.debug("Waiting for lock on Flyway schema history table");
            } else {
               LOG.error(
                  "Waiting for lock on Flyway schema history table. Application may be deadlocked. Lock row may require manual removal from the schema history table."
               );
            }

            Thread.sleep(1000L);
         } catch (InterruptedException var7) {
         }
      }
   }

   private String generateDeleteExpiredLockStatement(String deleteExpiredLockStatementTemplate) {
      LocalDateTime zonedDateTime = LocalDateTime.now(ZoneOffset.UTC).minusMinutes((long)this.lockTimeoutMins);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
      return String.format(deleteExpiredLockStatementTemplate.replace("?", "%s"), zonedDateTime.format(formatter));
   }

   private boolean insertLockingRow(String insertStatementTemplate, String booleanTrue) {
      String insertStatement = String.format(
         insertStatementTemplate.replace("?", "%s"), -100, "'" + this.tableLockString + "'", "'flyway-lock'", "''", "''", 0, "''", 0, booleanTrue
      );
      Results results = this.jdbcTemplate.executeStatement(insertStatement);
      return results.getException() == null;
   }

   public void doUnlock(String deleteLockTemplate) throws SQLException {
      this.stopLockWatchingThread();
      String deleteLockStatement = String.format(deleteLockTemplate.replace("?", "%s"), this.tableLockString);
      this.jdbcTemplate.execute(deleteLockStatement);
   }

   private String getNextRandomString() {
      return new BigInteger(128, random).toString(16);
   }

   private ScheduledExecutorService createScheduledExecutor() {
      return Executors.newScheduledThreadPool(2, r -> {
         Thread t = Executors.defaultThreadFactory().newThread(r);
         t.setDaemon(true);
         return t;
      });
   }

   private ScheduledFuture<?> startLockWatchingThread(String updateLockStatement) {
      Runnable lockUpdatingTask = () -> {
         LOG.debug("Updating lock in Flyway schema history table");
         this.jdbcTemplate.executeStatement(updateLockStatement);
      };
      return this.executor.scheduleAtFixedRate(lockUpdatingTask, 0L, (long)(this.lockTimeoutMins / 2), TimeUnit.MINUTES);
   }

   private void stopLockWatchingThread() {
      this.scheduledFuture.cancel(true);
   }
}
