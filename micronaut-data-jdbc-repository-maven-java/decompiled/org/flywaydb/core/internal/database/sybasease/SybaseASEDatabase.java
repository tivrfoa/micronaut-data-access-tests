package org.flywaydb.core.internal.database.sybasease;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.sqlscript.Delimiter;

public class SybaseASEDatabase extends Database<SybaseASEConnection> {
   private static final Log LOG = LogFactory.getLog(SybaseASEDatabase.class);
   private String databaseName = null;
   private boolean supportsMultiStatementTransactions = false;

   public SybaseASEDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected SybaseASEConnection doGetConnection(Connection connection) {
      return new SybaseASEConnection(this, connection);
   }

   @Override
   public void ensureSupported() {
      this.ensureDatabaseIsRecentEnough("15.7");
      this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("16.3", Edition.ENTERPRISE);
      this.recommendFlywayUpgradeIfNecessary("16.3");
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      return "CREATE TABLE "
         + table.getName()
         + " (\n    installed_rank INT NOT NULL,\n    version VARCHAR(50) NULL,\n    description VARCHAR(200) NOT NULL,\n    type VARCHAR(20) NOT NULL,\n    script VARCHAR(1000) NOT NULL,\n    checksum INT NULL,\n    installed_by VARCHAR(100) NOT NULL,\n    installed_on datetime DEFAULT getDate() NOT NULL,\n    execution_time INT NOT NULL,\n    success decimal NOT NULL,\n    PRIMARY KEY (installed_rank)\n)\nlock datarows on 'default'\n"
         + (baseline ? this.getBaselineStatement(table) + "\n" : "")
         + "go\nCREATE INDEX "
         + table.getName()
         + "_s_idx ON "
         + table.getName()
         + " (success)\ngo\n";
   }

   @Override
   public boolean supportsEmptyMigrationDescription() {
      return false;
   }

   @Override
   public Delimiter getDefaultDelimiter() {
      return Delimiter.GO;
   }

   @Override
   protected String doGetCurrentUser() throws SQLException {
      return this.getMainConnection().getJdbcTemplate().queryForString("SELECT user_name()");
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
   public String getOpenQuote() {
      return "";
   }

   @Override
   public String getCloseQuote() {
      return "";
   }

   @Override
   public boolean catalogIsSchema() {
      return false;
   }

   @Override
   public boolean supportsMultiStatementTransactions() {
      if (this.supportsMultiStatementTransactions) {
         LOG.debug("ddl in tran was found to be true at some point during execution.Therefore multi statement transaction support is assumed.");
         return true;
      } else {
         boolean ddlInTran = this.getDdlInTranOption();
         if (ddlInTran) {
            LOG.debug("ddl in tran is true. Multi statement transaction support is now assumed.");
            this.supportsMultiStatementTransactions = true;
         }

         return this.supportsMultiStatementTransactions;
      }
   }

   boolean getDdlInTranOption() {
      try {
         String databaseName = this.getDatabaseName();
         String getDatabaseMetadataQuery = "sp_helpdb " + databaseName + " -- ";
         Results results = this.getMainConnection().getJdbcTemplate().executeStatement(getDatabaseMetadataQuery);

         for(int resultsIndex = 0; resultsIndex < results.getResults().size(); ++resultsIndex) {
            List<String> columns = ((Result)results.getResults().get(resultsIndex)).getColumns();
            if (columns != null) {
               int statusIndex = this.getStatusIndex(columns);
               if (statusIndex > -1) {
                  String options = (String)((List)((Result)results.getResults().get(resultsIndex)).getData().get(0)).get(statusIndex);
                  return options.contains("ddl in tran");
               }
            }
         }

         return false;
      } catch (Exception var8) {
         throw new FlywayException(var8);
      }
   }

   private int getStatusIndex(List<String> columns) {
      for(int statusIndex = 0; statusIndex < columns.size(); ++statusIndex) {
         if ("status".equals(columns.get(statusIndex))) {
            return statusIndex;
         }
      }

      return -1;
   }

   String getDatabaseName() throws SQLException {
      if (this.databaseName == null) {
         this.databaseName = this.getMainConnection().getJdbcTemplate().queryForString("select db_name()");
      }

      return this.databaseName;
   }
}
