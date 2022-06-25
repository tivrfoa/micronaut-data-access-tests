package org.flywaydb.core.api.configuration;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.ConfigurationProvider;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.scanner.ClasspathClassScanner;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.StringUtils;

public class ClassicConfiguration implements Configuration {
   private static final Log LOG = LogFactory.getLog(ClassicConfiguration.class);
   private String driver;
   private String url;
   private String user;
   private String password;
   private DataSource dataSource;
   private int connectRetries;
   private int connectRetriesInterval = 120;
   private String initSql;
   private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
   private Locations locations = new Locations("db/migration");
   private Charset encoding = StandardCharsets.UTF_8;
   private boolean detectEncoding = false;
   private String defaultSchema = null;
   private String[] schemas = new String[0];
   private String table = "flyway_schema_history";
   private String tablespace;
   private MigrationVersion target;
   private boolean failOnMissingTarget = true;
   private MigrationPattern[] cherryPick;
   private boolean placeholderReplacement = true;
   private Map<String, String> placeholders = new HashMap();
   private String placeholderPrefix = "${";
   private String placeholderSuffix = "}";
   private String placeholderSeparator = ":";
   private String scriptPlaceholderPrefix = "FP__";
   private String scriptPlaceholderSuffix = "__";
   private String sqlMigrationPrefix = "V";
   private String baselineMigrationPrefix = "B";
   private String undoSqlMigrationPrefix = "U";
   private String repeatableSqlMigrationPrefix = "R";
   private ResourceProvider resourceProvider = null;
   private ClassProvider<JavaMigration> javaMigrationClassProvider = null;
   private String sqlMigrationSeparator = "__";
   private String[] sqlMigrationSuffixes = new String[]{".sql"};
   private JavaMigration[] javaMigrations = new JavaMigration[0];
   @Deprecated
   private boolean ignoreMissingMigrations;
   @Deprecated
   private boolean ignoreIgnoredMigrations;
   @Deprecated
   private boolean ignorePendingMigrations;
   @Deprecated
   private boolean ignoreFutureMigrations = true;
   private ValidatePattern[] ignoreMigrationPatterns = new ValidatePattern[0];
   private boolean validateMigrationNaming = false;
   private boolean validateOnMigrate = true;
   private boolean cleanOnValidationError;
   private boolean cleanDisabled;
   private MigrationVersion baselineVersion = MigrationVersion.fromVersion("1");
   private String baselineDescription = "<< Flyway Baseline >>";
   private boolean baselineOnMigrate;
   private boolean outOfOrder;
   private boolean skipExecutingMigrations;
   private final List<Callback> callbacks = new ArrayList();
   private boolean skipDefaultCallbacks;
   private MigrationResolver[] resolvers = new MigrationResolver[0];
   private boolean skipDefaultResolvers;
   private boolean mixed;
   private boolean group;
   private String installedBy;
   private boolean createSchemas = true;
   private String[] errorOverrides = new String[0];
   private OutputStream dryRunOutput;
   private boolean stream;
   private boolean batch;
   private boolean outputQueryResults = true;
   private String licenseKey;
   private int lockRetryCount = 50;
   private Map<String, String> jdbcProperties;
   private boolean oracleSqlplus;
   private boolean oracleSqlplusWarn;
   private String kerberosConfigFile = "";
   private String oracleKerberosConfigFile = "";
   private String oracleKerberosCacheFile = "";
   private String oracleWalletLocation;
   private boolean failOnMissingLocations = false;
   private String[] loggers = new String[]{"auto"};
   private final ClasspathClassScanner classScanner;

   public ClassicConfiguration() {
      this.classScanner = new ClasspathClassScanner(this.classLoader);
   }

   public ClassicConfiguration(ClassLoader classLoader) {
      if (classLoader != null) {
         this.classLoader = classLoader;
      }

      this.classScanner = new ClasspathClassScanner(this.classLoader);
   }

   public ClassicConfiguration(Configuration configuration) {
      this(configuration.getClassLoader());
      this.configure(configuration);
   }

   @Override
   public Location[] getLocations() {
      return (Location[])this.locations.getLocations().toArray(new Location[0]);
   }

   @Override
   public DataSource getDataSource() {
      if (this.dataSource == null && (StringUtils.hasLength(this.driver) || StringUtils.hasLength(this.user) || StringUtils.hasLength(this.password))) {
         LOG.warn("Discarding INCOMPLETE dataSource configuration! flyway.url must be set.");
      }

      return this.dataSource;
   }

   @Override
   public Callback[] getCallbacks() {
      return (Callback[])this.callbacks.toArray(new Callback[0]);
   }

   public void setDryRunOutput(OutputStream dryRunOutput) {
      throw new FlywayTeamsUpgradeRequiredException("dryRunOutput");
   }

   public void setDryRunOutputAsFile(File dryRunOutput) {
      throw new FlywayTeamsUpgradeRequiredException("dryRunOutput");
   }

   public void setDryRunOutputAsFileName(String dryRunOutputFileName) {
      throw new FlywayTeamsUpgradeRequiredException("dryRunOutput");
   }

   public void setErrorOverrides(String... errorOverrides) {
      throw new FlywayTeamsUpgradeRequiredException("errorOverrides");
   }

   public void setInstalledBy(String installedBy) {
      if ("".equals(installedBy)) {
         installedBy = null;
      }

      this.installedBy = installedBy;
   }

   public void setLoggers(String... loggers) {
      this.loggers = loggers;
   }

   public void setIgnoreMigrationPatterns(String... ignoreMigrationPatterns) {
      this.ignoreMigrationPatterns = (ValidatePattern[])Arrays.stream(ignoreMigrationPatterns)
         .map(ValidatePattern::fromPattern)
         .toArray(x$0 -> new ValidatePattern[x$0]);
   }

