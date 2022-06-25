package org.flywaydb.core.internal.database.cockroachdb;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

public class CockroachDBDatabase extends Database<CockroachDBConnection> {
   private final MigrationVersion determinedVersion = this.rawDetermineVersion();

   public CockroachDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      super(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   protected CockroachDBConnection doGetConnection(Connection connection) {
      return new CockroachDBConnection(this, connection);
   }

   @Override
   public final void ensureSupported() {
      this.ensureDatabaseIsRecentEnough("1.1");
      this.recommendFlywayUpgradeIfNecessary("21.1");
   }

   @Override
   public String getRawCreateScript(Table table, boolean baseline) {
      return "CREATE TABLE IF NOT EXISTS "
         + table
         + " (\n    \"installed_rank\" INT NOT NULL PRIMARY KEY,\n    \"version\" VARCHAR(50),\n    \"description\" VARCHAR(200) NOT NULL,\n    \"type\" VARCHAR(20) NOT NULL,\n    \"script\" VARCHAR(1000) NOT NULL,\n    \"checksum\" INTEGER,\n    \"installed_by\" VARCHAR(100) NOT NULL,\n    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now(),\n    \"execution_time\" INTEGER NOT NULL,\n    \"success\" BOOLEAN NOT NULL\n);\n"
         + (baseline ? this.getBaselineStatement(table) + ";\n" : "")
         + "CREATE INDEX IF NOT EXISTS \""
         + table.getName()
         + "_s_idx\" ON "
         + table
         + " (\"success\");";
   }

   private MigrationVersion rawDetermineVersion() {
      String version;
      try {
         JdbcTemplate template = new JdbcTemplate(this.rawMainJdbcConnection);
         version = template.queryForString("SELECT value FROM crdb_internal.node_build_info where field='Version'");
         if (version == null) {
            version = template.queryForString("SELECT value FROM crdb_internal.node_build_info where field='Tag'");
         }
      } catch (SQLException var6) {
         throw new FlywaySqlException("Unable to determine CockroachDB version", var6);
      }

      int firstDot = version.indexOf(".");
      int majorVersion = Integer.parseInt(version.substring(1, firstDot));
      String minorPatch = version.substring(firstDot + 1);
      int minorVersion = Integer.parseInt(minorPatch.substring(0, minorPatch.indexOf(".")));
      return MigrationVersion.fromVersion(majorVersion + "." + minorVersion);
   }

   @Override
   protected MigrationVersion determineVersion() {
      return this.determinedVersion;
   }

   boolean supportsSchemas() {
      return this.getVersion().isAtLeast("20.2");
   }

   @Override
   protected String doGetCurrentUser() throws SQLException {
      return this.getMainConnection().getJdbcTemplate().queryForString("SELECT * FROM [SHOW SESSION_USER]");
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
