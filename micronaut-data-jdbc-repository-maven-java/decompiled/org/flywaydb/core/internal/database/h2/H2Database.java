package org.flywaydb.core.internal.database.h2;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;

public class H2Database extends Database<H2Connection> {
   private static final String DEFAULT_USER = "<< default user >>";
   private static final String DUMMY_SCRIPT_NAME = "<< history table creation script >>";
   boolean supportsDropSchemaCascade;
   private boolean requiresV2MetadataColumnNames = super.determineVersion().isAtLeast("2.0.0");
   H2Database.CompatibilityMode compatibilityMode = this.determineCompatibilityMode();

   public H2Database(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected H2Connection doGetConnection(Connection connection) {
      return new H2Connection(this, connection, this.requiresV2MetadataColumnNames);
   }

   @Override
   protected MigrationVersion determineVersion() {
      String query = this.requiresV2MetadataColumnNames
         ? "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'info.BUILD_ID'"
         : "SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'info.BUILD_ID'";

      try {
         int buildId = this.getMainConnection().getJdbcTemplate().queryForInt(query);
         return MigrationVersion.fromVersion(super.determineVersion().getVersion() + "." + buildId);
      } catch (SQLException var3) {
         throw new FlywaySqlException("Unable to determine H2 build ID", var3);
      }
   }

   private H2Database.CompatibilityMode determineCompatibilityMode() {
      String query = this.requiresV2MetadataColumnNames
         ? "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MODE'"
         : "SELECT VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'MODE'";

      try {
         String mode = this.getMainConnection().getJdbcTemplate().queryForString(query);
         return mode != null && !"".equals(mode) ? H2Database.CompatibilityMode.valueOf(mode) : H2Database.CompatibilityMode.REGULAR;
      } catch (SQLException var3) {
         throw new FlywaySqlException("Unable to determine H2 compatibility mode", var3);
      }
   }

   @Override
   public final void ensureSupported() {
      this.ensureDatabaseIsRecentEnough("1.2.137");
      this.ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("1.4", Edition.ENTERPRISE);
      this.recommendFlywayUpgradeIfNecessary("2.1.210");
      this.supportsDropSchemaCascade = this.getVersion().isAtLeast("1.4.200");
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      String script = this.compatibilityMode == H2Database.CompatibilityMode.Oracle ? "<< history table creation script >>" : "";
      return "CREATE TABLE IF NOT EXISTS "
         + table
         + " (\n    \"installed_rank\" INT NOT NULL,\n    \"version\" VARCHAR(50),\n    \"description\" VARCHAR(200) NOT NULL,\n    \"type\" VARCHAR(20) NOT NULL,\n    \"script\" VARCHAR(1000) NOT NULL,\n    \"checksum\" INT,\n    \"installed_by\" VARCHAR(100) NOT NULL,\n    \"installed_on\" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n    \"execution_time\" INT NOT NULL,\n    \"success\" BOOLEAN NOT NULL,\n    CONSTRAINT \""
         + table.getName()
         + "_pk\" PRIMARY KEY (\"installed_rank\")\n) AS SELECT -1, NULL, '<< Flyway Schema History table created >>', 'TABLE', '"
         + script
         + "', NULL, '"
         + this.getInstalledBy()
         + "', CURRENT_TIMESTAMP, 0, TRUE;\n"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "")
         + "CREATE INDEX \""
         + table.getSchema().getName()
         + "\".\""
         + table.getName()
         + "_s_idx\" ON "
         + table
         + " (\"success\");";
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
         + this.quote(new String[]{"type"})
         + " != 'TABLE' AND "
         + this.quote(new String[]{"installed_rank"})
         + " > ? ORDER BY "
         + this.quote(new String[]{"installed_rank"});
   }

   @Override
   protected String doGetCurrentUser() throws SQLException {
      try {
         String user = this.getMainConnection().getJdbcTemplate().queryForString("SELECT USER()");
         if (this.compatibilityMode == H2Database.CompatibilityMode.Oracle && (user == null || "".equals(user))) {
            user = "<< default user >>";
         }

         return user;
      } catch (Exception var2) {
         if (this.compatibilityMode == H2Database.CompatibilityMode.Oracle) {
            return "<< default user >>";
         } else {
            throw var2;
         }
      }
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
      return this.requiresV2MetadataColumnNames ? "FALSE" : "0";
   }

   @Override
   public boolean catalogIsSchema() {
      return false;
   }

   private static enum CompatibilityMode {
      REGULAR,
      STRICT,
      LEGACY,
      DB2,
      Derby,
      HSQLDB,
      MSSQLServer,
      MySQL,
      Oracle,
      PostgreSQL,
      Ignite;
   }
}
