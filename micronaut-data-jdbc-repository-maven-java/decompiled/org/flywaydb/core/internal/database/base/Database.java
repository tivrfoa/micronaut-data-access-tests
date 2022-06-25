package org.flywaydb.core.internal.database.base;

import java.io.Closeable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.AbbreviationUtils;
import org.flywaydb.core.internal.util.StringUtils;

public abstract class Database<C extends Connection> implements Closeable {
   private static final Log LOG = LogFactory.getLog(Database.class);
   protected final DatabaseType databaseType;
   protected final Configuration configuration;
   protected final StatementInterceptor statementInterceptor;
   protected final JdbcConnectionFactory jdbcConnectionFactory;
   protected final DatabaseMetaData jdbcMetaData;
   protected JdbcTemplate jdbcTemplate;
   private C migrationConnection;
   private C mainConnection;
   protected final java.sql.Connection rawMainJdbcConnection;
   private MigrationVersion version;
   private String installedBy;

   public Database(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      this.databaseType = jdbcConnectionFactory.getDatabaseType();
      this.configuration = configuration;
      this.rawMainJdbcConnection = jdbcConnectionFactory.openConnection();

      try {
         this.jdbcMetaData = this.rawMainJdbcConnection.getMetaData();
      } catch (SQLException var5) {
         throw new FlywaySqlException("Unable to get metadata for connection", var5);
      }

      this.jdbcTemplate = new JdbcTemplate(this.rawMainJdbcConnection, this.databaseType);
      this.jdbcConnectionFactory = jdbcConnectionFactory;
      this.statementInterceptor = statementInterceptor;
   }

   private C getConnection(java.sql.Connection connection) {
      return this.doGetConnection(connection);
   }

   protected abstract C doGetConnection(java.sql.Connection var1);

   public abstract void ensureSupported();

   public final MigrationVersion getVersion() {
      if (this.version == null) {
         this.version = this.determineVersion();
      }

      return this.version;
   }

   protected final void ensureDatabaseIsRecentEnough(String oldestSupportedVersion) {
      if (!this.getVersion().isAtLeast(oldestSupportedVersion)) {
         throw new FlywayDbUpgradeRequiredException(
            this.databaseType,
            this.computeVersionDisplayName(this.getVersion()),
            this.computeVersionDisplayName(MigrationVersion.fromVersion(oldestSupportedVersion))
         );
      }
   }

   protected final void ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition(
      String oldestSupportedVersionInThisEdition, Edition editionWhereStillSupported
   ) {
      if (!this.getVersion().isAtLeast(oldestSupportedVersionInThisEdition)) {
         throw new FlywayEditionUpgradeRequiredException(editionWhereStillSupported, this.databaseType, this.computeVersionDisplayName(this.getVersion()));
      }
   }

   protected final void recommendFlywayUpgradeIfNecessary(String newestSupportedVersion) {
      if (this.getVersion().isNewerThan(newestSupportedVersion)) {
         this.recommendFlywayUpgrade(newestSupportedVersion);
      }

   }

   protected final void recommendFlywayUpgradeIfNecessaryForMajorVersion(String newestSupportedVersion) {
      if (this.getVersion().isMajorNewerThan(newestSupportedVersion)) {
         this.recommendFlywayUpgrade(newestSupportedVersion);
      }

   }

   protected final void notifyDatabaseIsNotFormallySupported() {
      LOG.warn("Support for " + this.databaseType + " is provided only on a community-led basis, and is not formally supported by Redgate");
   }

   private void recommendFlywayUpgrade(String newestSupportedVersion) {
      String message = "Flyway upgrade recommended: "
         + this.databaseType
         + " "
         + this.computeVersionDisplayName(this.getVersion())
         + " is newer than this version of Flyway and support has not been tested. The latest supported version of "
         + this.databaseType
         + " is "
         + newestSupportedVersion
         + ".";
      LOG.warn(message);
   }

   protected String computeVersionDisplayName(MigrationVersion version) {
      return version.getVersion();
   }

   public Delimiter getDefaultDelimiter() {
      return Delimiter.SEMICOLON;
   }

   public final String getCatalog() {
      try {
         return this.doGetCatalog();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Error retrieving the database name", var2);
      }
   }

   protected String doGetCatalog() throws SQLException {
      return this.getMainConnection().getJdbcConnection().getCatalog();
   }

   public final String getCurrentUser() {
      try {
         return this.doGetCurrentUser();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Error retrieving the database user", var2);
      }
   }

   protected String doGetCurrentUser() throws SQLException {
      return this.jdbcMetaData.getUserName();
   }

   public abstract boolean supportsDdlTransactions();

   public abstract boolean supportsChangingCurrentSchema();

   public abstract String getBooleanTrue();

   public abstract String getBooleanFalse();

   public final String quote(String... identifiers) {
      StringBuilder result = new StringBuilder();
      boolean first = true;

      for(String identifier : identifiers) {
         if (!first) {
            result.append(".");
         }

         first = false;
         result.append(this.doQuote(identifier));
      }

      return result.toString();
   }

   public String doQuote(String identifier) {
      return this.getOpenQuote() + identifier + this.getCloseQuote();
   }

