package org.flywaydb.core.internal.database.snowflake;

import java.sql.Connection;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class SnowflakeDatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "Snowflake";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:snowflake:") || url.startsWith("jdbc:p6spy:snowflake:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:snowflake:") ? "com.p6spy.engine.spy.P6SpyDriver" : "net.snowflake.client.jdbc.SnowflakeDriver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("Snowflake");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new SnowflakeDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new SnowflakeParser(configuration, parsingContext);
   }

   @Override
   public boolean detectUserRequiredByUrl(String url) {
      return !url.contains("user=");
   }

   @Override
   public boolean detectPasswordRequiredByUrl(String url) {
      return !url.contains("private_key_file=");
   }
}
