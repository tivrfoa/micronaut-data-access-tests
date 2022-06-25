package org.flywaydb.core.internal.jdbc;

import java.util.Map;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.sqlscript.SqlStatement;

public interface StatementInterceptor {
   void init(Database var1, Table var2);

   void schemaHistoryTableCreate(boolean var1);

   void schemaHistoryTableInsert(AppliedMigration var1);

   void close();

   void sqlScript(LoadableResource var1);

   void sqlStatement(SqlStatement var1);

   void interceptCommand(String var1);

   void interceptStatement(String var1);

   void interceptPreparedStatement(String var1, Map<Integer, Object> var2);

   void interceptCallableStatement(String var1);

   void schemaHistoryTableDeleteFailed(Table var1, AppliedMigration var2);
}
