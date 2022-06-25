package org.flywaydb.core.internal.database.cockroachdb;

import java.sql.Connection;
import java.util.Properties;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.authentication.postgres.PgpassFileReader;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DefaultExecutionStrategy;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.ExecutionTemplate;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeMessage;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class CockroachDBDatabaseType extends BaseDatabaseType {
   private static final Log LOG = LogFactory.getLog(CockroachDBDatabaseType.class);

   @Override
   public String getName() {
      return "CockroachDB";
   }

   @Override
   public int getNullType() {
      return 0;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:postgresql:") || url.startsWith("jdbc:p6spy:postgresql:");
   }

   @Override
   public int getPriority() {
      return 1;
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:postgresql:") ? "com.p6spy.engine.spy.P6SpyDriver" : "org.postgresql.Driver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      if (databaseProductName.startsWith("PostgreSQL")) {
         String selectVersionQueryOutput = getSelectVersionOutput(connection);
         return selectVersionQueryOutput.contains("CockroachDB");
      } else {
         return false;
      }
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new CockroachDBDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new CockroachDBParser(configuration, parsingContext);
   }

   @Override
   public DatabaseExecutionStrategy createExecutionStrategy(Connection connection) {
      return (DatabaseExecutionStrategy)(connection == null ? new DefaultExecutionStrategy() : new CockroachDBRetryingStrategy());
   }

   @Override
   public ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
      return new CockroachRetryingTransactionalExecutionTemplate(connection, rollbackOnException);
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
      props.put("applicationName", "Flyway by Redgate");
   }

   @Override
   public boolean detectUserRequiredByUrl(String url) {
      return !url.contains("user=");
   }

   @Override
   public boolean detectPasswordRequiredByUrl(String url) {
      return !url.contains("password=");
   }

   @Override
   public boolean externalAuthPropertiesRequired(String url, String username, String password) {
      return super.externalAuthPropertiesRequired(url, username, password);
   }

   @Override
   public Properties getExternalAuthProperties(String url, String username) {
      PgpassFileReader pgpassFileReader = new PgpassFileReader();
      if (pgpassFileReader.getPgpassFilePath() != null) {
         LOG.info(FlywayTeamsUpgradeMessage.generate("pgpass file '" + pgpassFileReader.getPgpassFilePath() + "'", "use this for database authentication"));
      }

      return super.getExternalAuthProperties(url, username);
   }
}
