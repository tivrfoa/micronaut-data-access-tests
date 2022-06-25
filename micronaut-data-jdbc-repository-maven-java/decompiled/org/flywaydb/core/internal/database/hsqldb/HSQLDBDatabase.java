package org.flywaydb.core.internal.database.hsqldb;

import java.sql.Connection;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;

public class HSQLDBDatabase extends Database<HSQLDBConnection> {
   public HSQLDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected HSQLDBConnection doGetConnection(Connection connection) {
      return new HSQLDBConnection(this, connection);
   }

   @Override
   public final void ensureSupported() {
      this.ensureDatabaseIsRecentEnough("1.8");
      this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("2.4", Edition.ENTERPRISE);
      this.recommendFlywayUpgradeIfNecessary("2.6");
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      return "CREATE TABLE "
         + table
         + " (\n    \"installed_rank\" INT NOT NULL,\n    \"version\" VARCHAR(50),\n    \"description\" VARCHAR(200) NOT NULL,\n    \"type\" VARCHAR(20) NOT NULL,\n    \"script\" VARCHAR(1000) NOT NULL,\n    \"checksum\" INT,\n    \"installed_by\" VARCHAR(100) NOT NULL,\n    \"installed_on\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n    \"execution_time\" INT NOT NULL,\n    \"success\" BIT NOT NULL\n);\n"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "")
         + "ALTER TABLE "
         + table
         + " ADD CONSTRAINT \""
         + table.getName()
         + "_pk\" PRIMARY KEY (\"installed_rank\");\nCREATE INDEX \""
         + table.getSchema().getName()
         + "\".\""
         + table.getName()
         + "_s_idx\" ON "
         + table
         + " (\"success\");";
   }

   @Override
   public boolean supportsDdlTransactions() {
      return false;
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
      return true;
   }
}
