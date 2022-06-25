package org.flywaydb.core.internal.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.StringUtils;

public class ConfigUtils {
   private static final Log LOG = LogFactory.getLog(ConfigUtils.class);
   public static final String CONFIG_FILE_NAME = "flyway.conf";
   public static final String CONFIG_FILES = "flyway.configFiles";
   public static final String CONFIG_FILE_ENCODING = "flyway.configFileEncoding";
   public static final String BASELINE_DESCRIPTION = "flyway.baselineDescription";
   public static final String BASELINE_ON_MIGRATE = "flyway.baselineOnMigrate";
   public static final String BASELINE_VERSION = "flyway.baselineVersion";
   public static final String BATCH = "flyway.batch";
   public static final String CALLBACKS = "flyway.callbacks";
   public static final String CLEAN_DISABLED = "flyway.cleanDisabled";
   public static final String CLEAN_ON_VALIDATION_ERROR = "flyway.cleanOnValidationError";
   public static final String CONNECT_RETRIES = "flyway.connectRetries";
   public static final String CONNECT_RETRIES_INTERVAL = "flyway.connectRetriesInterval";
   public static final String DEFAULT_SCHEMA = "flyway.defaultSchema";
   public static final String DRIVER = "flyway.driver";
   public static final String DRYRUN_OUTPUT = "flyway.dryRunOutput";
   public static final String ENCODING = "flyway.encoding";
   public static final String DETECT_ENCODING = "flyway.detectEncoding";
   public static final String ERROR_OVERRIDES = "flyway.errorOverrides";
   public static final String GROUP = "flyway.group";
   public static final String IGNORE_FUTURE_MIGRATIONS = "flyway.ignoreFutureMigrations";
   public static final String IGNORE_MISSING_MIGRATIONS = "flyway.ignoreMissingMigrations";
   public static final String IGNORE_IGNORED_MIGRATIONS = "flyway.ignoreIgnoredMigrations";
   public static final String IGNORE_PENDING_MIGRATIONS = "flyway.ignorePendingMigrations";
   public static final String IGNORE_MIGRATION_PATTERNS = "flyway.ignoreMigrationPatterns";
   public static final String INIT_SQL = "flyway.initSql";
   public static final String INSTALLED_BY = "flyway.installedBy";
   public static final String LICENSE_KEY = "flyway.licenseKey";
   public static final String LOCATIONS = "flyway.locations";
   public static final String MIXED = "flyway.mixed";
   public static final String OUT_OF_ORDER = "flyway.outOfOrder";
   public static final String SKIP_EXECUTING_MIGRATIONS = "flyway.skipExecutingMigrations";
   public static final String OUTPUT_QUERY_RESULTS = "flyway.outputQueryResults";
   public static final String PASSWORD = "flyway.password";
   public static final String PLACEHOLDER_PREFIX = "flyway.placeholderPrefix";
   public static final String PLACEHOLDER_REPLACEMENT = "flyway.placeholderReplacement";
   public static final String PLACEHOLDER_SUFFIX = "flyway.placeholderSuffix";
   public static final String PLACEHOLDER_SEPARATOR = "flyway.placeholderSeparator";
   public static final String SCRIPT_PLACEHOLDER_PREFIX = "flyway.scriptPlaceholderPrefix";
   public static final String SCRIPT_PLACEHOLDER_SUFFIX = "flyway.scriptPlaceholderSuffix";
   public static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";
   public static final String LOCK_RETRY_COUNT = "flyway.lockRetryCount";
   public static final String JDBC_PROPERTIES_PREFIX = "flyway.jdbcProperties.";
   public static final String REPEATABLE_SQL_MIGRATION_PREFIX = "flyway.repeatableSqlMigrationPrefix";
   public static final String RESOLVERS = "flyway.resolvers";
   public static final String SCHEMAS = "flyway.schemas";
   public static final String SKIP_DEFAULT_CALLBACKS = "flyway.skipDefaultCallbacks";
   public static final String SKIP_DEFAULT_RESOLVERS = "flyway.skipDefaultResolvers";
   public static final String SQL_MIGRATION_PREFIX = "flyway.sqlMigrationPrefix";
   public static final String SQL_MIGRATION_SEPARATOR = "flyway.sqlMigrationSeparator";
   public static final String SQL_MIGRATION_SUFFIXES = "flyway.sqlMigrationSuffixes";
   public static final String BASELINE_MIGRATION_PREFIX = "flyway.baselineMigrationPrefix";
   public static final String STREAM = "flyway.stream";
   public static final String TABLE = "flyway.table";
   public static final String TABLESPACE = "flyway.tablespace";
   public static final String TARGET = "flyway.target";
   public static final String CHERRY_PICK = "flyway.cherryPick";
   public static final String UNDO_SQL_MIGRATION_PREFIX = "flyway.undoSqlMigrationPrefix";
   public static final String URL = "flyway.url";
   public static final String USER = "flyway.user";
   public static final String VALIDATE_ON_MIGRATE = "flyway.validateOnMigrate";
   public static final String VALIDATE_MIGRATION_NAMING = "flyway.validateMigrationNaming";
   public static final String CREATE_SCHEMAS = "flyway.createSchemas";
   public static final String FAIL_ON_MISSING_LOCATIONS = "flyway.failOnMissingLocations";
   public static final String LOGGERS = "flyway.loggers";
   public static final String KERBEROS_CONFIG_FILE = "flyway.kerberosConfigFile";
   public static final String ORACLE_SQLPLUS = "flyway.oracle.sqlplus";
   public static final String ORACLE_SQLPLUS_WARN = "flyway.oracle.sqlplusWarn";
   public static final String ORACLE_KERBEROS_CONFIG_FILE = "flyway.oracle.kerberosConfigFile";
   public static final String ORACLE_KERBEROS_CACHE_FILE = "flyway.oracle.kerberosCacheFile";
   public static final String ORACLE_WALLET_LOCATION = "flyway.oracle.walletLocation";
   public static final String JAR_DIRS = "flyway.jarDirs";
   public static final String CONFIGURATIONS = "flyway.configurations";
   public static final String FLYWAY_PLUGINS_PREFIX = "flyway.plugins.";

