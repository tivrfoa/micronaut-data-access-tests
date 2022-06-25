package org.flywaydb.core.internal.database.db2;

import java.sql.Connection;
import java.util.Properties;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class DB2DatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "DB2";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:db2:") || url.startsWith("jdbc:p6spy:db2:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:db2:") ? "com.p6spy.engine.spy.P6SpyDriver" : "com.ibm.db2.jcc.DB2Driver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("DB2");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new DB2Database(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new DB2Parser(configuration, parsingContext);
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
      props.put("clientProgramName", "Flyway by Redgate");
      props.put("retrieveMessagesFromServerOnGetMessage", "true");
   }
}
