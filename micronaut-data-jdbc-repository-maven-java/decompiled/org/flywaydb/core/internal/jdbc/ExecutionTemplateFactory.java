package org.flywaydb.core.internal.jdbc;

import java.sql.Connection;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;

public class ExecutionTemplateFactory {
   public static ExecutionTemplate createExecutionTemplate(Connection connection) {
      return createTransactionalExecutionTemplate(connection, true, DatabaseTypeRegister.getDatabaseTypeForConnection(connection));
   }

   public static ExecutionTemplate createExecutionTemplate(Connection connection, Database database) {
      return (ExecutionTemplate)(database.supportsMultiStatementTransactions()
         ? createTransactionalExecutionTemplate(connection, true, database.getDatabaseType())
         : new PlainExecutionTemplate());
   }

   public static ExecutionTemplate createTableExclusiveExecutionTemplate(Connection connection, Table table, Database database) {
      return database.supportsMultiStatementTransactions()
         ? new TableLockingExecutionTemplate(
            table, createTransactionalExecutionTemplate(connection, database.supportsDdlTransactions(), database.getDatabaseType())
         )
         : new TableLockingExecutionTemplate(table, new PlainExecutionTemplate());
   }

   private static ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException, DatabaseType databaseType) {
      return databaseType.createTransactionalExecutionTemplate(connection, rollbackOnException);
   }
}