   public static Map<String, String> environmentVariablesToPropertyMap() {
      Map<String, String> result = new HashMap();

      for(Entry<String, String> entry : System.getenv().entrySet()) {
         String convertedKey = convertKey((String)entry.getKey());
         if (convertedKey != null) {
            result.put(convertKey((String)entry.getKey()), (String)entry.getValue());
         }
      }

      return result;
   }

   private static String convertKey(String key) {
      if ("FLYWAY_BASELINE_DESCRIPTION".equals(key)) {
         return "flyway.baselineDescription";
      } else if ("FLYWAY_BASELINE_ON_MIGRATE".equals(key)) {
         return "flyway.baselineOnMigrate";
      } else if ("FLYWAY_BASELINE_VERSION".equals(key)) {
         return "flyway.baselineVersion";
      } else if ("FLYWAY_BATCH".equals(key)) {
         return "flyway.batch";
      } else if ("FLYWAY_CALLBACKS".equals(key)) {
         return "flyway.callbacks";
      } else if ("FLYWAY_CLEAN_DISABLED".equals(key)) {
         return "flyway.cleanDisabled";
      } else if ("FLYWAY_CLEAN_ON_VALIDATION_ERROR".equals(key)) {
         return "flyway.cleanOnValidationError";
      } else if ("FLYWAY_CONFIG_FILE_ENCODING".equals(key)) {
         return "flyway.configFileEncoding";
      } else if ("FLYWAY_CONFIG_FILES".equals(key)) {
         return "flyway.configFiles";
      } else if ("FLYWAY_CONNECT_RETRIES".equals(key)) {
         return "flyway.connectRetries";
      } else if ("FLYWAY_CONNECT_RETRIES_INTERVAL".equals(key)) {
         return "flyway.connectRetriesInterval";
      } else if ("FLYWAY_DEFAULT_SCHEMA".equals(key)) {
         return "flyway.defaultSchema";
      } else if ("FLYWAY_DRIVER".equals(key)) {
         return "flyway.driver";
      } else if ("FLYWAY_DRYRUN_OUTPUT".equals(key)) {
         return "flyway.dryRunOutput";
      } else if ("FLYWAY_ENCODING".equals(key)) {
         return "flyway.encoding";
      } else if ("FLYWAY_DETECT_ENCODING".equals(key)) {
         return "flyway.detectEncoding";
      } else if ("FLYWAY_ERROR_OVERRIDES".equals(key)) {
         return "flyway.errorOverrides";
      } else if ("FLYWAY_GROUP".equals(key)) {
         return "flyway.group";
      } else if ("FLYWAY_IGNORE_FUTURE_MIGRATIONS".equals(key)) {
         return "flyway.ignoreFutureMigrations";
      } else if ("FLYWAY_IGNORE_MISSING_MIGRATIONS".equals(key)) {
         return "flyway.ignoreMissingMigrations";
      } else if ("FLYWAY_IGNORE_IGNORED_MIGRATIONS".equals(key)) {
         return "flyway.ignoreIgnoredMigrations";
      } else if ("FLYWAY_IGNORE_PENDING_MIGRATIONS".equals(key)) {
         return "flyway.ignorePendingMigrations";
      } else if ("FLYWAY_IGNORE_MIGRATION_PATTERNS".equals(key)) {
         return "flyway.ignoreMigrationPatterns";
      } else if ("FLYWAY_INIT_SQL".equals(key)) {
         return "flyway.initSql";
      } else if ("FLYWAY_INSTALLED_BY".equals(key)) {
         return "flyway.installedBy";
      } else if ("FLYWAY_LICENSE_KEY".equals(key)) {
         return "flyway.licenseKey";
      } else if ("FLYWAY_LOCATIONS".equals(key)) {
         return "flyway.locations";
      } else if ("FLYWAY_MIXED".equals(key)) {
         return "flyway.mixed";
      } else if ("FLYWAY_OUT_OF_ORDER".equals(key)) {
         return "flyway.outOfOrder";
      } else if ("FLYWAY_SKIP_EXECUTING_MIGRATIONS".equals(key)) {
         return "flyway.skipExecutingMigrations";
      } else if ("FLYWAY_OUTPUT_QUERY_RESULTS".equals(key)) {
         return "flyway.outputQueryResults";
      } else if ("FLYWAY_PASSWORD".equals(key)) {
         return "flyway.password";
      } else if ("FLYWAY_LOCK_RETRY_COUNT".equals(key)) {
         return "flyway.lockRetryCount";
      } else if ("FLYWAY_PLACEHOLDER_PREFIX".equals(key)) {
         return "flyway.placeholderPrefix";
      } else if ("FLYWAY_PLACEHOLDER_REPLACEMENT".equals(key)) {
         return "flyway.placeholderReplacement";
      } else if ("FLYWAY_PLACEHOLDER_SUFFIX".equals(key)) {
         return "flyway.placeholderSuffix";
      } else if ("FLYWAY_PLACEHOLDER_SEPARATOR".equals(key)) {
         return "flyway.placeholderSeparator";
      } else if ("FLYWAY_SCRIPT_PLACEHOLDER_PREFIX".equals(key)) {
         return "flyway.scriptPlaceholderPrefix";
      } else if ("FLYWAY_SCRIPT_PLACEHOLDER_SUFFIX".equals(key)) {
         return "flyway.scriptPlaceholderSuffix";
      } else if (key.matches("FLYWAY_PLACEHOLDERS_.+")) {
         return "flyway.placeholders." + key.substring("FLYWAY_PLACEHOLDERS_".length()).toLowerCase(Locale.ENGLISH);
      } else if (key.matches("FLYWAY_JDBC_PROPERTIES_.+")) {
         return "flyway.jdbcProperties." + key.substring("FLYWAY_JDBC_PROPERTIES_".length());
      } else if ("FLYWAY_REPEATABLE_SQL_MIGRATION_PREFIX".equals(key)) {
         return "flyway.repeatableSqlMigrationPrefix";
      } else if ("FLYWAY_RESOLVERS".equals(key)) {
         return "flyway.resolvers";
      } else if ("FLYWAY_SCHEMAS".equals(key)) {
         return "flyway.schemas";
      } else if ("FLYWAY_SKIP_DEFAULT_CALLBACKS".equals(key)) {
         return "flyway.skipDefaultCallbacks";
      } else if ("FLYWAY_SKIP_DEFAULT_RESOLVERS".equals(key)) {
         return "flyway.skipDefaultResolvers";
      } else if ("FLYWAY_SQL_MIGRATION_PREFIX".equals(key)) {
         return "flyway.sqlMigrationPrefix";
      } else if ("FLYWAY_SQL_MIGRATION_SEPARATOR".equals(key)) {
         return "flyway.sqlMigrationSeparator";
      } else if ("FLYWAY_SQL_MIGRATION_SUFFIXES".equals(key)) {
         return "flyway.sqlMigrationSuffixes";
      } else if ("FLYWAY_BASELINE_MIGRATION_PREFIX".equals(key)) {
         return "flyway.baselineMigrationPrefix";
      } else if ("FLYWAY_STREAM".equals(key)) {
         return "flyway.stream";
      } else if ("FLYWAY_TABLE".equals(key)) {
         return "flyway.table";
      } else if ("FLYWAY_TABLESPACE".equals(key)) {
         return "flyway.tablespace";
      } else if ("FLYWAY_TARGET".equals(key)) {
         return "flyway.target";
      } else if ("FLYWAY_CHERRY_PICK".equals(key)) {
         return "flyway.cherryPick";
      } else if ("FLYWAY_LOGGERS".equals(key)) {
         return "flyway.loggers";
      } else if ("FLYWAY_UNDO_SQL_MIGRATION_PREFIX".equals(key)) {
         return "flyway.undoSqlMigrationPrefix";
      } else if ("FLYWAY_URL".equals(key)) {
         return "flyway.url";
      } else if ("FLYWAY_USER".equals(key)) {
         return "flyway.user";
      } else if ("FLYWAY_VALIDATE_ON_MIGRATE".equals(key)) {
         return "flyway.validateOnMigrate";
      } else if ("FLYWAY_VALIDATE_MIGRATION_NAMING".equals(key)) {
         return "flyway.validateMigrationNaming";
      } else if ("FLYWAY_CREATE_SCHEMAS".equals(key)) {
         return "flyway.createSchemas";
      } else if ("FLYWAY_FAIL_ON_MISSING_LOCATIONS".equals(key)) {
         return "flyway.failOnMissingLocations";
      } else if ("FLYWAY_KERBEROS_CONFIG_FILE".equals(key)) {
         return "flyway.kerberosConfigFile";
      } else if ("FLYWAY_ORACLE_SQLPLUS".equals(key)) {
         return "flyway.oracle.sqlplus";
      } else if ("FLYWAY_ORACLE_SQLPLUS_WARN".equals(key)) {
         return "flyway.oracle.sqlplusWarn";
      } else if ("FLYWAY_ORACLE_KERBEROS_CONFIG_FILE".equals(key)) {
         return "flyway.oracle.kerberosConfigFile";
      } else if ("FLYWAY_ORACLE_KERBEROS_CACHE_FILE".equals(key)) {
         return "flyway.oracle.kerberosCacheFile";
      } else if ("FLYWAY_ORACLE_WALLET_LOCATION".equals(key)) {
         return "flyway.oracle.walletLocation";
      } else if ("FLYWAY_JAR_DIRS".equals(key)) {
         return "flyway.jarDirs";
      } else if ("FLYWAY_CONFIGURATIONS".equals(key)) {
         return "flyway.configurations";
      } else {
         for(ConfigurationExtension configurationExtension : PluginRegister.getPlugins(ConfigurationExtension.class)) {
            String configurationParameter = configurationExtension.getConfigurationParameterFromEnvironmentVariable(key);
            if (configurationParameter != null) {
               return configurationParameter;
            }
         }

         return null;
      }
   }

