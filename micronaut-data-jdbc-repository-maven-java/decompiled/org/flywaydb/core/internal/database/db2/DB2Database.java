package org.flywaydb.core.internal.database.db2;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;

public class DB2Database extends Database<DB2Connection> {
   public DB2Database(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected DB2Connection doGetConnection(Connection connection) {
      return new DB2Connection(this, connection);
   }

   @Override
   public final void ensureSupported() {
      this.ensureDatabaseIsRecentEnough("9.7");
      this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("11.5", Edition.ENTERPRISE);
      this.recommendFlywayUpgradeIfNecessary("11.5");
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      String tablespace = this.configuration.getTablespace() == null ? "" : " IN \"" + this.configuration.getTablespace() + "\"";
      return "CREATE TABLE "
         + table
         + " (\n    \"installed_rank\" INT NOT NULL,\n    \"version\" VARCHAR(50),\n    \"description\" VARCHAR(200) NOT NULL,\n    \"type\" VARCHAR(20) NOT NULL,\n    \"script\" VARCHAR(1000) NOT NULL,\n    \"checksum\" INT,\n    \"installed_by\" VARCHAR(100) NOT NULL,\n    \"installed_on\" TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,\n    \"execution_time\" INT NOT NULL,\n    \"success\" SMALLINT NOT NULL,\n    CONSTRAINT \""
         + table.getName()
         + "_s\" CHECK (\"success\" in(0,1))\n) ORGANIZE BY ROW"
         + tablespace
         + ";\nALTER TABLE "
         + table
         + " ADD CONSTRAINT \""
         + table.getName()
         + "_pk\" PRIMARY KEY (\"installed_rank\");\nCREATE INDEX \""
         + table.getSchema().getName()
         + "\".\""
         + table.getName()
         + "_s_idx\" ON "
         + table
         + " (\"success\");"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "");
   }

   @Override
   public String getSelectStatement(Table table) {
      return super.getSelectStatement(table) + " WITH UR";
   }

   @Override
   protected String doGetCurrentUser() throws SQLException {
      return this.getMainConnection().getJdbcTemplate().queryForString("select CURRENT_USER from sysibm.sysdummy1");
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
      return "1";
   }

   @Override
   public String getBooleanFalse() {
      return "0";
   }

   @Override
   public boolean catalogIsSchema() {
      return false;
   }

   @Override
   public boolean useSingleConnection() {
      return false;
   }
}