   public void setIgnoreMigrationPatterns(ValidatePattern... ignoreMigrationPatterns) {
      this.ignoreMigrationPatterns = ignoreMigrationPatterns;
   }

   public void setLocationsAsStrings(String... locations) {
      this.locations = new Locations(locations);
   }

   public void setLocations(Location... locations) {
      this.locations = new Locations(Arrays.asList(locations));
   }

   public void setDetectEncoding(boolean detectEncoding) {
      throw new FlywayTeamsUpgradeRequiredException("detectEncoding");
   }

   public void setEncodingAsString(String encoding) {
      this.encoding = Charset.forName(encoding);
   }

   public void setTargetAsString(String target) {
      if (target.endsWith("?")) {
         throw new FlywayTeamsUpgradeRequiredException("failOnMissingTarget");
      } else {
         this.setFailOnMissingTarget(true);
         this.setTarget(MigrationVersion.fromVersion(target));
      }
   }

   public void setCherryPick(MigrationPattern... cherryPick) {
      throw new FlywayTeamsUpgradeRequiredException("cherryPick");
   }

   public void setCherryPick(String... cherryPickAsString) {
      throw new FlywayTeamsUpgradeRequiredException("cherryPick");
   }

   public void setPlaceholderPrefix(String placeholderPrefix) {
      if (!StringUtils.hasLength(placeholderPrefix)) {
         throw new FlywayException("placeholderPrefix cannot be empty!", ErrorCode.CONFIGURATION);
      } else {
         this.placeholderPrefix = placeholderPrefix;
      }
   }

   public void setScriptPlaceholderPrefix(String scriptPlaceholderPrefix) {
      if (!StringUtils.hasLength(scriptPlaceholderPrefix)) {
         throw new FlywayException("scriptPlaceholderPrefix cannot be empty!", ErrorCode.CONFIGURATION);
      } else {
         this.scriptPlaceholderPrefix = scriptPlaceholderPrefix;
      }
   }

   public void setPlaceholderSuffix(String placeholderSuffix) {
      if (!StringUtils.hasLength(placeholderSuffix)) {
         throw new FlywayException("placeholderSuffix cannot be empty!", ErrorCode.CONFIGURATION);
      } else {
         this.placeholderSuffix = placeholderSuffix;
      }
   }

   public void setPlaceholderSeparator(String placeholderSeparator) {
      if (!StringUtils.hasLength(placeholderSeparator)) {
         throw new FlywayException("placeholderSeparator cannot be empty!", ErrorCode.CONFIGURATION);
      } else {
         this.placeholderSeparator = placeholderSeparator;
      }
   }

   public void setScriptPlaceholderSuffix(String scriptPlaceholderSuffix) {
      if (!StringUtils.hasLength(scriptPlaceholderSuffix)) {
         throw new FlywayException("scriptPlaceholderSuffix cannot be empty!", ErrorCode.CONFIGURATION);
      } else {
         this.scriptPlaceholderSuffix = scriptPlaceholderSuffix;
      }
   }