   protected String getOpenQuote() {
      return "\"";
   }

   protected String getCloseQuote() {
      return "\"";
   }

   protected String getEscapedQuote() {
      return "";
   }

   public String unQuote(String identifier) {
      String open = this.getOpenQuote();
      String close = this.getCloseQuote();
      if (!open.equals("") && !close.equals("") && identifier.startsWith(open) && identifier.endsWith(close)) {
         identifier = identifier.substring(open.length(), identifier.length() - close.length());
         if (!this.getEscapedQuote().equals("")) {
            identifier = StringUtils.replaceAll(identifier, this.getEscapedQuote(), close);
         }
      }

      return identifier;
   }

   public abstract boolean catalogIsSchema();

   public boolean useSingleConnection() {
      return false;
   }

   public DatabaseMetaData getJdbcMetaData() {
      return this.jdbcMetaData;
   }

   public final C getMainConnection() {
      if (this.mainConnection == null) {
         this.mainConnection = this.getConnection(this.rawMainJdbcConnection);
      }

      return this.mainConnection;
   }

   public final C getMigrationConnection() {
      if (this.migrationConnection == null) {
         if (this.useSingleConnection()) {
            this.migrationConnection = this.getMainConnection();
         } else {
            this.migrationConnection = this.getConnection(this.jdbcConnectionFactory.openConnection());
         }
      }

      return this.migrationConnection;
   }

   protected MigrationVersion determineVersion() {
      try {
         return MigrationVersion.fromVersion(this.jdbcMetaData.getDatabaseMajorVersion() + "." + this.jdbcMetaData.getDatabaseMinorVersion());
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to determine the major version of the database", var2);
      }
   }

   public final SqlScript getCreateScript(SqlScriptFactory sqlScriptFactory, Table table, boolean baseline) {
      return sqlScriptFactory.createSqlScript(new StringResource(this.getRawCreateScript(table, baseline)), false, null);
   }

   public abstract String getRawCreateScript(Table var1, boolean var2);

   public String getInsertStatement(Table table) {
      return "INSERT INTO "
         + table
         + " ("
         + this.quote("installed_rank")
         + ", "
         + this.quote("version")
         + ", "
         + this.quote("description")
         + ", "
         + this.quote("type")
         + ", "
         + this.quote("script")
         + ", "
         + this.quote("checksum")
         + ", "
         + this.quote("installed_by")
         + ", "
         + this.quote("execution_time")
         + ", "
         + this.quote("success")
         + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
   }

   public final String getBaselineStatement(Table table) {
      return String.format(
         this.getInsertStatement(table).replace("?", "%s"),
         1,
         "'" + this.configuration.getBaselineVersion() + "'",
         "'" + AbbreviationUtils.abbreviateDescription(this.configuration.getBaselineDescription()) + "'",
         "'" + MigrationType.BASELINE + "'",
         "'" + AbbreviationUtils.abbreviateScript(this.configuration.getBaselineDescription()) + "'",
         "NULL",
         "'" + this.installedBy + "'",
         0,
         this.getBooleanTrue()
      );
   }

   public String getSelectStatement(Table table) {
      return "SELECT "
         + this.quote("installed_rank")
         + ","
         + this.quote("version")
         + ","
         + this.quote("description")
         + ","
         + this.quote("type")
         + ","
         + this.quote("script")
         + ","
         + this.quote("checksum")
         + ","
         + this.quote("installed_on")
         + ","
         + this.quote("installed_by")
         + ","
         + this.quote("execution_time")
         + ","
         + this.quote("success")
         + " FROM "
         + table
         + " WHERE "
         + this.quote("installed_rank")
         + " > ? ORDER BY "
         + this.quote("installed_rank");
   }

   public final String getInstalledBy() {
      if (this.installedBy == null) {
         this.installedBy = this.configuration.getInstalledBy() == null ? this.getCurrentUser() : this.configuration.getInstalledBy();
      }

      return this.installedBy;
   }

   public void close() {
      if (!this.useSingleConnection() && this.migrationConnection != null) {
         this.migrationConnection.close();
      }

      if (this.mainConnection != null) {
         this.mainConnection.close();
      }

   }

   public DatabaseType getDatabaseType() {
      return this.databaseType;
   }

   public boolean supportsEmptyMigrationDescription() {
      return true;
   }

   public boolean supportsMultiStatementTransactions() {
      return true;
   }

   public void cleanPreSchemas() {
      try {
         this.doCleanPreSchemas();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to clean database " + this, var2);
      }
   }

   protected void doCleanPreSchemas() throws SQLException {
   }

   public void cleanPostSchemas(Schema[] schemas) {
      try {
         this.doCleanPostSchemas(schemas);
      } catch (SQLException var3) {
         throw new FlywaySqlException("Unable to clean schema " + this, var3);
      }
   }

   protected void doCleanPostSchemas(Schema[] schemas) throws SQLException {
   }

   public Schema[] getAllSchemas() {
      throw new UnsupportedOperationException("Getting all schemas not supported for " + this.getDatabaseType().getName());
   }

   public Configuration getConfiguration() {
      return this.configuration;
   }
}
