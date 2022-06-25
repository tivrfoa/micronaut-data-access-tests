package org.flywaydb.core.internal.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.util.StringUtils;

public class DatabaseTypeRegister {
   private static final Log LOG = LogFactory.getLog(DatabaseTypeRegister.class);
   private static final List<DatabaseType> SORTED_DATABASE_TYPES = (List<DatabaseType>)PluginRegister.getPlugins(DatabaseType.class)
      .stream()
      .sorted()
      .collect(Collectors.toList());

   public static DatabaseType getDatabaseTypeForUrl(String url) {
      List<DatabaseType> typesAcceptingUrl = getDatabaseTypesForUrl(url);
      if (typesAcceptingUrl.size() <= 0) {
         throw new FlywayException("No database found to handle " + redactJdbcUrl(url));
      } else {
         if (typesAcceptingUrl.size() > 1) {
            StringBuilder builder = new StringBuilder();

            for(DatabaseType type : typesAcceptingUrl) {
               if (builder.length() > 0) {
                  builder.append(", ");
               }

               builder.append(type.getName());
            }

            LOG.debug("Multiple databases found that handle url '" + redactJdbcUrl(url) + "': " + builder);
         }

         return (DatabaseType)typesAcceptingUrl.get(0);
      }
   }

   private static List<DatabaseType> getDatabaseTypesForUrl(String url) {
      List<DatabaseType> typesAcceptingUrl = new ArrayList();

      for(DatabaseType type : SORTED_DATABASE_TYPES) {
         if (type.handlesJDBCUrl(url)) {
            typesAcceptingUrl.add(type);
         }
      }

      return typesAcceptingUrl;
   }

   public static String redactJdbcUrl(String url) {
      List<DatabaseType> types = getDatabaseTypesForUrl(url);
      if (types.isEmpty()) {
         url = redactJdbcUrl(url, BaseDatabaseType.getDefaultJDBCCredentialsPattern());
      } else {
         for(DatabaseType type : types) {
            Pattern dbPattern = type.getJDBCCredentialsPattern();
            if (dbPattern != null) {
               url = redactJdbcUrl(url, dbPattern);
            }
         }
      }

      return url;
   }

   private static String redactJdbcUrl(String url, Pattern pattern) {
      Matcher matcher = pattern.matcher(url);
      if (matcher.find()) {
         String password = matcher.group(1);
         return url.replace(password, StringUtils.trimOrPad("", password.length(), '*'));
      } else {
         return url;
      }
   }

   public static DatabaseType getDatabaseTypeForConnection(Connection connection) {
      DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
      String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
      String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

      for(DatabaseType type : SORTED_DATABASE_TYPES) {
         if (type.handlesDatabaseProductNameAndVersion(databaseProductName, databaseProductVersion, connection)) {
            return type;
         }
      }

      throw new FlywayException("Unsupported Database: " + databaseProductName);
   }
}
