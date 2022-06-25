package org.flywaydb.core.internal.jdbc;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.exception.FlywaySqlException;

public class JdbcConnectionFactory implements Closeable {
   private static final Log LOG = LogFactory.getLog(JdbcConnectionFactory.class);
   private final DataSource dataSource;
   private final int connectRetries;
   private final int connectRetriesInterval;
   private final Configuration configuration;
   private final DatabaseType databaseType;
   private final String jdbcUrl;
   private final String driverInfo;
   private final String productName;
   private Connection firstConnection;
   private JdbcConnectionFactory.ConnectionInitializer connectionInitializer;

   public JdbcConnectionFactory(DataSource dataSource, Configuration configuration, StatementInterceptor statementInterceptor) {
      this.dataSource = dataSource;
      this.connectRetries = configuration.getConnectRetries();
      this.connectRetriesInterval = configuration.getConnectRetriesInterval();
      this.configuration = configuration;
      this.firstConnection = JdbcUtils.openConnection(dataSource, this.connectRetries, this.connectRetriesInterval);
      this.databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(this.firstConnection);
      DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(this.firstConnection);
      this.jdbcUrl = getJdbcUrl(databaseMetaData);
      this.driverInfo = getDriverInfo(databaseMetaData);
      this.productName = JdbcUtils.getDatabaseProductName(databaseMetaData);
      this.firstConnection = this.databaseType.alterConnectionAsNeeded(this.firstConnection, configuration);
   }

   public void setConnectionInitializer(JdbcConnectionFactory.ConnectionInitializer connectionInitializer) {
      this.connectionInitializer = connectionInitializer;
   }

   public Connection openConnection() throws FlywayException {
      Connection connection = this.firstConnection == null
         ? JdbcUtils.openConnection(this.dataSource, this.connectRetries, this.connectRetriesInterval)
         : this.firstConnection;
      this.firstConnection = null;
      if (this.connectionInitializer != null) {
         this.connectionInitializer.initialize(this, connection);
      }

      return this.databaseType.alterConnectionAsNeeded(connection, this.configuration);
   }

   public void close() {
      if (this.firstConnection != null) {
         try {
            this.firstConnection.close();
         } catch (Exception var2) {
            LOG.error("Error while closing connection: " + var2.getMessage(), var2);
         }

         this.firstConnection = null;
      }

   }

   private static String getJdbcUrl(DatabaseMetaData databaseMetaData) {
      String url;
      try {
         url = databaseMetaData.getURL();
      } catch (SQLException var3) {
         throw new FlywaySqlException("Unable to retrieve the JDBC connection URL!", var3);
      }

      return url == null ? "" : filterUrl(url);
   }

   static String filterUrl(String url) {
      int questionMark = url.indexOf("?");
      if (questionMark >= 0 && !url.contains("?databaseName=")) {
         url = url.substring(0, questionMark);
      }

      return url.replaceAll("://.*:.*@", "://");
   }

   private static String getDriverInfo(DatabaseMetaData databaseMetaData) {
      try {
         return databaseMetaData.getDriverName() + " " + databaseMetaData.getDriverVersion();
      } catch (SQLException var2) {
         throw new FlywaySqlException("Unable to read database driver info: " + var2.getMessage(), var2);
      }
   }

   public DatabaseType getDatabaseType() {
      return this.databaseType;
   }

   public String getJdbcUrl() {
      return this.jdbcUrl;
   }

   public String getDriverInfo() {
      return this.driverInfo;
   }

   public String getProductName() {
      return this.productName;
   }

   public interface ConnectionInitializer {
      void initialize(JdbcConnectionFactory var1, Connection var2);
   }
}