   public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
      this.sqlMigrationPrefix = sqlMigrationPrefix;
   }

   public void setBaselineMigrationPrefix(String baselineMigrationPrefix) {
      throw new FlywayTeamsUpgradeRequiredException("baselineMigrationPrefix");
   }

   public void setUndoSqlMigrationPrefix(String undoSqlMigrationPrefix) {
      throw new FlywayTeamsUpgradeRequiredException("undoSqlMigrationPrefix");
   }

   public void setJavaMigrations(JavaMigration... javaMigrations) {
      if (javaMigrations == null) {
         throw new FlywayException("javaMigrations cannot be null", ErrorCode.CONFIGURATION);
      } else {
         this.javaMigrations = javaMigrations;
      }
   }

   public void setStream(boolean stream) {
      throw new FlywayTeamsUpgradeRequiredException("stream");
   }

   public void setBatch(boolean batch) {
      throw new FlywayTeamsUpgradeRequiredException("batch");
   }

   public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
      if (!StringUtils.hasLength(sqlMigrationSeparator)) {
         throw new FlywayException("sqlMigrationSeparator cannot be empty!", ErrorCode.CONFIGURATION);
      } else {
         this.sqlMigrationSeparator = sqlMigrationSeparator;
      }
   }

   public void setSqlMigrationSuffixes(String... sqlMigrationSuffixes) {
      this.sqlMigrationSuffixes = sqlMigrationSuffixes;
   }

   public void setDataSource(DataSource dataSource) {
      this.driver = null;
      this.url = null;
      this.user = null;
      this.password = null;
      this.dataSource = dataSource;
   }

   public void setDataSource(String url, String user, String password) {
      this.url = url;
      this.user = user;
      this.password = password;
      this.dataSource = new DriverDataSource(this.classLoader, null, url, user, password, this);
   }

   public void setConnectRetries(int connectRetries) {
      if (connectRetries < 0) {
         throw new FlywayException("Invalid number of connectRetries (must be 0 or greater): " + connectRetries, ErrorCode.CONFIGURATION);
      } else {
         this.connectRetries = connectRetries;
      }
   }

   public void setConnectRetriesInterval(int connectRetriesInterval) {
      if (connectRetriesInterval < 0) {
         throw new FlywayException("Invalid number for connectRetriesInterval (must be 0 or greater): " + connectRetriesInterval, ErrorCode.CONFIGURATION);
      } else {
         this.connectRetriesInterval = connectRetriesInterval;
      }
   }

   public void setBaselineVersionAsString(String baselineVersion) {
      this.baselineVersion = MigrationVersion.fromVersion(baselineVersion);
   }

   public void setSkipExecutingMigrations(boolean skipExecutingMigrations) {
      throw new FlywayTeamsUpgradeRequiredException("skipExecutingMigrations");
   }

   public void setCallbacks(Callback... callbacks) {
      this.callbacks.clear();
      this.callbacks.addAll(Arrays.asList(callbacks));
   }

   public void setCallbacksAsClassNames(String... callbacks) {
      this.callbacks.clear();

      for(String callback : callbacks) {
         this.loadCallbackPath(callback);
      }

   }

   private void loadCallbackPath(String callbackPath) {
      Object o = null;

      try {
         o = ClassUtils.instantiate(callbackPath, this.classLoader);
      } catch (FlywayException var4) {
      }

      if (o != null) {
         if (!(o instanceof Callback)) {
            throw new FlywayException(
               "Invalid callback: " + callbackPath + " (must implement org.flywaydb.core.api.callback.Callback)", ErrorCode.CONFIGURATION
            );
         }

         this.callbacks.add((Callback)o);
      } else {
         this.loadCallbackLocation(callbackPath, true);
      }

   }

   public void loadCallbackLocation(String path, boolean errorOnNotFound) {
      for(String callback : this.classScanner.scanForType(path, Callback.class, errorOnNotFound)) {
         Class<? extends Callback> callbackClass;
         try {
            callbackClass = ClassUtils.loadClass(Callback.class, callback, this.classLoader);
         } catch (Throwable var9) {
            Throwable rootCause = ExceptionUtils.getRootCause(var9);
            LOG.warn(
               "Skipping "
                  + Callback.class
                  + ": "
                  + ClassUtils.formatThrowable(var9)
                  + (rootCause == var9 ? "" : " caused by " + ClassUtils.formatThrowable(rootCause) + " at " + ExceptionUtils.getThrowLocation(rootCause))
            );
            callbackClass = null;
         }

         if (callbackClass != null) {
            Callback callbackObj = ClassUtils.instantiate(callback, this.classLoader);
            this.callbacks.add(callbackObj);
         }
      }

   }

   public void setResolvers(MigrationResolver... resolvers) {
      this.resolvers = resolvers;
   }

   public void setResolversAsClassNames(String... resolvers) {
      List<MigrationResolver> resolverList = ClassUtils.instantiateAll(resolvers, this.classLoader);
      this.setResolvers((MigrationResolver[])resolverList.toArray(new MigrationResolver[resolvers.length]));
   }

   public void setOracleSqlplus(boolean oracleSqlplus) {
      throw new FlywayTeamsUpgradeRequiredException("oracle.sqlplus");
   }

   public void setOracleSqlplusWarn(boolean oracleSqlplusWarn) {
      throw new FlywayTeamsUpgradeRequiredException("oracle.sqlplusWarn");
   }

   /** @deprecated */
   public void setOracleKerberosConfigFile(String oracleKerberosConfigFile) {
      throw new FlywayTeamsUpgradeRequiredException("oracle.kerberosConfigFile");
   }

   public void setOracleKerberosCacheFile(String oracleKerberosCacheFile) {
      throw new FlywayTeamsUpgradeRequiredException("oracle.kerberosCacheFile");
   }

   public void setKerberosConfigFile(String kerberosConfigFile) {
      throw new FlywayTeamsUpgradeRequiredException("kerberosConfigFile");
   }

   public void setOracleWalletLocation(String oracleWalletLocation) {
      throw new FlywayTeamsUpgradeRequiredException("oracle.walletLocation");
   }

   public void setShouldCreateSchemas(boolean createSchemas) {
      this.createSchemas = createSchemas;
   }

   public void setLicenseKey(String licenseKey) {
      LOG.warn(Edition.ENTERPRISE + " upgrade required: licenseKey is not supported by " + Edition.COMMUNITY + ".");
   }

   public void setOutputQueryResults(boolean outputQueryResults) {
      throw new FlywayTeamsUpgradeRequiredException("outputQueryResults");
   }

   public void setJdbcProperties(Map<String, String> jdbcProperties) {
      throw new FlywayTeamsUpgradeRequiredException("jdbcProperties");
   }

   public void configure(Configuration configuration) {
      this.setLoggers(configuration.getLoggers());
      this.setBaselineDescription(configuration.getBaselineDescription());
      this.setBaselineOnMigrate(configuration.isBaselineOnMigrate());
      this.setBaselineVersion(configuration.getBaselineVersion());
      this.setCallbacks(configuration.getCallbacks());
      this.setCleanDisabled(configuration.isCleanDisabled());
      this.setCleanOnValidationError(configuration.isCleanOnValidationError());
      this.setDataSource(configuration.getDataSource());
      this.setConnectRetries(configuration.getConnectRetries());
      this.setConnectRetriesInterval(configuration.getConnectRetriesInterval());
      this.setInitSql(configuration.getInitSql());
      this.setEncoding(configuration.getEncoding());
      this.setGroup(configuration.isGroup());
      this.setValidateMigrationNaming(configuration.isValidateMigrationNaming());
      this.setIgnoreMigrationPatterns(configuration.getIgnoreMigrationPatterns());
      this.setIgnoreFutureMigrations(configuration.isIgnoreFutureMigrations());
      this.setIgnoreMissingMigrations(configuration.isIgnoreMissingMigrations());
      this.setIgnoreIgnoredMigrations(configuration.isIgnoreIgnoredMigrations());
      this.setIgnorePendingMigrations(configuration.isIgnorePendingMigrations());
      this.setInstalledBy(configuration.getInstalledBy());
      this.setJavaMigrations(configuration.getJavaMigrations());
      this.setLocations(configuration.getLocations());
      this.setMixed(configuration.isMixed());
      this.setOutOfOrder(configuration.isOutOfOrder());
      this.setPlaceholderPrefix(configuration.getPlaceholderPrefix());
      this.setPlaceholderReplacement(configuration.isPlaceholderReplacement());
      this.setPlaceholders(configuration.getPlaceholders());
      this.setPlaceholderSuffix(configuration.getPlaceholderSuffix());
      this.setPlaceholderSeparator(configuration.getPlaceholderSeparator());
      this.setScriptPlaceholderPrefix(configuration.getScriptPlaceholderPrefix());
      this.setScriptPlaceholderSuffix(configuration.getScriptPlaceholderSuffix());
      this.setRepeatableSqlMigrationPrefix(configuration.getRepeatableSqlMigrationPrefix());
      this.setResolvers(configuration.getResolvers());
      this.setDefaultSchema(configuration.getDefaultSchema());
      this.setSchemas(configuration.getSchemas());
      this.setSkipDefaultCallbacks(configuration.isSkipDefaultCallbacks());
      this.setSkipDefaultResolvers(configuration.isSkipDefaultResolvers());
      this.setSqlMigrationPrefix(configuration.getSqlMigrationPrefix());
      this.setSqlMigrationSeparator(configuration.getSqlMigrationSeparator());
      this.setSqlMigrationSuffixes(configuration.getSqlMigrationSuffixes());
      this.setTable(configuration.getTable());
      this.setTablespace(configuration.getTablespace());
      this.setTarget(configuration.getTarget());
      this.setFailOnMissingTarget(configuration.isFailOnMissingTarget());
      this.setValidateOnMigrate(configuration.isValidateOnMigrate());
      this.setResourceProvider(configuration.getResourceProvider());
      this.setJavaMigrationClassProvider(configuration.getJavaMigrationClassProvider());
      this.setShouldCreateSchemas(configuration.isCreateSchemas());
      this.setLockRetryCount(configuration.getLockRetryCount());
      this.setFailOnMissingLocations(configuration.isFailOnMissingLocations());
      this.url = configuration.getUrl();
      this.user = configuration.getUser();
      this.password = configuration.getPassword();
      this.configureFromConfigurationProviders(this);
   }

   public void configure(Properties properties) {
      this.configure(ConfigUtils.propertiesToMap(properties));
   }

   public void configure(Map<String, String> props) {
      props = new HashMap(props);

      for(ConfigurationExtension configurationExtension : PluginRegister.getPlugins(ConfigurationExtension.class)) {
         configurationExtension.extractParametersFromConfiguration(props);
      }

      String driverProp = (String)props.remove("flyway.driver");
      if (driverProp != null) {
         this.dataSource = null;
         this.driver = driverProp;
      }

      String urlProp = (String)props.remove("flyway.url");
      if (urlProp != null) {
         this.dataSource = null;
         this.url = urlProp;
      }

      String userProp = (String)props.remove("flyway.user");
      if (userProp != null) {
         this.dataSource = null;
         this.user = userProp;
      }

      String passwordProp = (String)props.remove("flyway.password");
      if (passwordProp != null) {
         this.dataSource = null;
         this.password = passwordProp;
      }

      Integer connectRetriesProp = ConfigUtils.removeInteger(props, "flyway.connectRetries");
      if (connectRetriesProp != null) {
         this.setConnectRetries(connectRetriesProp);
      }

      Integer connectRetriesIntervalProp = ConfigUtils.removeInteger(props, "flyway.connectRetriesInterval");
      if (connectRetriesIntervalProp != null) {
         this.setConnectRetriesInterval(connectRetriesIntervalProp);
      }

      String initSqlProp = (String)props.remove("flyway.initSql");
      if (initSqlProp != null) {
         this.setInitSql(initSqlProp);
      }

      String locationsProp = (String)props.remove("flyway.locations");
      if (locationsProp != null) {
         this.setLocationsAsStrings(StringUtils.tokenizeToStringArray(locationsProp, ","));
      }

      Boolean placeholderReplacementProp = ConfigUtils.removeBoolean(props, "flyway.placeholderReplacement");
      if (placeholderReplacementProp != null) {
         this.setPlaceholderReplacement(placeholderReplacementProp);
      }

      String placeholderPrefixProp = (String)props.remove("flyway.placeholderPrefix");
      if (placeholderPrefixProp != null) {
         this.setPlaceholderPrefix(placeholderPrefixProp);
      }

      String placeholderSuffixProp = (String)props.remove("flyway.placeholderSuffix");
      if (placeholderSuffixProp != null) {
         this.setPlaceholderSuffix(placeholderSuffixProp);
      }

      String placeholderSeparatorProp = (String)props.remove("flyway.placeholderSeparator");
      if (placeholderSeparatorProp != null) {
         this.setPlaceholderSeparator(placeholderSeparatorProp);
      }

      String scriptPlaceholderPrefixProp = (String)props.remove("flyway.scriptPlaceholderPrefix");
      if (scriptPlaceholderPrefixProp != null) {
         this.setScriptPlaceholderPrefix(scriptPlaceholderPrefixProp);
      }

      String scriptPlaceholderSuffixProp = (String)props.remove("flyway.scriptPlaceholderSuffix");
      if (scriptPlaceholderSuffixProp != null) {
         this.setScriptPlaceholderSuffix(scriptPlaceholderSuffixProp);
      }

      String sqlMigrationPrefixProp = (String)props.remove("flyway.sqlMigrationPrefix");
      if (sqlMigrationPrefixProp != null) {
         this.setSqlMigrationPrefix(sqlMigrationPrefixProp);
      }

      String undoSqlMigrationPrefixProp = (String)props.remove("flyway.undoSqlMigrationPrefix");
      if (undoSqlMigrationPrefixProp != null) {
         this.setUndoSqlMigrationPrefix(undoSqlMigrationPrefixProp);
      }

      String baselineMigrationPrefixProp = (String)props.remove("flyway.baselineMigrationPrefix");
      if (baselineMigrationPrefixProp != null) {
         this.setBaselineMigrationPrefix(baselineMigrationPrefixProp);
      }

      String repeatableSqlMigrationPrefixProp = (String)props.remove("flyway.repeatableSqlMigrationPrefix");
      if (repeatableSqlMigrationPrefixProp != null) {
         this.setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefixProp);
      }

      String sqlMigrationSeparatorProp = (String)props.remove("flyway.sqlMigrationSeparator");
      if (sqlMigrationSeparatorProp != null) {
         this.setSqlMigrationSeparator(sqlMigrationSeparatorProp);
      }

      String sqlMigrationSuffixesProp = (String)props.remove("flyway.sqlMigrationSuffixes");
      if (sqlMigrationSuffixesProp != null) {
         this.setSqlMigrationSuffixes(StringUtils.tokenizeToStringArray(sqlMigrationSuffixesProp, ","));
      }

      String encodingProp = (String)props.remove("flyway.encoding");
      if (encodingProp != null) {
         this.setEncodingAsString(encodingProp);
      }

      Boolean detectEncoding = ConfigUtils.removeBoolean(props, "flyway.detectEncoding");
      if (detectEncoding != null) {
         this.setDetectEncoding(detectEncoding);
      }

      String defaultSchemaProp = (String)props.remove("flyway.defaultSchema");
      if (defaultSchemaProp != null) {
         this.setDefaultSchema(defaultSchemaProp);
      }

      String schemasProp = (String)props.remove("flyway.schemas");
      if (schemasProp != null) {
         this.setSchemas(StringUtils.tokenizeToStringArray(schemasProp, ","));
      }

      String tableProp = (String)props.remove("flyway.table");
      if (tableProp != null) {
         this.setTable(tableProp);
      }

      String tablespaceProp = (String)props.remove("flyway.tablespace");
      if (tablespaceProp != null) {
         this.setTablespace(tablespaceProp);
      }

      Boolean cleanOnValidationErrorProp = ConfigUtils.removeBoolean(props, "flyway.cleanOnValidationError");
      if (cleanOnValidationErrorProp != null) {
         this.setCleanOnValidationError(cleanOnValidationErrorProp);
      }

      Boolean cleanDisabledProp = ConfigUtils.removeBoolean(props, "flyway.cleanDisabled");
      if (cleanDisabledProp != null) {
         this.setCleanDisabled(cleanDisabledProp);
      }

      Boolean validateOnMigrateProp = ConfigUtils.removeBoolean(props, "flyway.validateOnMigrate");
      if (validateOnMigrateProp != null) {
         this.setValidateOnMigrate(validateOnMigrateProp);
      }

      String baselineVersionProp = (String)props.remove("flyway.baselineVersion");
      if (baselineVersionProp != null) {
         this.setBaselineVersion(MigrationVersion.fromVersion(baselineVersionProp));
      }

      String baselineDescriptionProp = (String)props.remove("flyway.baselineDescription");
      if (baselineDescriptionProp != null) {
         this.setBaselineDescription(baselineDescriptionProp);
      }

      Boolean baselineOnMigrateProp = ConfigUtils.removeBoolean(props, "flyway.baselineOnMigrate");
      if (baselineOnMigrateProp != null) {
         this.setBaselineOnMigrate(baselineOnMigrateProp);
      }

      Boolean ignoreMissingMigrationsProp = ConfigUtils.removeBoolean(props, "flyway.ignoreMissingMigrations");
      if (ignoreMissingMigrationsProp != null) {
         this.setIgnoreMissingMigrations(ignoreMissingMigrationsProp);
      }

      Boolean ignoreIgnoredMigrationsProp = ConfigUtils.removeBoolean(props, "flyway.ignoreIgnoredMigrations");
      if (ignoreIgnoredMigrationsProp != null) {
         this.setIgnoreIgnoredMigrations(ignoreIgnoredMigrationsProp);
      }

      Boolean ignorePendingMigrationsProp = ConfigUtils.removeBoolean(props, "flyway.ignorePendingMigrations");
      if (ignorePendingMigrationsProp != null) {
         this.setIgnorePendingMigrations(ignorePendingMigrationsProp);
      }

      Boolean ignoreFutureMigrationsProp = ConfigUtils.removeBoolean(props, "flyway.ignoreFutureMigrations");
      if (ignoreFutureMigrationsProp != null) {
         this.setIgnoreFutureMigrations(ignoreFutureMigrationsProp);
      }

      Boolean validateMigrationNamingProp = ConfigUtils.removeBoolean(props, "flyway.validateMigrationNaming");
      if (validateMigrationNamingProp != null) {
         this.setValidateMigrationNaming(validateMigrationNamingProp);
      }

      String targetProp = (String)props.remove("flyway.target");
      if (targetProp != null) {
         this.setTargetAsString(targetProp);
      }

      String cherryPickProp = (String)props.remove("flyway.cherryPick");
      if (cherryPickProp != null) {
         this.setCherryPick(StringUtils.tokenizeToStringArray(cherryPickProp, ","));
      }

      String loggersProp = (String)props.remove("flyway.loggers");
      if (loggersProp != null) {
         this.setLoggers(StringUtils.tokenizeToStringArray(loggersProp, ","));
      }

      Integer lockRetryCount = ConfigUtils.removeInteger(props, "flyway.lockRetryCount");
      if (lockRetryCount != null) {
         this.setLockRetryCount(lockRetryCount);
      }

      Boolean outOfOrderProp = ConfigUtils.removeBoolean(props, "flyway.outOfOrder");
      if (outOfOrderProp != null) {
         this.setOutOfOrder(outOfOrderProp);
      }

      Boolean skipExecutingMigrationsProp = ConfigUtils.removeBoolean(props, "flyway.skipExecutingMigrations");
      if (skipExecutingMigrationsProp != null) {
         this.setSkipExecutingMigrations(skipExecutingMigrationsProp);
      }

      Boolean outputQueryResultsProp = ConfigUtils.removeBoolean(props, "flyway.outputQueryResults");
      if (outputQueryResultsProp != null) {
         this.setOutputQueryResults(outputQueryResultsProp);
      }

      String resolversProp = (String)props.remove("flyway.resolvers");
      if (StringUtils.hasLength(resolversProp)) {
         this.setResolversAsClassNames(StringUtils.tokenizeToStringArray(resolversProp, ","));
      }

      Boolean skipDefaultResolversProp = ConfigUtils.removeBoolean(props, "flyway.skipDefaultResolvers");
      if (skipDefaultResolversProp != null) {
         this.setSkipDefaultResolvers(skipDefaultResolversProp);
      }

      String callbacksProp = (String)props.remove("flyway.callbacks");
      if (StringUtils.hasLength(callbacksProp)) {
         this.setCallbacksAsClassNames(StringUtils.tokenizeToStringArray(callbacksProp, ","));
      }

      Boolean skipDefaultCallbacksProp = ConfigUtils.removeBoolean(props, "flyway.skipDefaultCallbacks");
      if (skipDefaultCallbacksProp != null) {
         this.setSkipDefaultCallbacks(skipDefaultCallbacksProp);
      }

      Map<String, String> placeholdersFromProps = this.getPropertiesUnderNamespace(props, this.getPlaceholders(), "flyway.placeholders.");
      this.setPlaceholders(placeholdersFromProps);
      Boolean mixedProp = ConfigUtils.removeBoolean(props, "flyway.mixed");
      if (mixedProp != null) {
         this.setMixed(mixedProp);
      }

      Boolean groupProp = ConfigUtils.removeBoolean(props, "flyway.group");
      if (groupProp != null) {
         this.setGroup(groupProp);
      }

      String installedByProp = (String)props.remove("flyway.installedBy");
      if (installedByProp != null) {
         this.setInstalledBy(installedByProp);
      }

      String dryRunOutputProp = (String)props.remove("flyway.dryRunOutput");
      if (dryRunOutputProp != null) {
         this.setDryRunOutputAsFileName(dryRunOutputProp);
      }

      String errorOverridesProp = (String)props.remove("flyway.errorOverrides");
      if (errorOverridesProp != null) {
         this.setErrorOverrides(StringUtils.tokenizeToStringArray(errorOverridesProp, ","));
      }

      Boolean streamProp = ConfigUtils.removeBoolean(props, "flyway.stream");
      if (streamProp != null) {
         this.setStream(streamProp);
      }

      Boolean batchProp = ConfigUtils.removeBoolean(props, "flyway.batch");
      if (batchProp != null) {
         this.setBatch(batchProp);
      }

      Boolean oracleSqlplusProp = ConfigUtils.removeBoolean(props, "flyway.oracle.sqlplus");
      if (oracleSqlplusProp != null) {
         this.setOracleSqlplus(oracleSqlplusProp);
      }

      Boolean oracleSqlplusWarnProp = ConfigUtils.removeBoolean(props, "flyway.oracle.sqlplusWarn");
      if (oracleSqlplusWarnProp != null) {
         this.setOracleSqlplusWarn(oracleSqlplusWarnProp);
      }

      Boolean createSchemasProp = ConfigUtils.removeBoolean(props, "flyway.createSchemas");
      if (createSchemasProp != null) {
         this.setShouldCreateSchemas(createSchemasProp);
      }

      String kerberosConfigFile = (String)props.remove("flyway.kerberosConfigFile");
      if (kerberosConfigFile != null) {
         this.setKerberosConfigFile(kerberosConfigFile);
      }

      String oracleKerberosConfigFile = (String)props.remove("flyway.oracle.kerberosConfigFile");
      if (oracleKerberosConfigFile != null) {
         this.setOracleKerberosConfigFile(oracleKerberosConfigFile);
      }

      String oracleKerberosCacheFile = (String)props.remove("flyway.oracle.kerberosCacheFile");
      if (oracleKerberosCacheFile != null) {
         this.setOracleKerberosCacheFile(oracleKerberosCacheFile);
      }

      String oracleWalletLocationProp = (String)props.remove("flyway.oracle.walletLocation");
      if (oracleWalletLocationProp != null) {
         this.setOracleWalletLocation(oracleWalletLocationProp);
      }

      String ignoreMigrationPatternsProp = (String)props.remove("flyway.ignoreMigrationPatterns");
      if (ignoreMigrationPatternsProp != null) {
         this.setIgnoreMigrationPatterns(StringUtils.tokenizeToStringArray(ignoreMigrationPatternsProp, ","));
      }

      String licenseKeyProp = (String)props.remove("flyway.licenseKey");
      if (licenseKeyProp != null) {
         this.setLicenseKey(licenseKeyProp);
      }

      Boolean failOnMissingLocationsProp = ConfigUtils.removeBoolean(props, "flyway.failOnMissingLocations");
      if (failOnMissingLocationsProp != null) {
         this.setFailOnMissingLocations(failOnMissingLocationsProp);
      }

      if (StringUtils.hasText(this.url)
         && (StringUtils.hasText(urlProp) || StringUtils.hasText(driverProp) || StringUtils.hasText(userProp) || StringUtils.hasText(passwordProp))) {
         Map<String, String> jdbcPropertiesFromProps = this.getPropertiesUnderNamespace(props, this.getPlaceholders(), "flyway.jdbcProperties.");
         this.setDataSource(new DriverDataSource(this.classLoader, this.driver, this.url, this.user, this.password, this, jdbcPropertiesFromProps));
      }

      ConfigUtils.checkConfigurationForUnrecognisedProperties(props, "flyway.");
   }

   private void configureFromConfigurationProviders(ClassicConfiguration configuration) {
      Map<String, String> config = new HashMap();

      for(ConfigurationProvider configurationProvider : PluginRegister.getPlugins(ConfigurationProvider.class)) {
         ConfigurationExtension configurationExtension = PluginRegister.getPlugin(configurationProvider.getConfigurationExtensionClass());

         try {
            config.putAll(configurationProvider.getConfiguration(configurationExtension, configuration));
         } catch (Exception var7) {
            throw new FlywayException("Unable to read configuration from " + configurationProvider.getClass().getName() + ": " + var7.getMessage());
         }
      }

      this.configure(config);
   }

   private Map<String, String> getPropertiesUnderNamespace(Map<String, String> properties, Map<String, String> current, String namespace) {
      Iterator<Entry<String, String>> iterator = properties.entrySet().iterator();

      while(iterator.hasNext()) {
         Entry<String, String> entry = (Entry)iterator.next();
         String propertyName = (String)entry.getKey();
         if (propertyName.startsWith(namespace) && propertyName.length() > namespace.length()) {
            String placeholderName = propertyName.substring(namespace.length());
            String placeholderValue = (String)entry.getValue();
            current.put(placeholderName, placeholderValue);
            iterator.remove();
         }
      }

      return current;
   }

   public void configureUsingEnvVars() {
      this.configure(ConfigUtils.environmentVariablesToPropertyMap());
   }

   @Override
   public String getUrl() {
      return this.url;
   }

   @Override
   public String getUser() {
      return this.user;
   }

   @Override
   public String getPassword() {
      return this.password;
   }

   @Override
   public int getConnectRetries() {
      return this.connectRetries;
   }

   @Override
   public int getConnectRetriesInterval() {
      return this.connectRetriesInterval;
   }

   @Override
   public String getInitSql() {
      return this.initSql;
   }

   @Override
   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   @Override
   public Charset getEncoding() {
      return this.encoding;
   }

   @Override
   public boolean isDetectEncoding() {
      return this.detectEncoding;
   }

   @Override
   public String getDefaultSchema() {
      return this.defaultSchema;
   }

   @Override
   public String[] getSchemas() {
      return this.schemas;
   }

   @Override
   public String getTable() {
      return this.table;
   }

   @Override
   public String getTablespace() {
      return this.tablespace;
   }

   @Override
   public MigrationVersion getTarget() {
      return this.target;
   }

   @Override
   public boolean isFailOnMissingTarget() {
      return this.failOnMissingTarget;
   }

   @Override
   public MigrationPattern[] getCherryPick() {
      return this.cherryPick;
   }

   @Override
   public boolean isPlaceholderReplacement() {
      return this.placeholderReplacement;
   }

   @Override
   public Map<String, String> getPlaceholders() {
      return this.placeholders;
   }

   @Override
   public String getPlaceholderPrefix() {
      return this.placeholderPrefix;
   }

   @Override
   public String getPlaceholderSuffix() {
      return this.placeholderSuffix;
   }

   @Override
   public String getPlaceholderSeparator() {
      return this.placeholderSeparator;
   }

   @Override
   public String getScriptPlaceholderPrefix() {
      return this.scriptPlaceholderPrefix;
   }

   @Override
   public String getScriptPlaceholderSuffix() {
      return this.scriptPlaceholderSuffix;
   }

   @Override
   public String getSqlMigrationPrefix() {
      return this.sqlMigrationPrefix;
   }

   @Override
   public String getBaselineMigrationPrefix() {
      return this.baselineMigrationPrefix;
   }

   @Override
   public String getUndoSqlMigrationPrefix() {
      return this.undoSqlMigrationPrefix;
   }

   @Override
   public String getRepeatableSqlMigrationPrefix() {
      return this.repeatableSqlMigrationPrefix;
   }

   @Override
   public ResourceProvider getResourceProvider() {
      return this.resourceProvider;
   }

   @Override
   public ClassProvider<JavaMigration> getJavaMigrationClassProvider() {
      return this.javaMigrationClassProvider;
   }

   @Override
   public String getSqlMigrationSeparator() {
      return this.sqlMigrationSeparator;
   }

   @Override
   public String[] getSqlMigrationSuffixes() {
      return this.sqlMigrationSuffixes;
   }

   @Override
   public JavaMigration[] getJavaMigrations() {
      return this.javaMigrations;
   }

   @Deprecated
   @Override
   public boolean isIgnoreMissingMigrations() {
      return this.ignoreMissingMigrations;
   }

   @Deprecated
   @Override
   public boolean isIgnoreIgnoredMigrations() {
      return this.ignoreIgnoredMigrations;
   }

   @Deprecated
   @Override
   public boolean isIgnorePendingMigrations() {
      return this.ignorePendingMigrations;
   }

   @Deprecated
   @Override
   public boolean isIgnoreFutureMigrations() {
      return this.ignoreFutureMigrations;
   }

   @Override
   public ValidatePattern[] getIgnoreMigrationPatterns() {
      return this.ignoreMigrationPatterns;
   }

   @Override
   public boolean isValidateMigrationNaming() {
      return this.validateMigrationNaming;
   }

   @Override
   public boolean isValidateOnMigrate() {
      return this.validateOnMigrate;
   }

   @Override
   public boolean isCleanOnValidationError() {
      return this.cleanOnValidationError;
   }

   @Override
   public boolean isCleanDisabled() {
      return this.cleanDisabled;
   }

   @Override
   public MigrationVersion getBaselineVersion() {
      return this.baselineVersion;
   }

   @Override
   public String getBaselineDescription() {
      return this.baselineDescription;
   }

   @Override
   public boolean isBaselineOnMigrate() {
      return this.baselineOnMigrate;
   }

   @Override
   public boolean isOutOfOrder() {
      return this.outOfOrder;
   }

   @Override
   public boolean isSkipExecutingMigrations() {
      return this.skipExecutingMigrations;
   }

   @Override
   public boolean isSkipDefaultCallbacks() {
      return this.skipDefaultCallbacks;
   }

   @Override
   public MigrationResolver[] getResolvers() {
      return this.resolvers;
   }

   @Override
   public boolean isSkipDefaultResolvers() {
      return this.skipDefaultResolvers;
   }

   @Override
   public boolean isMixed() {
      return this.mixed;
   }

   @Override
   public boolean isGroup() {
      return this.group;
   }

   @Override
   public String getInstalledBy() {
      return this.installedBy;
   }

   @Override
   public boolean isCreateSchemas() {
      return this.createSchemas;
   }

   @Override
   public String[] getErrorOverrides() {
      return this.errorOverrides;
   }

   @Override
   public OutputStream getDryRunOutput() {
      return this.dryRunOutput;
   }

   @Override
   public boolean isStream() {
      return this.stream;
   }

   @Override
   public boolean isBatch() {
      return this.batch;
   }

   @Override
   public boolean isOutputQueryResults() {
      return this.outputQueryResults;
   }

   @Override
   public String getLicenseKey() {
      return this.licenseKey;
   }

   @Override
   public int getLockRetryCount() {
      return this.lockRetryCount;
   }

   @Override
   public Map<String, String> getJdbcProperties() {
      return this.jdbcProperties;
   }

   @Override
   public boolean isOracleSqlplus() {
      return this.oracleSqlplus;
   }

   @Override
   public boolean isOracleSqlplusWarn() {
      return this.oracleSqlplusWarn;
   }

   @Override
   public String getKerberosConfigFile() {
      return this.kerberosConfigFile;
   }

   @Override
   public String getOracleKerberosConfigFile() {
      return this.oracleKerberosConfigFile;
   }

   @Override
   public String getOracleKerberosCacheFile() {
      return this.oracleKerberosCacheFile;
   }

   @Override
   public String getOracleWalletLocation() {
      return this.oracleWalletLocation;
   }

   @Override
   public boolean isFailOnMissingLocations() {
      return this.failOnMissingLocations;
   }

   @Override
   public String[] getLoggers() {
      return this.loggers;
   }

   public void setDriver(String driver) {
      this.driver = driver;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public void setInitSql(String initSql) {
      this.initSql = initSql;
   }

   public void setClassLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   public void setEncoding(Charset encoding) {
      this.encoding = encoding;
   }

   public void setDefaultSchema(String defaultSchema) {
      this.defaultSchema = defaultSchema;
   }

   public void setSchemas(String[] schemas) {
      this.schemas = schemas;
   }

   public void setTable(String table) {
      this.table = table;
   }

   public void setTablespace(String tablespace) {
      this.tablespace = tablespace;
   }

   public void setTarget(MigrationVersion target) {
      this.target = target;
   }

   public void setFailOnMissingTarget(boolean failOnMissingTarget) {
      this.failOnMissingTarget = failOnMissingTarget;
   }

   public void setPlaceholderReplacement(boolean placeholderReplacement) {
      this.placeholderReplacement = placeholderReplacement;
   }

   public void setPlaceholders(Map<String, String> placeholders) {
      this.placeholders = placeholders;
   }

   public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
      this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
   }

   public void setResourceProvider(ResourceProvider resourceProvider) {
      this.resourceProvider = resourceProvider;
   }

   public void setJavaMigrationClassProvider(ClassProvider<JavaMigration> javaMigrationClassProvider) {
      this.javaMigrationClassProvider = javaMigrationClassProvider;
   }

   @Deprecated
   public void setIgnoreMissingMigrations(boolean ignoreMissingMigrations) {
      this.ignoreMissingMigrations = ignoreMissingMigrations;
   }

   @Deprecated
   public void setIgnoreIgnoredMigrations(boolean ignoreIgnoredMigrations) {
      this.ignoreIgnoredMigrations = ignoreIgnoredMigrations;
   }

   @Deprecated
   public void setIgnorePendingMigrations(boolean ignorePendingMigrations) {
      this.ignorePendingMigrations = ignorePendingMigrations;
   }

   @Deprecated
   public void setIgnoreFutureMigrations(boolean ignoreFutureMigrations) {
      this.ignoreFutureMigrations = ignoreFutureMigrations;
   }

   public void setValidateMigrationNaming(boolean validateMigrationNaming) {
      this.validateMigrationNaming = validateMigrationNaming;
   }

   public void setValidateOnMigrate(boolean validateOnMigrate) {
      this.validateOnMigrate = validateOnMigrate;
   }

   public void setCleanOnValidationError(boolean cleanOnValidationError) {
      this.cleanOnValidationError = cleanOnValidationError;
   }

   public void setCleanDisabled(boolean cleanDisabled) {
      this.cleanDisabled = cleanDisabled;
   }

   public void setBaselineVersion(MigrationVersion baselineVersion) {
      this.baselineVersion = baselineVersion;
   }

   public void setBaselineDescription(String baselineDescription) {
      this.baselineDescription = baselineDescription;
   }

   public void setBaselineOnMigrate(boolean baselineOnMigrate) {
      this.baselineOnMigrate = baselineOnMigrate;
   }

   public void setOutOfOrder(boolean outOfOrder) {
      this.outOfOrder = outOfOrder;
   }

   public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks) {
      this.skipDefaultCallbacks = skipDefaultCallbacks;
   }

   public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
      this.skipDefaultResolvers = skipDefaultResolvers;
   }

   public void setMixed(boolean mixed) {
      this.mixed = mixed;
   }

   public void setGroup(boolean group) {
      this.group = group;
   }

   public void setCreateSchemas(boolean createSchemas) {
      this.createSchemas = createSchemas;
   }

   public void setLockRetryCount(int lockRetryCount) {
      this.lockRetryCount = lockRetryCount;
   }

   public void setFailOnMissingLocations(boolean failOnMissingLocations) {
      this.failOnMissingLocations = failOnMissingLocations;
   }
}
