package org.flywaydb.database.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.database.mysql.mariadb.MariaDBDatabaseType;

public class MySQLDatabase extends Database<MySQLConnection> {
   private static final Log LOG = LogFactory.getLog(MySQLDatabase.class);
   private static final Pattern MARIADB_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+(-\\d+)*-MariaDB(-\\w+)*");
   private static final Pattern MARIADB_WITH_MAXSCALE_VERSION_PATTERN = Pattern.compile(
      "(\\d+\\.\\d+)\\.\\d+(-\\d+)* (\\d+\\.\\d+)\\.\\d+(-\\d+)*-maxscale(-\\w+)*"
   );
   private static final Pattern MYSQL_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+)\\.\\d+\\w*");
   private final boolean pxcStrict;
   private final boolean gtidConsistencyEnforced;
   final boolean eventSchedulerQueryable;

   public MySQLDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
      JdbcTemplate jdbcTemplate = new JdbcTemplate(this.rawMainJdbcConnection, this.databaseType);
      this.pxcStrict = this.isMySQL() && isRunningInPerconaXtraDBClusterWithStrictMode(jdbcTemplate);
      this.gtidConsistencyEnforced = this.isMySQL() && isRunningInGTIDConsistencyMode(jdbcTemplate);
      this.eventSchedulerQueryable = this.isMySQL() || isEventSchedulerQueryable(jdbcTemplate);
   }

   private static boolean isEventSchedulerQueryable(JdbcTemplate jdbcTemplate) {
      try {
         jdbcTemplate.queryForString("SELECT event_name FROM information_schema.events LIMIT 1");
         return true;
      } catch (SQLException var2) {
         LOG.debug("Detected unqueryable MariaDB event scheduler, most likely due to it being OFF or DISABLED.");
         return false;
      }
   }

   static boolean isRunningInPerconaXtraDBClusterWithStrictMode(JdbcTemplate jdbcTemplate) {
      try {
         String pcx_strict_mode = jdbcTemplate.queryForString(
            "select VARIABLE_VALUE from performance_schema.global_variables where variable_name = 'pxc_strict_mode'"
         );
         if ("ENFORCING".equals(pcx_strict_mode) || "MASTER".equals(pcx_strict_mode)) {
            LOG.debug("Detected Percona XtraDB Cluster in strict mode");
            return true;
         }
      } catch (SQLException var2) {
         LOG.debug("Unable to detect whether we are running in a Percona XtraDB Cluster. Assuming not to be.");
      }

      return false;
   }

   static boolean isRunningInGTIDConsistencyMode(JdbcTemplate jdbcTemplate) {
      try {
         String gtidConsistency = jdbcTemplate.queryForString("SELECT @@GLOBAL.ENFORCE_GTID_CONSISTENCY");
         if ("ON".equals(gtidConsistency)) {
            LOG.debug("Detected GTID consistency being enforced");
            return true;
         }
      } catch (SQLException var2) {
         LOG.debug("Unable to detect whether database enforces GTID consistency. Assuming not.");
      }

      return false;
   }

   boolean isMySQL() {
      return this.databaseType instanceof MySQLDatabaseType;
   }

   boolean isMariaDB() {
      return this.databaseType instanceof MariaDBDatabaseType;
   }

   boolean isPxcStrict() {
      return this.pxcStrict;
   }

   protected boolean isCreateTableAsSelectAllowed() {
      return !this.pxcStrict && !this.gtidConsistencyEnforced;
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      String tablespace = this.configuration.getTablespace() == null ? "" : " TABLESPACE \"" + this.configuration.getTablespace() + "\"";
      String baselineMarker = "";
      if (baseline) {
         if (this.isCreateTableAsSelectAllowed()) {
            baselineMarker = " AS SELECT     1 as \"installed_rank\",     '"
               + this.configuration.getBaselineVersion()
               + "' as \"version\",     '"
               + this.configuration.getBaselineDescription()
               + "' as \"description\",     '"
               + MigrationType.BASELINE
               + "' as \"type\",     '"
               + this.configuration.getBaselineDescription()
               + "' as \"script\",     NULL as \"checksum\",     '"
               + this.getInstalledBy()
               + "' as \"installed_by\",     CURRENT_TIMESTAMP as \"installed_on\",     0 as \"execution_time\",     TRUE as \"success\"\n";
         } else {
            baselineMarker = ";\n" + this.getBaselineStatement(table);
         }
      }

      return "CREATE TABLE "
         + table
         + " (\n    `installed_rank` INT NOT NULL,\n    `version` VARCHAR(50),\n    `description` VARCHAR(200) NOT NULL,\n    `type` VARCHAR(20) NOT NULL,\n    `script` VARCHAR(1000) NOT NULL,\n    `checksum` INT,\n    `installed_by` VARCHAR(100) NOT NULL,\n    `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n    `execution_time` INT NOT NULL,\n    `success` BOOL NOT NULL,\n    CONSTRAINT "
         + this.getConstraintName(table.getName())
         + " PRIMARY KEY (`installed_rank`)\n)"
         + tablespace
         + " ENGINE=InnoDB"
         + baselineMarker
         + ";\nCREATE INDEX `"
         + table.getName()
         + "_s_idx` ON "
         + table
         + " (`success`);";
   }

   protected String getConstraintName(String tableName) {
      return "`" + tableName + "_pk`";
   }

   protected MySQLConnection doGetConnection(Connection connection) {
      return new MySQLConnection(this, connection);
   }

   @Override
   protected MigrationVersion determineVersion() {
      String selectVersionOutput = BaseDatabaseType.getSelectVersionOutput(this.rawMainJdbcConnection);
      return this.databaseType instanceof MariaDBDatabaseType
         ? extractMariaDBVersionFromString(selectVersionOutput)
         : extractMySQLVersionFromString(selectVersionOutput);
   }

   static MigrationVersion extractMySQLVersionFromString(String selectVersionOutput) {
      return extractVersionFromString(selectVersionOutput, MYSQL_VERSION_PATTERN);
   }

   static MigrationVersion extractMariaDBVersionFromString(String selectVersionOutput) {
      return extractVersionFromString(selectVersionOutput, MARIADB_VERSION_PATTERN, MARIADB_WITH_MAXSCALE_VERSION_PATTERN);
   }

   private static MigrationVersion extractVersionFromString(String versionString, Pattern... patterns) {
      for(Pattern pattern : patterns) {
         Matcher matcher = pattern.matcher(versionString);
         if (matcher.find()) {
            return MigrationVersion.fromVersion(matcher.group(1));
         }
      }

      throw new FlywayException("Unable to determine version from '" + versionString + "'");
   }

   @Override
   public final void ensureSupported() {
      if (this.databaseType.getName().equals("TiDB")) {
         this.ensureDatabaseIsRecentEnough("5.0");
         this.recommendFlywayUpgradeIfNecessary("5.0");
      } else {
         this.ensureDatabaseIsRecentEnough("5.1");
         if (this.databaseType instanceof MariaDBDatabaseType) {
            this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("10.2", Edition.ENTERPRISE);
            this.recommendFlywayUpgradeIfNecessary("10.6");
         } else {
            this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("8.0", Edition.ENTERPRISE);
            this.recommendFlywayUpgradeIfNecessary("8.0");
         }

      }
   }

   @Override
   protected String doGetCurrentUser() throws SQLException {
      return this.getMainConnection().getJdbcTemplate().queryForString("SELECT SUBSTRING_INDEX(USER(),'@',1)");
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
      return "`";
   }

   @Override
   public String getCloseQuote() {
      return "`";
   }

   @Override
   public boolean catalogIsSchema() {
      return true;
   }

   @Override
   public boolean useSingleConnection() {
      return !this.pxcStrict;
   }
}
