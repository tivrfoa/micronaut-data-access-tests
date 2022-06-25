package org.flywaydb.core.internal.database.saphana;

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

public class SAPHANADatabaseType extends BaseDatabaseType {
   @Override
   public String getName() {
      return "SAP HANA";
   }

   @Override
   public int getNullType() {
      return 12;
   }

   @Override
   public boolean handlesJDBCUrl(String url) {
      return url.startsWith("jdbc:sap:") || url.startsWith("jdbc:p6spy:sap:");
   }

   @Override
   public String getDriverClass(String url, ClassLoader classLoader) {
      return url.startsWith("jdbc:p6spy:sap:") ? "com.p6spy.engine.spy.P6SpyDriver" : "com.sap.db.jdbc.Driver";
   }

   @Override
   public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
      return databaseProductName.startsWith("HDB");
   }

   @Override
   public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
      return new SAPHANADatabase(configuration, jdbcConnectionFactory, statementInterceptor);
   }

   @Override
   public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
      return new SAPHANAParser(configuration, parsingContext);
   }

   @Override
   public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
      props.put("SESSIONVARIABLE:APPLICATION", "Flyway by Redgate");
   }
}