   public static Map<String, String> loadDefaultConfigurationFiles(File installationDir, String encoding) {
      Map<String, String> configMap = new HashMap();
      configMap.putAll(loadConfigurationFile(new File(installationDir.getAbsolutePath() + "/conf/" + "flyway.conf"), encoding, false));
      configMap.putAll(loadConfigurationFile(new File(System.getProperty("user.home") + "/" + "flyway.conf"), encoding, false));
      configMap.putAll(loadConfigurationFile(new File("flyway.conf"), encoding, false));
      return configMap;
   }

   public static Map<String, String> loadConfigurationFile(File configFile, String encoding, boolean failIfMissing) throws FlywayException {
      String errorMessage = "Unable to load config file: " + configFile.getAbsolutePath();
      if ("-".equals(configFile.getName())) {
         return loadConfigurationFromInputStream(System.in);
      } else if (configFile.isFile() && configFile.canRead()) {
         LOG.debug("Loading config file: " + configFile.getAbsolutePath());

         try {
            return loadConfigurationFromReader(new InputStreamReader(new FileInputStream(configFile), encoding));
         } catch (FlywayException | IOException var5) {
            throw new FlywayException(errorMessage, var5);
         }
      } else if (!failIfMissing) {
         LOG.debug(errorMessage);
         return new HashMap();
      } else {
         throw new FlywayException(errorMessage);
      }
   }

