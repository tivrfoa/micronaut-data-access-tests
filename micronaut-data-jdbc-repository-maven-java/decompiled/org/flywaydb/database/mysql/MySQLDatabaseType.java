package org.flywaydb.database.mysql;

import java.sql.Connection;
import java.util.Properties;
import org.flywaydb.authentication.mysql.MySQLOptionFileReader;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeMessage;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.ClassUtils;

public class MySQLDatabaseType extends BaseDatabaseType {
   private static final Log LOG = LogFactory.getLog(MySQLDatabaseType.class);
   private static final String MYSQL_LEGACY_JDBC_DRIVER = "com.mysql.jdbc.Driver";
   private static final String MARIADB_JDBC_DRIVER = "org.mariadb.jdbc.Driver";

   @Override
   public String getName() {
      return "MySQL";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      if (url.startsWith("jdbc-secretsmanager:mysql:")) {
         throw new FlywayTeamsUpgradeRequiredException("jdbc-secretsmanager");
      } else {
         return url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:google:") || url.startsWith("jdbc:p6spy:mysql:") || url.startsWith("jdbc:p6spy:google:");
      }
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      if (url.startsWith("jdbc:p6spy:mysql:") || url.startsWith("jdbc:p6spy:google:")) {
         return "com.p6spy.engine.spy.P6SpyDriver";
      } else {
         return url.startsWith("jdbc:mysql:") ? "com.mysql.cj.jdbc.Driver" : "com.mysql.jdbc.GoogleDriver";
      }
   }

   @Override
   public String getBackupDriverClass(String url, ClassLoader classLoader) {
      if (ClassUtils.isPresent("com.mysql.jdbc.Driver", classLoader)) {
         return "com.mysql.jdbc.Driver";
      } else {
         return ClassUtils.isPresent("org.mariadb.jdbc.Driver", classLoader) && !url.contains("disableMariaDbDriver") ? "org.mariadb.jdbc.Driver" : null;
      }
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.contains("MySQL");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new MySQLDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new MySQLParser(configuration, parsingContext);
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
      props.put("connectionAttributes", "program_name:Flyway by Redgate");
   }

   @Override
   public boolean detectPasswordRequiredByUrl(String url) {
      return super.detectPasswordRequiredByUrl(url);
   }

   @Override
   public boolean externalAuthPropertiesRequired(String url, String username, String password) {
      return super.externalAuthPropertiesRequired(url, username, password);
   }

   @Override
   public Properties getExternalAuthProperties(String url, String username) {
      MySQLOptionFileReader mySQLOptionFileReader = new MySQLOptionFileReader();
      mySQLOptionFileReader.populateOptionFiles();
      if (!mySQLOptionFileReader.optionFiles.isEmpty()) {
         LOG.info(FlywayTeamsUpgradeMessage.generate("a MySQL option file", "use this for database authentication"));
      }

      return super.getExternalAuthProperties(url, username);
   }

   @Override
   public String instantiateClassExtendedErrorMessage() {
      return "Failure probably due to inability to load dependencies. Please ensure you have downloaded 'https://dev.mysql.com/downloads/connector/j/' and extracted to 'flyway/drivers' folder";
   }
}
