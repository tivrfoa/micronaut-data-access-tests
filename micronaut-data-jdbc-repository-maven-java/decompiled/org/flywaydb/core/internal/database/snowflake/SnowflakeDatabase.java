package org.flywaydb.core.internal.database.snowflake;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;

public class SnowflakeDatabase extends Database<SnowflakeConnection> {
   private static final Log LOG = LogFactory.getLog(SnowflakeDatabase.class);
   private final boolean quotedIdentifiersIgnoreCase = getQuotedIdentifiersIgnoreCase(this.jdbcTemplate);

   public SnowflakeDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
      LOG.info("QUOTED_IDENTIFIERS_IGNORE_CASE option is " + this.quotedIdentifiersIgnoreCase);
   }

   private static boolean getQuotedIdentifiersIgnoreCase(JdbcTemplate jdbcTemplate) {
      try {
         List<Map<String, String>> result = jdbcTemplate.queryForList("SHOW PARAMETERS LIKE 'QUOTED_IDENTIFIERS_IGNORE_CASE'");
         Map<String, String> row = (Map)result.get(0);
         return "TRUE".equals(((String)row.get("value")).toUpperCase(Locale.ENGLISH));
      } catch (SQLException var3) {
         LOG.warn("Could not query for parameter QUOTED_IDENTIFIERS_IGNORE_CASE.");
         return false;
      }
   }

   protected SnowflakeConnection doGetConnection(Connection connection) {
      return new SnowflakeConnection(this, connection);
   }

   @Override
   public void ensureSupported() {
      this.ensureDatabaseIsRecentEnough("3.0");
      this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("3", Edition.ENTERPRISE);
      this.recommendFlywayUpgradeIfNecessaryForMajorVersion("5.1");
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      return "CREATE TABLE "
         + table
         + " (\n"
         + this.quote(new String[]{"installed_rank"})
         + " NUMBER(38,0) NOT NULL,\n"
         + this.quote(new String[]{"version"})
         + " VARCHAR(50),\n"
         + this.quote(new String[]{"description"})
         + " VARCHAR(200),\n"
         + this.quote(new String[]{"type"})
         + " VARCHAR(20) NOT NULL,\n"
         + this.quote(new String[]{"script"})
         + " VARCHAR(1000) NOT NULL,\n"
         + this.quote(new String[]{"checksum"})
         + " NUMBER(38,0),\n"
         + this.quote(new String[]{"installed_by"})
         + " VARCHAR(100) NOT NULL,\n"
         + this.quote(new String[]{"installed_on"})
         + " TIMESTAMP_LTZ(9) NOT NULL DEFAULT CURRENT_TIMESTAMP(),\n"
         + this.quote(new String[]{"execution_time"})
         + " NUMBER(38,0) NOT NULL,\n"
         + this.quote(new String[]{"success"})
         + " BOOLEAN NOT NULL,\nprimary key ("
         + this.quote(new String[]{"installed_rank"})
         + "));\n"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "");
   }

   @Override
   public String getSelectStatement(Table table) {
      return "SELECT "
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

   @Override
   public String getInsertStatement(Table table) {
      return "INSERT INTO "
         + table
         + " ("
         + this.quote(new String[]{"installed_rank"})
         + ", "
         + this.quote(new String[]{"version"})
         + ", "
         + this.quote(new String[]{"description"})
         + ", "
         + this.quote(new String[]{"type"})
         + ", "
         + this.quote(new String[]{"script"})
         + ", "
         + this.quote(new String[]{"checksum"})
         + ", "
         + this.quote(new String[]{"installed_by"})
         + ", "
         + this.quote(new String[]{"execution_time"})
         + ", "
         + this.quote(new String[]{"success"})
         + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
      return "true";
   }

   @Override
   public String getBooleanFalse() {
      return "false";
   }

   @Override
   public boolean catalogIsSchema() {
      return false;
   }
}
