package org.flywaydb.core.api.configuration;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.ClassUtils;

public class FluentConfiguration implements Configuration {
   private final ClassicConfiguration config;

   public FluentConfiguration() {
      this.config = new ClassicConfiguration();
   }

   public FluentConfiguration(ClassLoader classLoader) {
      this.config = new ClassicConfiguration(classLoader);
   }

   public Flyway load() {
      return new Flyway(this);
   }

   public FluentConfiguration configuration(Configuration configuration) {
      this.config.configure(configuration);
      return this;
   }

   public FluentConfiguration dryRunOutput(OutputStream dryRunOutput) {
      this.config.setDryRunOutput(dryRunOutput);
      return this;
   }

   public FluentConfiguration dryRunOutput(File dryRunOutput) {
      this.config.setDryRunOutputAsFile(dryRunOutput);
      return this;
   }

   public FluentConfiguration dryRunOutput(String dryRunOutputFileName) {
      this.config.setDryRunOutputAsFileName(dryRunOutputFileName);
      return this;
   }

   public FluentConfiguration errorOverrides(String... errorOverrides) {
      this.config.setErrorOverrides(errorOverrides);
      return this;
   }

   public FluentConfiguration group(boolean group) {
      this.config.setGroup(group);
      return this;
   }

   public FluentConfiguration installedBy(String installedBy) {
      this.config.setInstalledBy(installedBy);
      return this;
   }

   public FluentConfiguration loggers(String... loggers) {
      this.config.setLoggers(loggers);
      return this;
   }

   public FluentConfiguration mixed(boolean mixed) {
      this.config.setMixed(mixed);
      return this;
   }

   @Deprecated
   public FluentConfiguration ignoreMissingMigrations(boolean ignoreMissingMigrations) {
      this.config.setIgnoreMissingMigrations(ignoreMissingMigrations);
      return this;
   }

   @Deprecated
   public FluentConfiguration ignoreIgnoredMigrations(boolean ignoreIgnoredMigrations) {
      this.config.setIgnoreIgnoredMigrations(ignoreIgnoredMigrations);
      return this;
   }

   @Deprecated
   public FluentConfiguration ignorePendingMigrations(boolean ignorePendingMigrations) {
      this.config.setIgnorePendingMigrations(ignorePendingMigrations);
      return this;
   }

   @Deprecated
   public FluentConfiguration ignoreFutureMigrations(boolean ignoreFutureMigrations) {
      this.config.setIgnoreFutureMigrations(ignoreFutureMigrations);
      return this;
   }

   public FluentConfiguration ignoreMigrationPatterns(String... ignoreMigrationPatterns) {
      this.config.setIgnoreMigrationPatterns(ignoreMigrationPatterns);
      return this;
   }

   public FluentConfiguration ignoreMigrationPatterns(ValidatePattern... ignoreMigrationPatterns) {
      this.config.setIgnoreMigrationPatterns(ignoreMigrationPatterns);
      return this;
   }

   public FluentConfiguration validateMigrationNaming(boolean validateMigrationNaming) {
      this.config.setValidateMigrationNaming(validateMigrationNaming);
      return this;
   }

   public FluentConfiguration validateOnMigrate(boolean validateOnMigrate) {
      this.config.setValidateOnMigrate(validateOnMigrate);
      return this;
   }

   public FluentConfiguration cleanOnValidationError(boolean cleanOnValidationError) {
      this.config.setCleanOnValidationError(cleanOnValidationError);
      return this;
   }

   public FluentConfiguration cleanDisabled(boolean cleanDisabled) {
      this.config.setCleanDisabled(cleanDisabled);
      return this;
   }

   public FluentConfiguration locations(String... locations) {
      this.config.setLocationsAsStrings(locations);
      return this;
   }

   public FluentConfiguration locations(Location... locations) {
      this.config.setLocations(locations);
      return this;
   }

   public FluentConfiguration encoding(String encoding) {
      this.config.setEncodingAsString(encoding);
      return this;
   }

   public FluentConfiguration encoding(Charset encoding) {
      this.config.setEncoding(encoding);
      return this;
   }

   public FluentConfiguration detectEncoding(boolean detectEncoding) {
      this.config.setDetectEncoding(detectEncoding);
      return this;
   }

