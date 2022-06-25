package org.flywaydb.core.internal.database.redshift;

import java.sql.Connection;
import java.util.Map;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.ClassUtils;

public class RedshiftDatabaseType extends BaseDatabaseType {
   private static final String REDSHIFT_JDBC4_DRIVER = "com.amazon.redshift.jdbc4.Driver";
   private static final String REDSHIFT_JDBC41_DRIVER = "com.amazon.redshift.jdbc41.Driver";

   @Override
   public String getName() {
      return "Redshift";
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
      return url.startsWith("jdbc:redshift:") || url.startsWith("jdbc:p6spy:redshift:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:redshift:") ? "com.p6spy.engine.spy.P6SpyDriver" : "com.amazon.redshift.jdbc42.Driver";
   }

   @Override
   public String getBackupDriverClass(String url, ClassLoader classLoader) {
      return ClassUtils.isPresent("com.amazon.redshift.jdbc41.Driver", classLoader) ? "com.amazon.redshift.jdbc41.Driver" : "com.amazon.redshift.jdbc4.Driver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      if (databaseProductName.startsWith("PostgreSQL")) {
         String selectVersionQueryOutput = getSelectVersionOutput(connection);
         if (databaseProductName.startsWith("PostgreSQL 8") && selectVersionQueryOutput.contains("Redshift")) {
            return true;
         }
      }

      return databaseProductName.startsWith("Redshift");
   }

   @Override
   public void setOverridingConnectionProps(Map<String, String> props) {
      props.put("enableFetchRingBuffer", "false");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new RedshiftDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new RedshiftParser(configuration, parsingContext);
   }
}
