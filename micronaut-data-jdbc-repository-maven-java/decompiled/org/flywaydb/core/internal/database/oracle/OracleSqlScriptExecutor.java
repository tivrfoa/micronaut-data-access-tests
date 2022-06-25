package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.sqlscript.DefaultSqlScriptExecutor;

public class OracleSqlScriptExecutor extends DefaultSqlScriptExecutor {
   private static final Log LOG = LogFactory.getLog(OracleSqlScriptExecutor.class);

   public OracleSqlScriptExecutor(
      JdbcTemplate jdbcTemplate,
      CallbackExecutor callbackExecutor,
      boolean undo,
      boolean batch,
      boolean outputQueryResults,
      StatementInterceptor statementInterceptor
   ) {
      super(jdbcTemplate, callbackExecutor, undo, batch, outputQueryResults, statementInterceptor);
   }
}