   public static Map<String, String> loadConfigurationFromInputStream(InputStream inputStream) {
      Map<String, String> config = new HashMap();

      try {
         if (inputStream != null && inputStream.available() > 0) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            LOG.debug("Attempting to load configuration from standard input");
            int firstCharacter = bufferedReader.read();
            if (bufferedReader.ready() && firstCharacter != -1) {
               String configurationString = (char)firstCharacter + FileCopyUtils.copyToString(bufferedReader);
               Map<String, String> configurationFromStandardInput = loadConfigurationFromString(configurationString);
               if (configurationFromStandardInput.isEmpty()) {
                  LOG.debug("Empty configuration provided from standard input");
               } else {
                  LOG.info("Loaded configuration from standard input");
                  config.putAll(configurationFromStandardInput);
               }
            } else {
               LOG.debug("Could not load configuration from standard input");
            }
         }
      } catch (Exception var6) {
         LOG.debug("Could not load configuration from standard input " + var6.getMessage());
      }

      return config;
   }

   public static Map<String, String> loadConfigurationFromReader(Reader reader) throws FlywayException {
      try {
         String contents = FileCopyUtils.copyToString(reader);
         return loadConfigurationFromString(contents);
      } catch (IOException var2) {
         throw new FlywayException("Unable to read config", var2);
      }
   }

   public static Map<String, String> loadConfigurationFromString(String configuration) throws IOException {
      String[] lines = configuration.replace("\r\n", "\n").split("\n");
      StringBuilder confBuilder = new StringBuilder();

      for(int i = 0; i < lines.length; ++i) {
         String replacedLine = lines[i].trim().replace("\\", "\\\\");
         if (replacedLine.endsWith("\\\\") && i < lines.length - 1) {
            String nextLine = lines[i + 1];
            boolean restoreMultilineDelimiter = false;
            if (!nextLine.isEmpty()) {
               if (!nextLine.trim().startsWith("flyway.") || !nextLine.contains("=")) {
                  restoreMultilineDelimiter = true;
               } else if (SqlScriptMetadata.isMultilineBooleanExpression(nextLine)) {
                  restoreMultilineDelimiter = true;
               }
            }

            if (restoreMultilineDelimiter) {
               replacedLine = replacedLine.substring(0, replacedLine.length() - 2) + "\\";
            }
         }

         confBuilder.append(replacedLine).append("\n");
      }

      String contents = confBuilder.toString();
      Properties properties = new Properties();
      contents = expandEnvironmentVariables(contents, System.getenv());
      properties.load(new StringReader(contents));
      return propertiesToMap(properties);
   }

   static String expandEnvironmentVariables(String value, Map<String, String> environmentVariables) {
      Pattern pattern = Pattern.compile("\\$\\{([A-Za-z0-9_]+)}");
      Matcher matcher = pattern.matcher(value);

      String expandedValue;
      String variableValue;
      for(expandedValue = value;
         matcher.find();
         expandedValue = expandedValue.replaceAll(Pattern.quote(matcher.group(0)), Matcher.quoteReplacement(variableValue))
      ) {
         String variableName = matcher.group(1);
         variableValue = (String)environmentVariables.getOrDefault(variableName, "");
         LOG.debug("Expanding environment variable in config: " + variableName + " -> " + variableValue);
      }

      return expandedValue;
   }

   public static Map<String, String> propertiesToMap(Properties properties) {
      Map<String, String> props = new HashMap();

      for(Entry<Object, Object> entry : properties.entrySet()) {
         props.put(entry.getKey().toString(), entry.getValue().toString());
      }

      return props;
   }

   public static void putIfSet(Map<String, String> config, String key, Object... values) {
      for(Object value : values) {
         if (value != null) {
            config.put(key, value.toString());
            return;
         }
      }

   }

   public static void putArrayIfSet(Map<String, String> config, String key, String[]... values) {
      for(String[] value : values) {
         if (value != null) {
            config.put(key, StringUtils.arrayToCommaDelimitedString(value));
            return;
         }
      }

   }

   public static Boolean removeBoolean(Map<String, String> config, String key) {
      String value = (String)config.remove(key);
      if (value == null) {
         return null;
      } else if (!"true".equals(value) && !"false".equals(value)) {
         throw new FlywayException("Invalid value for " + key + " (should be either true or false): " + value, ErrorCode.CONFIGURATION);
      } else {
         return Boolean.valueOf(value);
      }
   }

   public static Integer removeInteger(Map<String, String> config, String key) {
      String value = (String)config.remove(key);
      if (value == null) {
         return null;
      } else {
         try {
            return Integer.valueOf(value);
         } catch (NumberFormatException var4) {
            throw new FlywayException("Invalid value for " + key + " (should be an integer): " + value, ErrorCode.CONFIGURATION);
         }
      }
   }

   public static void dumpConfiguration(Map<String, String> config) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("Using configuration:");

         for(Entry<String, String> entry : new TreeMap(config).entrySet()) {
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key.toLowerCase().endsWith("password")) {
               value = StringUtils.trimOrPad("", value.length(), '*');
            } else if ("flyway.licenseKey".equals(key)) {
               value = value.substring(0, 8) + "******" + value.substring(value.length() - 4);
            } else if ("flyway.url".equals(key)) {
               value = DatabaseTypeRegister.redactJdbcUrl(value);
            }

            LOG.debug(key + " -> " + value);
         }
      }

   }

   public static void checkConfigurationForUnrecognisedProperties(Map<String, String> config, String prefix) {
      ArrayList<String> unknownFlywayProperties = new ArrayList();

      for(String key : config.keySet()) {
         if (prefix == null || key.startsWith(prefix) && !key.startsWith("flyway.plugins.")) {
            unknownFlywayProperties.add(key);
         }
      }

      if (!unknownFlywayProperties.isEmpty()) {
         String property = unknownFlywayProperties.size() == 1 ? "property" : "properties";
         String message = String.format("Unknown configuration %s: %s", property, StringUtils.arrayToCommaDelimitedString(unknownFlywayProperties.toArray()));
         throw new FlywayException(message, ErrorCode.CONFIGURATION);
      }
   }

   private ConfigUtils() {
   }
}
