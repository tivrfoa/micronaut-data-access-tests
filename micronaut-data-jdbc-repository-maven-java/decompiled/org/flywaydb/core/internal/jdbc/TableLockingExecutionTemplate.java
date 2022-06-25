package org.flywaydb.core.internal.jdbc;

import java.util.concurrent.Callable;
import org.flywaydb.core.internal.database.base.Table;

public class TableLockingExecutionTemplate implements ExecutionTemplate {
   private final Table table;
   private final ExecutionTemplate executionTemplate;

   @Override
   public <T> T execute(final Callable<T> callback) {
      return this.executionTemplate.execute(new Callable<T>() {
         public T call() throws Exception {
            Object var1;
            try {
               TableLockingExecutionTemplate.this.table.lock();
               var1 = callback.call();
            } finally {
               TableLockingExecutionTemplate.this.table.unlock();
            }

            return (T)var1;
         }
      });
   }

   TableLockingExecutionTemplate(Table table, ExecutionTemplate executionTemplate) {
      this.table = table;
      this.executionTemplate = executionTemplate;
   }
}
