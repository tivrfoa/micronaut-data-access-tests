package org.flywaydb.core.internal.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.strategy.BackoffStrategy;
import org.flywaydb.core.internal.util.ExceptionUtils;

public class JdbcUtils {
   private static final Log LOG = LogFactory.getLog(JdbcUtils.class);

   public static Connection openConnection(DataSource dataSource, int connectRetries, int connectRetriesInterval) throws FlywayException {
      BackoffStrategy backoffStrategy = new BackoffStrategy(1, 2, connectRetriesInterval);
      int retries = 0;

      while(true) {
         try {
            return dataSource.getConnection();
         } catch (SQLException var10) {
            if ("08S01".equals(var10.getSQLState()) && var10.getMessage().contains("This driver is not configured for integrated authentication")) {
               throw new FlywaySqlException(
                  "Unable to obtain connection from database"
                     + getDataSourceInfo(dataSource, true)
                     + ": "
                     + var10.getMessage()
                     + "\nTo setup integrated authentication see "
                     + "https://rd.gt/39KICcS",
                  var10
               );
            }

            if (var10.getSQLState() == null && var10.getMessage().contains("MSAL4J")) {
               throw new FlywaySqlException(
                  "Unable to obtain connection from database"
                     + getDataSourceInfo(dataSource, false)
                     + ": "
                     + var10.getMessage()
                     + "\nYou need to install some extra drivers in order for interactive authentication to work.\nFor instructions, see "
                     + "https://rd.gt/3unaRb8",
                  var10
               );
            }

            if (++retries > connectRetries) {
               throw new FlywaySqlException(
                  "Unable to obtain connection from database" + getDataSourceInfo(dataSource, false) + ": " + var10.getMessage(), var10
               );
            }

            Throwable rootCause = ExceptionUtils.getRootCause(var10);
            String message = "Connection error: " + var10.getMessage();
            if (rootCause != null && rootCause != var10 && rootCause.getMessage() != null) {
               message = message + "\n(Caused by " + rootCause.getMessage() + ")";
            }

            LOG.warn(message + "\nRetrying in " + backoffStrategy.peek() + " sec...");

            try {
               Thread.sleep((long)backoffStrategy.next() * 1000L);
            } catch (InterruptedException var9) {
               throw new FlywaySqlException(
                  "Unable to obtain connection from database" + getDataSourceInfo(dataSource, false) + ": " + var10.getMessage(), var10
               );
            }
         }
      }
   }

   private static String getDataSourceInfo(DataSource dataSource, boolean suppressNullUserMessage) {
      if (!(dataSource instanceof DriverDataSource)) {
         return "";
      } else {
         DriverDataSource driverDataSource = (DriverDataSource)dataSource;
         String user = driverDataSource.getUser();
         String info = " (" + DatabaseTypeRegister.redactJdbcUrl(driverDataSource.getUrl()) + ")";
         if (user != null || !suppressNullUserMessage) {
            info = info + " for user '" + user + "'";
         }

         return info;
      }
   }

   public static void closeConnection(Connection connection) {
      if (connection != null) {
         try {
            connection.close();
         } catch (Exception var2) {
            LOG.error("Error while closing database Connection: " + var2.getMessage(), var2);
         }

      }
   }

   public static void closeStatement(Statement statement) {
      if (statement != null) {
         try {
            statement.close();
         } catch (SQLException var2) {
            LOG.error("Error while closing JDBC Statement", var2);
         }

      }
   }

   public static void closeResultSet(ResultSet resultSet) {
      if (resultSet != null) {
         try {
            resultSet.close();
         } catch (SQLException var2) {
            LOG.error("Error while closing JDBC ResultSet", var2);
         }

      }
   }

   public static DatabaseMetaData getDatabaseMetaData(Connection connection) {
      DatabaseMetaData databaseMetaData;
      try {
         databaseMetaData = connection.getMetaData();
      } catch (SQLException var3) {
         throw new FlywaySqlException("Unable to read database connection metadata: " + var3.getMessage(), var3);
      }

      if (databaseMetaData == null) {
         throw new FlywayException("Unable to read database connection metadata while it is null!");
      } else {
         return databaseMetaData;
      }
   }

   public static String getDatabaseProductName(DatabaseMetaData databaseMetaData) {
      try {
         String databaseProductName = databaseMetaData.getDatabaseProductName();
         if (databaseProductName == null) {
            throw new FlywayException("Unable to determine database. Product name is null.");
         } else {
            return databaseProductName + " " + databaseMetaData.getDatabaseMajorVersion() + "." + databaseMetaData.getDatabaseMinorVersion();
         }
      } catch (SQLException var2) {
         throw new FlywaySqlException("Error while determining database product name", var2);
      }
   }

   public static String getDatabaseProductVersion(DatabaseMetaData databaseMetaData) {
      try {
         return databaseMetaData.getDatabaseProductVersion();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Error while determining database product version", var2);
      }
   }

   private JdbcUtils() {
   }
}
