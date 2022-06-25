package org.flywaydb.core.api.configuration;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;

public interface Configuration {
   ClassLoader getClassLoader();

   String getUrl();

   String getUser();

   String getPassword();

   DataSource getDataSource();

   int getConnectRetries();

   int getConnectRetriesInterval();

   String getInitSql();

   MigrationVersion getBaselineVersion();

   String getBaselineDescription();

   MigrationResolver[] getResolvers();

   boolean isSkipDefaultResolvers();

   Callback[] getCallbacks();

   boolean isSkipDefaultCallbacks();

   String getSqlMigrationPrefix();

   String getBaselineMigrationPrefix();

   String getUndoSqlMigrationPrefix();

   String getRepeatableSqlMigrationPrefix();

   String getSqlMigrationSeparator();

   String[] getSqlMigrationSuffixes();

   JavaMigration[] getJavaMigrations();

   boolean isPlaceholderReplacement();

   String getPlaceholderSuffix();

   String getPlaceholderPrefix();

   String getPlaceholderSeparator();

   String getScriptPlaceholderSuffix();

   String getScriptPlaceholderPrefix();

   Map<String, String> getPlaceholders();

   MigrationVersion getTarget();

   boolean isFailOnMissingTarget();

   MigrationPattern[] getCherryPick();

   String getTable();

   String getTablespace();

   String getDefaultSchema();

   String[] getSchemas();

   Charset getEncoding();

   boolean isDetectEncoding();

   Location[] getLocations();

   boolean isBaselineOnMigrate();

   boolean isSkipExecutingMigrations();

   boolean isOutOfOrder();

   @Deprecated
   boolean isIgnoreMissingMigrations();

   @Deprecated
   boolean isIgnoreIgnoredMigrations();

   @Deprecated
   boolean isIgnorePendingMigrations();

   @Deprecated
   boolean isIgnoreFutureMigrations();

   ValidatePattern[] getIgnoreMigrationPatterns();

   boolean isValidateMigrationNaming();

   boolean isValidateOnMigrate();

   boolean isCleanOnValidationError();

   boolean isCleanDisabled();

   boolean isMixed();

   boolean isGroup();

   String getInstalledBy();

   String[] getErrorOverrides();

   OutputStream getDryRunOutput();

   boolean isStream();

   boolean isBatch();

   boolean isOracleSqlplus();

   boolean isOracleSqlplusWarn();

   String getKerberosConfigFile();

   String getOracleKerberosConfigFile();

   String getOracleKerberosCacheFile();

   String getLicenseKey();

   boolean isOutputQueryResults();

   ResourceProvider getResourceProvider();

   ClassProvider<JavaMigration> getJavaMigrationClassProvider();

   boolean isCreateSchemas();

   int getLockRetryCount();

   Map<String, String> getJdbcProperties();

   boolean isFailOnMissingLocations();

   String getOracleWalletLocation();

   String[] getLoggers();
}
