package org.flywaydb.core.api.executor;

import java.sql.SQLException;

public interface MigrationExecutor {
   void execute(Context var1) throws SQLException;

   boolean canExecuteInTransaction();

   boolean shouldExecute();
}
