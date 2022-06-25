package org.flywaydb.core.internal.database.h2;

import java.sql.Connection;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class H2DatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "H2";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:h2:") || url.startsWith("jdbc:p6spy:h2:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:h2:") ? "com.p6spy.engine.spy.P6SpyDriver" : "org.h2.Driver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("H2");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new H2Database(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new H2Parser(configuration, parsingContext);
   }

   @Override
   public boolean detectUserRequiredByUrl(String url) {
      return !url.toLowerCase().contains(":mem:");
   }

   @Override
   public boolean detectPasswordRequiredByUrl(String url) {
      return !url.toLowerCase().contains(":mem:");
   }
}
