package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.AsciiTable;

public class DefaultSqlScriptExecutor implements SqlScriptExecutor {
   private static final Log LOG = LogFactory.getLog(DefaultSqlScriptExecutor.class);
   protected final JdbcTemplate jdbcTemplate;

   public DefaultSqlScriptExecutor(
      JdbcTemplate jdbcTemplate,
      CallbackExecutor callbackExecutor,
      boolean undo,
      boolean batch,
      boolean outputQueryResults,
      StatementInterceptor statementInterceptor
   ) {
      this.jdbcTemplate = jdbcTemplate;
   }

   @Override
   public void execute(SqlScript sqlScript) {
      SqlStatementIterator sqlStatementIterator = sqlScript.getSqlStatements();

      try {
         while(sqlStatementIterator.hasNext()) {
            SqlStatement sqlStatement = (SqlStatement)sqlStatementIterator.next();
            this.executeStatement(this.jdbcTemplate, sqlScript, sqlStatement);
         }
      } catch (Throwable var6) {
         if (sqlStatementIterator != null) {
            try {
               sqlStatementIterator.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (sqlStatementIterator != null) {
         sqlStatementIterator.close();
      }

   }

   protected void logStatementExecution(SqlStatement sqlStatement) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("Executing SQL: " + sqlStatement.getSql());
      }

   }

   protected void executeStatement(JdbcTemplate jdbcTemplate, SqlScript sqlScript, SqlStatement sqlStatement) {
      this.logStatementExecution(sqlStatement);
      String sql = sqlStatement.getSql() + sqlStatement.getDelimiter();
      Results results = sqlStatement.execute(jdbcTemplate);
      if (results.getException() != null) {
         this.printWarnings(results);
         this.handleException(results, sqlScript, sqlStatement);
      } else {
         this.printWarnings(results);
         this.handleResults(results);
      }
   }

   protected void handleResults(Results results) {
      for(Result result : results.getResults()) {
         long updateCount = result.getUpdateCount();
         if (updateCount != -1L) {
            this.handleUpdateCount(updateCount);
         }

         this.outputQueryResult(result);
      }

   }

   protected void outputQueryResult(Result result) {
      if (result.getColumns() != null) {
         LOG.info(new AsciiTable(result.getColumns(), result.getData(), true, "", "No rows returned").render());
      }

   }

   private void handleUpdateCount(long updateCount) {
      LOG.debug(updateCount + (updateCount == 1L ? " row" : " rows") + " affected");
   }

   protected void handleException(Results results, SqlScript sqlScript, SqlStatement sqlStatement) {
      throw new FlywaySqlScriptException(sqlScript.getResource(), sqlStatement, results.getException());
   }

   private void printWarnings(Results results) {
      for(Warning warning : results.getWarnings()) {
         if ("00000".equals(warning.getState())) {
            LOG.info("DB: " + warning.getMessage());
         } else {
            LOG.warn("DB: " + warning.getMessage() + " (SQL State: " + warning.getState() + " - Error Code: " + warning.getCode() + ")");
         }
      }

   }
}
