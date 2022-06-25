package org.flywaydb.core.internal.database.sybasease;

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

public class SybaseASEJTDSDatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "Sybase ASE";
   }

   @Override
   public int getNullType() {
      return 0;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:jtds:") || url.startsWith("jdbc:p6spy:jtds:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:jtds:") ? "com.p6spy.engine.spy.P6SpyDriver" : "net.sourceforge.jtds.jdbc.Driver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("ASE");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new SybaseASEDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new SybaseASEParser(configuration, parsingContext);
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
      props.put("APPLICATIONNAME", "Flyway by Redgate");
   }
}
