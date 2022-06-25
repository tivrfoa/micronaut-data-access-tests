package org.flywaydb.core.internal.resolver.sql;

import java.sql.SQLException;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;

public class SqlMigrationExecutor implements MigrationExecutor {
   private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
   private final SqlScript sqlScript;
   private final boolean undo;
   private final boolean batch;

   @Override
   public void execute(Context context) throws SQLException {
      DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(context.getConnection());
      DatabaseExecutionStrategy strategy = databaseType.createExecutionStrategy(context.getConnection());
      strategy.execute(() -> {
         this.executeOnce(context);
         return true;
      });
   }

   private void executeOnce(Context context) {
      boolean outputQueryResults = false;
      this.sqlScriptExecutorFactory.createSqlScriptExecutor(context.getConnection(), this.undo, this.batch, outputQueryResults).execute(this.sqlScript);
   }

   @Override
   public boolean canExecuteInTransaction() {
      return this.sqlScript.executeInTransaction();
   }

   @Override
   public boolean shouldExecute() {
      return this.sqlScript.shouldExecute();
   }

   SqlMigrationExecutor(SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScript sqlScript, boolean undo, boolean batch) {
      this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
      this.sqlScript = sqlScript;
      this.undo = undo;
      this.batch = batch;
   }
}
