package org.flywaydb.core.internal.database.sqlite;

import java.sql.Connection;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

public class SQLiteDatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "SQLite";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:sqlite:")
         || url.startsWith("jdbc:sqldroid:")
         || url.startsWith("jdbc:p6spy:sqlite:")
         || url.startsWith("jdbc:p6spy:sqldroid:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      if (url.startsWith("jdbc:p6spy:sqlite:") || url.startsWith("jdbc:p6spy:sqldroid:")) {
         return "com.p6spy.engine.spy.P6SpyDriver";
      } else {
         return url.startsWith("jdbc:sqldroid:") ? "org.sqldroid.SQLDroidDriver" : "org.sqlite.JDBC";
      }
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("SQLite");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new SQLiteDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new SQLiteParser(configuration, parsingContext);
   }
}
