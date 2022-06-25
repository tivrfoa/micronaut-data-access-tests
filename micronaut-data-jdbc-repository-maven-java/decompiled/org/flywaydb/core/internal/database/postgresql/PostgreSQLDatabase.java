package org.flywaydb.core.internal.database.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.util.StringUtils;

public class PostgreSQLDatabase extends Database<PostgreSQLConnection> {
   public PostgreSQLDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected PostgreSQLConnection doGetConnection(Connection connection) {
      return new PostgreSQLConnection(this, connection);
   }

   @Override
   public void ensureSupported() {
      this.ensureDatabaseIsRecentEnough("9.0");
      this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10", Edition.ENTERPRISE);
      this.recommendFlywayUpgradeIfNecessaryForMajorVersion("14");
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      String tablespace = this.configuration.getTablespace() == null ? "" : " TABLESPACE \"" + this.configuration.getTablespace() + "\"";
      return "CREATE TABLE "
         + table
         + " (\n    \"installed_rank\" INT NOT NULL,\n    \"version\" VARCHAR(50),\n    \"description\" VARCHAR(200) NOT NULL,\n    \"type\" VARCHAR(20) NOT NULL,\n    \"script\" VARCHAR(1000) NOT NULL,\n    \"checksum\" INTEGER,\n    \"installed_by\" VARCHAR(100) NOT NULL,\n    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now(),\n    \"execution_time\" INTEGER NOT NULL,\n    \"success\" BOOLEAN NOT NULL\n)"
         + tablespace
         + ";\n"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "")
         + "ALTER TABLE "
         + table
         + " ADD CONSTRAINT \""
         + table.getName()
         + "_pk\" PRIMARY KEY (\"installed_rank\");\nCREATE INDEX \""
         + table.getName()
         + "_s_idx\" ON "
         + table
         + " (\"success\");";
   }

   @Override
   protected String doGetCurrentUser() throws SQLException {
      return this.getMainConnection().getJdbcTemplate().queryForString("SELECT current_user");
   }

   @Override
   public boolean supportsDdlTransactions() {
      return true;
   }

   @Override
   public boolean supportsChangingCurrentSchema() {
      return true;
   }

   @Override
   public String getBooleanTrue() {
      return "TRUE";
   }

   @Override
   public String getBooleanFalse() {
      return "FALSE";
   }

   @Override
   public String doQuote(String identifier) {
      return this.getOpenQuote() + StringUtils.replaceAll(identifier, this.getCloseQuote(), this.getEscapedQuote()) + this.getCloseQuote();
   }

   @Override
   public String getEscapedQuote() {
      return "\"\"";
   }

   @Override
   public boolean catalogIsSchema() {
      return false;
   }

   @Override
   public boolean useSingleConnection() {
      return true;
   }

   @Override
   public String getSelectStatement(Table table) {
      return "/*NO LOAD BALANCE*/\nSELECT "
         + this.quote(new String[]{"installed_rank"})
         + ","
         + this.quote(new String[]{"version"})
         + ","
         + this.quote(new String[]{"description"})
         + ","
         + this.quote(new String[]{"type"})
         + ","
         + this.quote(new String[]{"script"})
         + ","
         + this.quote(new String[]{"checksum"})
         + ","
         + this.quote(new String[]{"installed_on"})
         + ","
         + this.quote(new String[]{"installed_by"})
         + ","
         + this.quote(new String[]{"execution_time"})
         + ","
         + this.quote(new String[]{"success"})
         + " FROM "
         + table
         + " WHERE "
         + this.quote(new String[]{"installed_rank"})
         + " > ? ORDER BY "
         + this.quote(new String[]{"installed_rank"});
   }
}
