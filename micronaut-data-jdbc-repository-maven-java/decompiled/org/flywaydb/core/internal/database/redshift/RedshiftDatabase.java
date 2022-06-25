package org.flywaydb.core.internal.database.redshift;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

public class RedshiftDatabase extends Database<RedshiftConnection> {
   public RedshiftDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected RedshiftConnection doGetConnection(Connection connection) {
      return new RedshiftConnection(this, connection);
   }

   @Override
   public final void ensureSupported() {
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      return "CREATE TABLE "
         + table
         + " (\n    \"installed_rank\" INT NOT NULL SORTKEY,\n    \"version\" VARCHAR(50),\n    \"description\" VARCHAR(200) NOT NULL,\n    \"type\" VARCHAR(20) NOT NULL,\n    \"script\" VARCHAR(1000) NOT NULL,\n    \"checksum\" INTEGER,\n    \"installed_by\" VARCHAR(100) NOT NULL,\n    \"installed_on\" TIMESTAMP NOT NULL DEFAULT getdate(),\n    \"execution_time\" INTEGER NOT NULL,\n    \"success\" BOOLEAN NOT NULL\n);\n"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "")
         + "ALTER TABLE "
         + table
         + " ADD CONSTRAINT \""
         + table.getName()
         + "_pk\" PRIMARY KEY (\"installed_rank\");";
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
      return false;
   }
}