   public FluentConfiguration defaultSchema(String schema) {
      this.config.setDefaultSchema(schema);
      return this;
   }

   public FluentConfiguration schemas(String... schemas) {
      this.config.setSchemas(schemas);
      return this;
   }

   public FluentConfiguration table(String table) {
      this.config.setTable(table);
      return this;
   }

   public FluentConfiguration tablespace(String tablespace) {
      this.config.setTablespace(tablespace);
      return this;
   }

   public FluentConfiguration target(MigrationVersion target) {
      this.config.setTarget(target);
      return this;
   }

   public FluentConfiguration target(String target) {
      this.config.setTargetAsString(target);
      return this;
   }

   public FluentConfiguration cherryPick(MigrationPattern... cherryPick) {
      this.config.setCherryPick(cherryPick);
      return this;
   }

   public FluentConfiguration cherryPick(String... cherryPickAsString) {
      this.config.setCherryPick(cherryPickAsString);
      return this;
   }

   public FluentConfiguration placeholderReplacement(boolean placeholderReplacement) {
      this.config.setPlaceholderReplacement(placeholderReplacement);
      return this;
   }

   public FluentConfiguration placeholders(Map<String, String> placeholders) {
      this.config.setPlaceholders(placeholders);
      return this;
   }

   public FluentConfiguration placeholderPrefix(String placeholderPrefix) {
      this.config.setPlaceholderPrefix(placeholderPrefix);
      return this;
   }

   public FluentConfiguration placeholderSuffix(String placeholderSuffix) {
      this.config.setPlaceholderSuffix(placeholderSuffix);
      return this;
   }

   public FluentConfiguration placeholderSeparator(String placeholderSeparator) {
      this.config.setPlaceholderSeparator(placeholderSeparator);
      return this;
   }

   public FluentConfiguration scriptPlaceholderPrefix(String scriptPlaceholderPrefix) {
      this.config.setScriptPlaceholderPrefix(scriptPlaceholderPrefix);
      return this;
   }

   public FluentConfiguration scriptPlaceholderSuffix(String scriptPlaceholderSuffix) {
      this.config.setScriptPlaceholderSuffix(scriptPlaceholderSuffix);
      return this;
   }

   public FluentConfiguration sqlMigrationPrefix(String sqlMigrationPrefix) {
      this.config.setSqlMigrationPrefix(sqlMigrationPrefix);
      return this;
   }

   public FluentConfiguration baselineMigrationPrefix(String baselineMigrationPrefix) {
      this.config.setBaselineMigrationPrefix(baselineMigrationPrefix);
      return this;
   }

   public FluentConfiguration undoSqlMigrationPrefix(String undoSqlMigrationPrefix) {
      this.config.setUndoSqlMigrationPrefix(undoSqlMigrationPrefix);
      return this;
   }

