package org.flywaydb.core.internal.database.informix;

import java.sql.Connection;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class InformixDatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "Informix";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:informix-sqli:") || url.startsWith("jdbc:p6spy:informix-sqli:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:informix-sqli:") ? "com.p6spy.engine.spy.P6SpyDriver" : "com.informix.jdbc.IfxDriver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("Informix");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new InformixDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new InformixParser(configuration, parsingContext);
   }
}
