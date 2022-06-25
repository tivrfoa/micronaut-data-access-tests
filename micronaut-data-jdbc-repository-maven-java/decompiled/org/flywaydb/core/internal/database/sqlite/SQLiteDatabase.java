package org.flywaydb.core.internal.database.sqlite;

import java.sql.Connection;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

public class SQLiteDatabase extends Database<SQLiteConnection> {
   public SQLiteDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected SQLiteConnection doGetConnection(Connection connection) {
      return new SQLiteConnection(this, connection);
   }

   @Override
   public final void ensureSupported() {
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      return "CREATE TABLE "
         + table
         + " (\n    \"installed_rank\" INT NOT NULL PRIMARY KEY,\n    \"version\" VARCHAR(50),\n    \"description\" VARCHAR(200) NOT NULL,\n    \"type\" VARCHAR(20) NOT NULL,\n    \"script\" VARCHAR(1000) NOT NULL,\n    \"checksum\" INT,\n    \"installed_by\" VARCHAR(100) NOT NULL,\n    \"installed_on\" TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%f','now')),\n    \"execution_time\" INT NOT NULL,\n    \"success\" BOOLEAN NOT NULL\n);\n"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "")
         + "CREATE INDEX \""
         + table.getSchema().getName()
         + "\".\""
         + table.getName()
         + "_s_idx\" ON \""
         + table.getName()
         + "\" (\"success\");";
   }

   @Override
   protected String doGetCurrentUser() {
      return "";
   }

   @Override
   public boolean supportsDdlTransactions() {
      return true;
   }

   @Override
   public boolean supportsChangingCurrentSchema() {
      return false;
   }

   @Override
   public String getBooleanTrue() {
      return "1";
   }

   @Override
   public String getBooleanFalse() {
      return "0";
   }

   @Override
   public boolean catalogIsSchema() {
      return true;
   }

   @Override
   public boolean useSingleConnection() {
      return true;
   }
}