   public FluentConfiguration repeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
      this.config.setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix);
      return this;
   }

   public FluentConfiguration sqlMigrationSeparator(String sqlMigrationSeparator) {
      this.config.setSqlMigrationSeparator(sqlMigrationSeparator);
      return this;
   }

   public FluentConfiguration sqlMigrationSuffixes(String... sqlMigrationSuffixes) {
      this.config.setSqlMigrationSuffixes(sqlMigrationSuffixes);
      return this;
   }

   public FluentConfiguration javaMigrations(JavaMigration... javaMigrations) {
      this.config.setJavaMigrations(javaMigrations);
      return this;
   }

   public FluentConfiguration dataSource(DataSource dataSource) {
      this.config.setDataSource(dataSource);
      return this;
   }

   public FluentConfiguration dataSource(String url, String user, String password) {
      this.config.setDataSource(url, user, password);
      return this;
   }

   public FluentConfiguration connectRetries(int connectRetries) {
      this.config.setConnectRetries(connectRetries);
      return this;
   }

   public FluentConfiguration connectRetriesInterval(int connectRetriesInterval) {
      this.config.setConnectRetriesInterval(connectRetriesInterval);
      return this;
   }

   public FluentConfiguration initSql(String initSql) {
      this.config.setInitSql(initSql);
      return this;
   }

   public FluentConfiguration baselineVersion(MigrationVersion baselineVersion) {
      this.config.setBaselineVersion(baselineVersion);
      return this;
   }

   public FluentConfiguration baselineVersion(String baselineVersion) {
      this.config.setBaselineVersion(MigrationVersion.fromVersion(baselineVersion));
      return this;
   }

   public FluentConfiguration baselineDescription(String baselineDescription) {
      this.config.setBaselineDescription(baselineDescription);
      return this;
   }

   public FluentConfiguration baselineOnMigrate(boolean baselineOnMigrate) {
      this.config.setBaselineOnMigrate(baselineOnMigrate);
      return this;
   }

   public FluentConfiguration outOfOrder(boolean outOfOrder) {
      this.config.setOutOfOrder(outOfOrder);
      return this;
   }

   public FluentConfiguration skipExecutingMigrations(boolean skipExecutingMigrations) {
      this.config.setSkipExecutingMigrations(skipExecutingMigrations);
      return this;
   }

   public FluentConfiguration callbacks(Callback... callbacks) {
      this.config.setCallbacks(callbacks);
      return this;
   }

   public FluentConfiguration callbacks(String... callbacks) {
      this.config.setCallbacksAsClassNames(callbacks);
      return this;
   }

   public FluentConfiguration skipDefaultCallbacks(boolean skipDefaultCallbacks) {
      this.config.setSkipDefaultCallbacks(skipDefaultCallbacks);
      return this;
   }

   public FluentConfiguration resolvers(MigrationResolver... resolvers) {
      this.config.setResolvers(resolvers);
      return this;
   }

   public FluentConfiguration resolvers(String... resolvers) {
      this.config.setResolversAsClassNames(resolvers);
      return this;
   }

   public FluentConfiguration skipDefaultResolvers(boolean skipDefaultResolvers) {
      this.config.setSkipDefaultResolvers(skipDefaultResolvers);
      return this;
   }

   public FluentConfiguration stream(boolean stream) {
      this.config.setStream(stream);
      return this;
   }

   public FluentConfiguration batch(boolean batch) {
      this.config.setBatch(batch);
      return this;
   }

   public FluentConfiguration lockRetryCount(int lockRetryCount) {
      this.config.setLockRetryCount(lockRetryCount);
      return this;
   }

   public FluentConfiguration jdbcProperties(Map<String, String> jdbcProperties) {
      this.config.setJdbcProperties(jdbcProperties);
      return this;
   }

   public FluentConfiguration oracleSqlplus(boolean oracleSqlplus) {
      this.config.setOracleSqlplus(oracleSqlplus);
      return this;
   }

   public FluentConfiguration oracleSqlplusWarn(boolean oracleSqlplusWarn) {
      this.config.setOracleSqlplusWarn(oracleSqlplusWarn);
      return this;
   }

   public FluentConfiguration kerberosConfigFile(String kerberosConfigFile) {
      this.config.setKerberosConfigFile(kerberosConfigFile);
      return this;
   }

   /** @deprecated */
   public FluentConfiguration oracleKerberosConfigFile(String oracleKerberosConfigFile) {
      this.config.setOracleKerberosConfigFile(oracleKerberosConfigFile);
      return this;
   }

   public FluentConfiguration oracleKerberosCacheFile(String oracleKerberosCacheFile) {
      this.config.setOracleKerberosCacheFile(oracleKerberosCacheFile);
      return this;
   }

   public FluentConfiguration oracleWalletLocation(String oracleWalletLocation) {
      this.config.setOracleWalletLocation(oracleWalletLocation);
      return this;
   }

   public FluentConfiguration licenseKey(String licenseKey) {
      this.config.setLicenseKey(licenseKey);
      return this;
   }

   public FluentConfiguration resourceProvider(ResourceProvider resourceProvider) {
      this.config.setResourceProvider(resourceProvider);
      return this;
   }

   public FluentConfiguration javaMigrationClassProvider(ClassProvider<JavaMigration> javaMigrationClassProvider) {
      this.config.setJavaMigrationClassProvider(javaMigrationClassProvider);
      return this;
   }

   public FluentConfiguration outputQueryResults(boolean outputQueryResults) {
      this.config.setOutputQueryResults(outputQueryResults);
      return this;
   }

   public FluentConfiguration configuration(Properties properties) {
      this.config.configure(properties);
      return this;
   }

   public FluentConfiguration configuration(Map<String, String> props) {
      this.config.configure(props);
      return this;
   }

   public FluentConfiguration loadDefaultConfigurationFiles() {
      return this.loadDefaultConfigurationFiles("UTF-8");
   }

   public FluentConfiguration loadDefaultConfigurationFiles(String encoding) {
      String installationPath = ClassUtils.getLocationOnDisk(FluentConfiguration.class);
      File installationDir = new File(installationPath).getParentFile();
      Map<String, String> configMap = ConfigUtils.loadDefaultConfigurationFiles(installationDir, encoding);
      this.config.configure(configMap);
      return this;
   }

   public FluentConfiguration createSchemas(boolean createSchemas) {
      this.config.setShouldCreateSchemas(createSchemas);
      return this;
   }

   public FluentConfiguration envVars() {
      this.config.configureUsingEnvVars();
      return this;
   }

   public FluentConfiguration failOnMissingLocations(boolean failOnMissingLocations) {
      this.config.setFailOnMissingLocations(failOnMissingLocations);
      return this;
   }

   @Override
   public ClassLoader getClassLoader() {
      return this.config.getClassLoader();
   }

   @Override
   public String getUrl() {
      return this.config.getUrl();
   }

   @Override
   public String getUser() {
      return this.config.getUser();
   }

   @Override
   public String getPassword() {
      return this.config.getPassword();
   }

   @Override
   public DataSource getDataSource() {
      return this.config.getDataSource();
   }

   @Override
   public int getConnectRetries() {
      return this.config.getConnectRetries();
   }

   @Override
   public int getConnectRetriesInterval() {
      return this.config.getConnectRetriesInterval();
   }

   @Override
   public String getInitSql() {
      return this.config.getInitSql();
   }

   @Override
   public MigrationVersion getBaselineVersion() {
      return this.config.getBaselineVersion();
   }

   @Override
   public String getBaselineDescription() {
      return this.config.getBaselineDescription();
   }

   @Override
   public MigrationResolver[] getResolvers() {
      return this.config.getResolvers();
   }

   @Override
   public boolean isSkipDefaultResolvers() {
      return this.config.isSkipDefaultResolvers();
   }

   @Override
   public Callback[] getCallbacks() {
      return this.config.getCallbacks();
   }

   @Override
   public boolean isSkipDefaultCallbacks() {
      return this.config.isSkipDefaultCallbacks();
   }

   @Override
   public String getSqlMigrationPrefix() {
      return this.config.getSqlMigrationPrefix();
   }

   @Override
   public String getBaselineMigrationPrefix() {
      return this.config.getBaselineMigrationPrefix();
   }

   @Override
   public String getUndoSqlMigrationPrefix() {
      return this.config.getUndoSqlMigrationPrefix();
   }

   @Override
   public String getRepeatableSqlMigrationPrefix() {
      return this.config.getRepeatableSqlMigrationPrefix();
   }

   @Override
   public String getSqlMigrationSeparator() {
      return this.config.getSqlMigrationSeparator();
   }

   @Override
   public String[] getSqlMigrationSuffixes() {
      return this.config.getSqlMigrationSuffixes();
   }

   @Override
   public JavaMigration[] getJavaMigrations() {
      return this.config.getJavaMigrations();
   }

   @Override
   public boolean isPlaceholderReplacement() {
      return this.config.isPlaceholderReplacement();
   }

   @Override
   public String getPlaceholderSuffix() {
      return this.config.getPlaceholderSuffix();
   }

   @Override
   public String getPlaceholderPrefix() {
      return this.config.getPlaceholderPrefix();
   }

   @Override
   public String getPlaceholderSeparator() {
      return this.config.getPlaceholderSeparator();
   }

   @Override
   public String getScriptPlaceholderSuffix() {
      return this.config.getScriptPlaceholderSuffix();
   }

   @Override
   public String getScriptPlaceholderPrefix() {
      return this.config.getScriptPlaceholderPrefix();
   }

   @Override
   public Map<String, String> getPlaceholders() {
      return this.config.getPlaceholders();
   }

   @Override
   public MigrationVersion getTarget() {
      return this.config.getTarget();
   }

   @Override
   public boolean isFailOnMissingTarget() {
      return this.config.isFailOnMissingTarget();
   }

   @Override
   public MigrationPattern[] getCherryPick() {
      return this.config.getCherryPick();
   }

   @Override
   public String getTable() {
      return this.config.getTable();
   }

   @Override
   public String getTablespace() {
      return this.config.getTablespace();
   }

   @Override
   public String getDefaultSchema() {
      return this.config.getDefaultSchema();
   }

   @Override
   public String[] getSchemas() {
      return this.config.getSchemas();
   }

   @Override
   public Charset getEncoding() {
      return this.config.getEncoding();
   }

   @Override
   public boolean isDetectEncoding() {
      return this.config.isDetectEncoding();
   }

   @Override
   public Location[] getLocations() {
      return this.config.getLocations();
   }

   @Override
   public boolean isBaselineOnMigrate() {
      return this.config.isBaselineOnMigrate();
   }

   @Override
   public boolean isSkipExecutingMigrations() {
      return this.config.isSkipExecutingMigrations();
   }

   @Override
   public boolean isOutOfOrder() {
      return this.config.isOutOfOrder();
   }

   @Deprecated
   @Override
   public boolean isIgnoreMissingMigrations() {
      return this.config.isIgnoreMissingMigrations();
   }

   @Deprecated
   @Override
   public boolean isIgnoreIgnoredMigrations() {
      return this.config.isIgnoreIgnoredMigrations();
   }

   @Deprecated
   @Override
   public boolean isIgnorePendingMigrations() {
      return this.config.isIgnorePendingMigrations();
   }

   @Deprecated
   @Override
   public boolean isIgnoreFutureMigrations() {
      return this.config.isIgnoreFutureMigrations();
   }

   @Override
   public ValidatePattern[] getIgnoreMigrationPatterns() {
      return this.config.getIgnoreMigrationPatterns();
   }

   @Override
   public boolean isValidateMigrationNaming() {
      return this.config.isValidateMigrationNaming();
   }

   @Override
   public boolean isValidateOnMigrate() {
      return this.config.isValidateOnMigrate();
   }

   @Override
   public boolean isCleanOnValidationError() {
      return this.config.isCleanOnValidationError();
   }

   @Override
   public boolean isCleanDisabled() {
      return this.config.isCleanDisabled();
   }

   @Override
   public boolean isMixed() {
      return this.config.isMixed();
   }

   @Override
   public boolean isGroup() {
      return this.config.isGroup();
   }

   @Override
   public String getInstalledBy() {
      return this.config.getInstalledBy();
   }

   @Override
   public String[] getErrorOverrides() {
      return this.config.getErrorOverrides();
   }

   @Override
   public OutputStream getDryRunOutput() {
      return this.config.getDryRunOutput();
   }

   @Override
   public boolean isStream() {
      return this.config.isStream();
   }

   @Override
   public boolean isBatch() {
      return this.config.isBatch();
   }

   @Override
   public boolean isOracleSqlplus() {
      return this.config.isOracleSqlplus();
   }

   @Override
   public boolean isOracleSqlplusWarn() {
      return this.config.isOracleSqlplusWarn();
   }

   @Override
   public String getKerberosConfigFile() {
      return this.config.getKerberosConfigFile();
   }

   @Override
   public String getOracleKerberosConfigFile() {
      return this.config.getOracleKerberosConfigFile();
   }

   @Override
   public String getOracleKerberosCacheFile() {
      return this.config.getOracleKerberosCacheFile();
   }

   @Override
   public String getLicenseKey() {
      return this.config.getLicenseKey();
   }

   @Override
   public boolean isOutputQueryResults() {
      return this.config.isOutputQueryResults();
   }

   @Override
   public ResourceProvider getResourceProvider() {
      return this.config.getResourceProvider();
   }

   @Override
   public ClassProvider<JavaMigration> getJavaMigrationClassProvider() {
      return this.config.getJavaMigrationClassProvider();
   }

   @Override
   public boolean isCreateSchemas() {
      return this.config.isCreateSchemas();
   }

   @Override
   public int getLockRetryCount() {
      return this.config.getLockRetryCount();
   }

   @Override
   public Map<String, String> getJdbcProperties() {
      return this.config.getJdbcProperties();
   }

   @Override
   public boolean isFailOnMissingLocations() {
      return this.config.isFailOnMissingLocations();
   }

   @Override
   public String getOracleWalletLocation() {
      return this.config.getOracleWalletLocation();
   }

   @Override
   public String[] getLoggers() {
      return this.config.getLoggers();
   }
}
