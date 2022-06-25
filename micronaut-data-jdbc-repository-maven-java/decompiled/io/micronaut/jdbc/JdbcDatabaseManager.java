package io.micronaut.jdbc;

import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JdbcDatabaseManager {
   private static List<JdbcDatabaseManager.JdbcDatabase> databases = new ArrayList(16);

   public static Optional<JdbcDatabaseManager.JdbcDatabase> findDatabase(String jdbcUrl) {
      if (StringUtils.isNotEmpty(jdbcUrl)) {
         if (!jdbcUrl.startsWith("jdbc")) {
            throw new IllegalArgumentException("Invalid JDBC URL [" + jdbcUrl + "]. JDBC URLs must start with 'jdbc'.");
         } else {
            String partialUrl = jdbcUrl.substring(5);
            String prefix = partialUrl.substring(0, partialUrl.indexOf(58)).toLowerCase();
            return databases.stream().filter(db -> db.containsPrefix(prefix)).findFirst();
         }
      } else {
         return Optional.empty();
      }
   }

   public static Optional<JdbcDatabaseManager.EmbeddedJdbcDatabase> get(ClassLoader classLoader) {
      return databases.stream()
         .filter(JdbcDatabaseManager.JdbcDatabase::isEmbedded)
         .map(JdbcDatabaseManager.EmbeddedJdbcDatabase.class::cast)
         .filter(embeddedDatabase -> ClassUtils.isPresent(embeddedDatabase.getDriverClassName(), classLoader))
         .findFirst();
   }

   public static boolean isEmbedded(String driverClassName) {
      return databases.stream().filter(JdbcDatabaseManager.JdbcDatabase::isEmbedded).anyMatch(db -> db.driverClassName.equals(driverClassName));
   }

   static {
      databases.add(new JdbcDatabaseManager.EmbeddedJdbcDatabase("org.h2.Driver", "h2", "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"));
      databases.add(
         new JdbcDatabaseManager.EmbeddedJdbcDatabase(
            "org.apache.derby.jdbc.EmbeddedDriver", "SELECT 1 FROM SYSIBM.SYSDUMMY1", new String[]{"derby"}, "jdbc:derby:memory:%s;create=true"
         )
      );
      databases.add(
         new JdbcDatabaseManager.EmbeddedJdbcDatabase(
            "org.hsqldb.jdbc.JDBCDriver", "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS", new String[]{"hsqldb"}, "jdbc:hsqldb:mem:%s"
         )
      );
      databases.add(new JdbcDatabaseManager.JdbcDatabase("com.mysql.cj.jdbc.Driver", "mysql"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("oracle.jdbc.OracleDriver", "SELECT 1 FROM DUAL", new String[]{"oracle"}));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("org.postgresql.Driver", "postgresql"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("org.sqlite.JDBC", "sqlite"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("org.mariadb.jdbc.Driver", "mariadb"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("com.google.appengine.api.rdbms.AppEngineDriver", "gae"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("net.sourceforge.jtds.jdbc.Driver", "jtds"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("org.firebirdsql.jdbc.FBDriver", "SELECT 1 FROM RDB$DATABASE", new String[]{"firebirdsql"}));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("com.ibm.db2.jcc.DB2Driver", "SELECT 1 FROM SYSIBM.SYSDUMMY1", new String[]{"db2"}));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("com.ibm.as400.access.AS400JDBCDriver", "SELECT 1 FROM SYSIBM.SYSDUMMY1", new String[]{"as400"}));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("com.teradata.jdbc.TeraDriver", "teradata"));
      databases.add(new JdbcDatabaseManager.JdbcDatabase("com.informix.jdbc.IfxDriver", "select count(*) from systables", new String[]{"informix"}));
   }

   public static class EmbeddedJdbcDatabase extends JdbcDatabaseManager.JdbcDatabase {
      private String defaultUrl;
      private String defaultName = "devDb";

      EmbeddedJdbcDatabase(String driverClassName, String validationQuery, String[] urlPrefixes, String defaultUrl) {
         super(driverClassName, validationQuery, urlPrefixes);
         this.defaultUrl = defaultUrl;
      }

      EmbeddedJdbcDatabase(String driverClassName, String[] urlPrefixes, String defaultUrl) {
         super(driverClassName, urlPrefixes);
         this.defaultUrl = defaultUrl;
      }

      EmbeddedJdbcDatabase(String driverClassName, String urlPrefix, String defaultUrl) {
         super(driverClassName, urlPrefix);
         this.defaultUrl = defaultUrl;
      }

      public String getUrl(String databaseName) {
         if (databaseName == null) {
            databaseName = this.defaultName;
         }

         return String.format(this.defaultUrl, databaseName);
      }

      @Override
      protected boolean isEmbedded() {
         return Boolean.TRUE;
      }
   }

   public static class JdbcDatabase {
      private String driverClassName;
      private String validationQuery;
      private Collection<String> urlPrefixes;

      JdbcDatabase(String driverClassName, String validationQuery, String[] urlPrefixes) {
         this.driverClassName = driverClassName;
         this.urlPrefixes = Arrays.asList(urlPrefixes);
         this.validationQuery = validationQuery;
      }

      JdbcDatabase(String driverClassName, String[] urlPrefixes) {
         this(driverClassName, "SELECT 1", urlPrefixes);
      }

      JdbcDatabase(String driverClassName, String urlPrefix) {
         this(driverClassName, "SELECT 1", new String[]{urlPrefix});
      }

      protected boolean isEmbedded() {
         return Boolean.FALSE;
      }

      public String getDriverClassName() {
         return this.driverClassName;
      }

      public String getValidationQuery() {
         return this.validationQuery;
      }

      public boolean containsPrefix(String prefix) {
         return this.urlPrefixes.contains(prefix);
      }
   }
}
