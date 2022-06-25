package org.flywaydb.core.internal.strategy;

import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.SqlCallable;

public class RetryStrategy {
   private static int numberOfRetries = 50;
   private static boolean unlimitedRetries;
   private int numberOfRetriesRemaining = numberOfRetries;

   public static void setNumberOfRetries(int retries) {
      numberOfRetries = retries;
      unlimitedRetries = retries < 0;
   }

   private boolean hasMoreRetries() {
      return unlimitedRetries || this.numberOfRetriesRemaining > 0;
   }

   private void nextRetry() {
      if (!unlimitedRetries) {
         --this.numberOfRetriesRemaining;
      }

   }

   private int nextWaitInMilliseconds() {
      return 1000;
   }

   public void doWithRetries(SqlCallable<Boolean> callable, String interruptionMessage, String retriesExceededMessage) throws SQLException {
      while(!callable.call()) {
         try {
            Thread.sleep((long)this.nextWaitInMilliseconds());
         } catch (InterruptedException var5) {
            throw new FlywayException(interruptionMessage, var5);
         }

         if (!this.hasMoreRetries()) {
            throw new FlywayException(retriesExceededMessage);
         }

         this.nextRetry();
      }

   }
}
