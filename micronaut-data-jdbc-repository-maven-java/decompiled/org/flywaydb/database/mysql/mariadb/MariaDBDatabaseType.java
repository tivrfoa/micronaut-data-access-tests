package org.flywaydb.database.mysql.mariadb;

import java.sql.Connection;
import java.util.Properties;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class MariaDBDatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "MariaDB";
   }

   @Override
   public int getPriority() {
      return 1;
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      if (url.startsWith("jdbc-secretsmanager:mariadb:")) {
         throw new FlywayTeamsUpgradeRequiredException("jdbc-secretsmanager");
      } else {
         return url.startsWith("jdbc:mariadb:") || url.startsWith("jdbc:p6spy:mariadb:");
      }
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:mariadb:") ? "com.p6spy.engine.spy.P6SpyDriver" : "org.mariadb.jdbc.Driver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("MariaDB")
         || databaseProductName.contains("MySQL") && databaseProductVersion.contains("MariaDB")
         || databaseProductName.contains("MySQL") && getSelectVersionOutput(connection).contains("MariaDB");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new MariaDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new MariaDBParser(configuration, parsingContext);
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
      props.put("connectionAttributes", "program_name:Flyway by Redgate");
   }

   @Override
   public boolean detectPasswordRequiredByUrl(String url) {
      return super.detectPasswordRequiredByUrl(url);
   }
}
