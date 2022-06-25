package org.flywaydb.core.internal.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.StringUtils;

public class DriverDataSource implements DataSource {
   private Driver driver;
   private final String url;
   private final DatabaseType type;
   private final String user;
   private final String password;
   private final Properties defaultProperties;
   private final Map<String, String> additionalProperties;
   private boolean autoCommit = true;

   public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password) throws FlywayException {
      this(classLoader, driverClass, url, user, password, null, new Properties(), new HashMap());
   }

   public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Configuration configuration) throws FlywayException {
      this(classLoader, driverClass, url, user, password, configuration, new Properties(), new HashMap());
   }

   public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password, Map<String, String> additionalProperties) throws FlywayException {
      this(classLoader, driverClass, url, user, password, null, new Properties(), additionalProperties);
   }

   public DriverDataSource(
      ClassLoader classLoader,
      String driverClass,
      String url,
      String user,
      String password,
      Configuration configuration,
      Map<String, String> additionalProperties
   ) throws FlywayException {
      this(classLoader, driverClass, url, user, password, configuration, new Properties(), additionalProperties);
   }

   public DriverDataSource(
      ClassLoader classLoader,
      String driverClass,
      String url,
      String user,
      String password,
      Configuration configuration,
      Properties defaultProperties,
      Map<String, String> additionalProperties
   ) throws FlywayException {
      this.url = this.detectFallbackUrl(url);
      this.type = DatabaseTypeRegister.getDatabaseTypeForUrl(url);
      if (!StringUtils.hasLength(driverClass)) {
         if (this.type == null) {
            throw new FlywayException("Unable to autodetect JDBC driver for url: " + DatabaseTypeRegister.redactJdbcUrl(url));
         }

         driverClass = this.type.getDriverClass(url, classLoader);
      }

      if (additionalProperties != null) {
         this.additionalProperties = additionalProperties;
      } else {
         this.additionalProperties = new HashMap();
      }

      this.defaultProperties = new Properties(defaultProperties);
      this.type.setDefaultConnectionProps(url, defaultProperties, classLoader);
      this.type.setConfigConnectionProps(configuration, defaultProperties, classLoader);
      this.type.setOverridingConnectionProps(this.additionalProperties);

      try {
         this.driver = ClassUtils.instantiate(driverClass, classLoader);
      } catch (FlywayException var13) {
         String backupDriverClass = this.type.getBackupDriverClass(url, classLoader);
         if (backupDriverClass == null) {
            String extendedError = this.type.instantiateClassExtendedErrorMessage();
            if (StringUtils.hasText(extendedError)) {
               extendedError = "\r\n" + extendedError;
            }

            throw new FlywayException(
               "Unable to instantiate JDBC driver: " + driverClass + " => Check whether the jar file is present" + extendedError, var13, ErrorCode.JDBC_DRIVER
            );
         }

         try {
            this.driver = ClassUtils.instantiate(backupDriverClass, classLoader);
         } catch (Exception var12) {
            throw new FlywayException(
               "Unable to instantiate JDBC driver: " + driverClass + " or backup driver: " + backupDriverClass + " => Check whether the jar file is present",
               var13,
               ErrorCode.JDBC_DRIVER
            );
         }
      }

      this.user = this.detectFallbackUser(user);
      this.password = this.detectFallbackPassword(password);
      if (this.type.externalAuthPropertiesRequired(url, user, password)) {
         defaultProperties.putAll(this.type.getExternalAuthProperties(url, user));
      }

   }

   private String detectFallbackUrl(String url) {
      if (!StringUtils.hasText(url)) {
         String boxfuseDatabaseUrl = System.getenv("BOXFUSE_DATABASE_URL");
         if (StringUtils.hasText(boxfuseDatabaseUrl)) {
            return boxfuseDatabaseUrl;
         } else {
            throw new FlywayException("Missing required JDBC URL. Unable to create DataSource!");
         }
      } else {
         return url;
      }
   }

   private String detectFallbackUser(String user) {
      if (!StringUtils.hasText(user)) {
         String boxfuseDatabaseUser = System.getenv("BOXFUSE_DATABASE_USER");
         if (StringUtils.hasText(boxfuseDatabaseUser)) {
            return boxfuseDatabaseUser;
         }
      }

      return user;
   }

   private String detectFallbackPassword(String password) {
      if (!StringUtils.hasText(password)) {
         String boxfuseDatabasePassword = System.getenv("BOXFUSE_DATABASE_PASSWORD");
         if (StringUtils.hasText(boxfuseDatabasePassword)) {
            return boxfuseDatabasePassword;
         }
      }

      return password;
   }

   public Connection getConnection() throws SQLException {
      return this.getConnectionFromDriver(this.getUser(), this.getPassword());
   }

   public Connection getConnection(String username, String password) throws SQLException {
      return this.getConnectionFromDriver(username, password);
   }

   protected Connection getConnectionFromDriver(String username, String password) throws SQLException {
      Properties properties = new Properties(this.defaultProperties);
      if (username != null) {
         properties.setProperty("user", username);
      }

      if (password != null) {
         properties.setProperty("password", password);
      }

      properties.putAll(this.additionalProperties);
      Connection connection = this.driver.connect(this.url, properties);
      if (connection == null) {
         throw new FlywayException("Unable to connect to " + DatabaseTypeRegister.redactJdbcUrl(this.url));
      } else {
         connection.setAutoCommit(this.autoCommit);
         return connection;
      }
   }

   public void shutdownDatabase() {
      this.type.shutdownDatabase(this.url, this.driver);
   }

   public int getLoginTimeout() {
      return 0;
   }

   public void setLoginTimeout(int timeout) {
      this.unsupportedMethod("setLoginTimeout");
   }

   public PrintWriter getLogWriter() {
      this.unsupportedMethod("getLogWriter");
      return null;
   }

   public void setLogWriter(PrintWriter pw) {
      this.unsupportedMethod("setLogWriter");
   }

   public <T> T unwrap(Class<T> iface) {
      this.unsupportedMethod("unwrap");
      return null;
   }

   public boolean isWrapperFor(Class<?> iface) {
      return DataSource.class.equals(iface);
   }

   public Logger getParentLogger() {
      this.unsupportedMethod("getParentLogger");
      return null;
   }

   private void unsupportedMethod(String methodName) {
      throw new UnsupportedOperationException(methodName);
   }

   public Driver getDriver() {
      return this.driver;
   }

   public String getUrl() {
      return this.url;
   }

   public String getUser() {
      return this.user;
   }

   public String getPassword() {
      return this.password;
   }

   public Map<String, String> getAdditionalProperties() {
      return this.additionalProperties;
   }

   public boolean isAutoCommit() {
      return this.autoCommit;
   }

   public void setAutoCommit(boolean autoCommit) {
      this.autoCommit = autoCommit;
   }
}
