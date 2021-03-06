package org.flywaydb.core.internal.database.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutor;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.util.ClassUtils;

public class OracleDatabaseType extends BaseDatabaseType {
   private static final Log LOG = LogFactory.getLog(OracleDatabaseType.class);
   private static final Pattern usernamePasswordPattern = Pattern.compile("^jdbc:oracle:thin:[a-zA-Z0-9#_$]+/([a-zA-Z0-9#_$]+)@.*");

   @Override
   public String getName() {
      return "Oracle";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      if (url.startsWith("jdbc-secretsmanager:oracle:")) {
         throw new FlywayTeamsUpgradeRequiredException("jdbc-secretsmanager");
      } else {
         return url.startsWith("jdbc:oracle") || url.startsWith("jdbc:p6spy:oracle");
      }
   }

   @Override
   public Pattern getJDBCCredentialsPattern() {
      return usernamePasswordPattern;
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:oracle:") ? "com.p6spy.engine.spy.P6SpyDriver" : "oracle.jdbc.OracleDriver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("Oracle");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      OracleDatabase.enableTnsnamesOraSupport();
      return new OracleDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new OracleParser(configuration, parsingContext);
   }

   @Override
   public SqlScriptExecutorFactory createSqlScriptExecutorFactory(
      JdbcConnectionFactory jdbcConnectionFactory, final CallbackExecutor callbackExecutor, final StatementInterceptor statementInterceptor
   ) {
      final DatabaseType thisRef = this;
      return new SqlScriptExecutorFactory() {
         @Override
         public SqlScriptExecutor createSqlScriptExecutor(Connection connection, boolean undo, boolean batch, boolean outputQueryResults) {
            return new OracleSqlScriptExecutor(new JdbcTemplate(connection, thisRef), callbackExecutor, undo, batch, outputQueryResults, statementInterceptor);
         }
      };
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
      String osUser = System.getProperty("user.name");
      props.put("v$session.osuser", osUser.substring(0, Math.min(osUser.length(), 30)));
      props.put("v$session.program", "Flyway by Redgate");
      props.put("oracle.net.keepAlive", "true");
      String oobb = ClassUtils.getStaticFieldValue("oracle.jdbc.OracleConnection", "CONNECTION_PROPERTY_THIN_NET_DISABLE_OUT_OF_BAND_BREAK", classLoader);
      props.put(oobb, "true");
   }

   @Override
   public void setConfigConnectionProps(Configuration config, Properties props, ClassLoader classLoader) {
   }

   @Override
   public boolean detectUserRequiredByUrl(String url) {
      return !usernamePasswordPattern.matcher(url).matches();
   }

   @Override
   public boolean detectPasswordRequiredByUrl(String url) {
      return !usernamePasswordPattern.matcher(url).matches();
   }

   @Override
   public Connection alterConnectionAsNeeded(Connection connection, Configuration configuration) {
      Map<String, String> jdbcProperties = configuration.getJdbcProperties();
      if (jdbcProperties != null && jdbcProperties.containsKey("PROXY_USER_NAME")) {
         try {
            oracle.jdbc.OracleConnection oracleConnection;
            try {
               if (connection instanceof oracle.jdbc.OracleConnection) {
                  oracleConnection = (oracle.jdbc.OracleConnection)connection;
               } else {
                  if (!connection.isWrapperFor(oracle.jdbc.OracleConnection.class)) {
                     throw new FlywayException("Unable to extract Oracle connection type from '" + connection.getClass().getName() + "'");
                  }

                  oracleConnection = (oracle.jdbc.OracleConnection)connection.unwrap(oracle.jdbc.OracleConnection.class);
               }
            } catch (SQLException var6) {
               throw new FlywayException("Unable to unwrap connection type '" + connection.getClass().getName() + "'", var6);
            }

            if (!oracleConnection.isProxySession()) {
               Properties props = new Properties();
               props.putAll(configuration.getJdbcProperties());
               oracleConnection.openProxySession(1, props);
            }
         } catch (FlywayException var7) {
            LOG.warn(var7.getMessage());
         } catch (SQLException var8) {
            throw new FlywayException("Unable to open proxy session: " + var8.getMessage(), var8);
         }
      }

      return super.alterConnectionAsNeeded(connection, configuration);
   }
}
