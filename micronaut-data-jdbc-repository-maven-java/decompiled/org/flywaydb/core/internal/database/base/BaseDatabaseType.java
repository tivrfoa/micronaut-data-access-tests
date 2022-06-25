package org.flywaydb.core.internal.database.base;

import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DefaultExecutionStrategy;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.jdbc.TransactionalExecutionTemplate;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.DefaultSqlScriptExecutor;
import org.flywaydb.core.internal.sqlscript.ParserSqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;

public abstract class BaseDatabaseType implements DatabaseType {
   private static final Log LOG = LogFactory.getLog(BaseDatabaseType.class);
   private static final Pattern defaultJdbcCredentialsPattern = Pattern.compile("password=([^;&]*).*", 2);
   protected static final String APPLICATION_NAME = "Flyway by Redgate";

   @Override
   public abstract String getName();

   public String toString() {
      return this.getName();
   }

   @Override
   public abstract int getNullType();

   @Override
   public abstract boolean handlesJDBCUrl(String var1);

   @Override
   public int getPriority() {
      return 0;
   }

   @Override
   public int compareTo(DatabaseType other) {
      return other.getPriority() - this.getPriority();
   }

   @Override
   public Pattern getJDBCCredentialsPattern() {
      return defaultJdbcCredentialsPattern;
   }

   public static Pattern getDefaultJDBCCredentialsPattern() {
      return defaultJdbcCredentialsPattern;
   }

   @Override
   public abstract String getDriverClass(String var1, ClassLoader var2);

   @Override
   public String getBackupDriverClass(String url, ClassLoader classLoader) {
      return null;
   }

   @Override
   public abstract boolean handlesDatabaseProductNameAndVersion(String var1, String var2, java.sql.Connection var3);

   @Override
   public Database createDatabase(
      Configuration configuration, boolean printInfo, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor
   ) {
      String databaseProductName = jdbcConnectionFactory.getProductName();
      if (printInfo) {
         LOG.info("Database: " + jdbcConnectionFactory.getJdbcUrl() + " (" + databaseProductName + ")");
         LOG.debug("Driver  : " + jdbcConnectionFactory.getDriverInfo());
      }

      Database database = this.createDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
      String intendedCurrentSchema = configuration.getDefaultSchema();
      if (!database.supportsChangingCurrentSchema() && intendedCurrentSchema != null) {
         LOG.warn(
            databaseProductName
               + " does not support setting the schema for the current session. Default schema will NOT be changed to "
               + intendedCurrentSchema
               + " !"
         );
      }

      return database;
   }

   @Override
   public abstract Database createDatabase(Configuration var1, JdbcConnectionFactory var2, StatementInterceptor var3);

   @Override
   public abstract Parser createParser(Configuration var1, ResourceProvider var2, ParsingContext var3);

   @Override
   public SqlScriptFactory createSqlScriptFactory(Configuration configuration, ParsingContext parsingContext) {
      return (resource, mixed, resourceProvider) -> new ParserSqlScript(
            this.createParser(configuration, resourceProvider, parsingContext),
            resource,
            SqlScriptMetadata.getMetadataResource(resourceProvider, resource),
            mixed
         );
   }

   @Override
   public SqlScriptExecutorFactory createSqlScriptExecutorFactory(
      JdbcConnectionFactory jdbcConnectionFactory, CallbackExecutor callbackExecutor, StatementInterceptor statementInterceptor
   ) {
      boolean supportsBatch = false;
      return (connection, undo, batch, outputQueryResults) -> new DefaultSqlScriptExecutor(
            new JdbcTemplate(connection, this), callbackExecutor, undo, supportsBatch && batch, outputQueryResults, statementInterceptor
         );
   }

   @Override
   public DatabaseExecutionStrategy createExecutionStrategy(java.sql.Connection connection) {
      return new DefaultExecutionStrategy();
   }

   @Override
   public ExecutionTemplate createTransactionalExecutionTemplate(java.sql.Connection connection, boolean rollbackOnException) {
      return new TransactionalExecutionTemplate(connection, rollbackOnException);
   }

   public static String getSelectVersionOutput(java.sql.Connection connection) {
      PreparedStatement statement = null;
      ResultSet resultSet = null;
      String result = null;

      String var5;
      try {
         statement = connection.prepareStatement("SELECT version()");
         resultSet = statement.executeQuery();
         if (resultSet.next()) {
            result = resultSet.getString(1);
         }

         return result;
      } catch (SQLException var9) {
         var5 = "";
      } finally {
         JdbcUtils.closeResultSet(resultSet);
         JdbcUtils.closeStatement(statement);
      }

      return var5;
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
   }

   @Override
   public void setConfigConnectionProps(Configuration config, Properties props, ClassLoader classLoader) {
   }

   @Override
   public void setOverridingConnectionProps(Map<String, String> props) {
   }

   @Override
   public void shutdownDatabase(String url, Driver driver) {
   }

   @Override
   public boolean detectUserRequiredByUrl(String url) {
      return true;
   }

   @Override
   public boolean detectPasswordRequiredByUrl(String url) {
      return true;
   }

   @Override
   public boolean externalAuthPropertiesRequired(String url, String username, String password) {
      return false;
   }

   @Override
   public Properties getExternalAuthProperties(String url, String username) {
      return new Properties();
   }

   @Override
   public java.sql.Connection alterConnectionAsNeeded(java.sql.Connection connection, Configuration configuration) {
      return connection;
   }

   @Override
   public String instantiateClassExtendedErrorMessage() {
      return "";
   }

   @Override
   public void printMessages() {
   }
}
