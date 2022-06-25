package io.micronaut.jdbc;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.StringUtils;
import java.util.Optional;

public class CalculatedSettings {
   private String calculatedDriverClassName;
   private String calculatedUrl;
   private String calculatedUsername;
   private String calculatedPassword;
   private String calculatedValidationQuery;
   private Optional<JdbcDatabaseManager.EmbeddedJdbcDatabase> embeddedDatabaseConnection;
   private BasicJdbcConfiguration basicJdbcConfiguration;

   public CalculatedSettings(BasicJdbcConfiguration basicJdbcConfiguration) {
      this.basicJdbcConfiguration = basicJdbcConfiguration;
      this.embeddedDatabaseConnection = JdbcDatabaseManager.get(this.getClass().getClassLoader());
   }

   public CalculatedSettings(BasicJdbcConfiguration basicJdbcConfiguration, ClassLoader classLoader) {
      this.basicJdbcConfiguration = basicJdbcConfiguration;
      this.embeddedDatabaseConnection = JdbcDatabaseManager.get(classLoader);
   }

   public String getDriverClassName() {
      String driverClassName = this.basicJdbcConfiguration.getConfiguredDriverClassName();
      if (this.calculatedDriverClassName == null || StringUtils.hasText(driverClassName)) {
         if (StringUtils.hasText(driverClassName)) {
            if (!this.driverClassIsPresent(driverClassName)) {
               throw new ConfigurationException(
                  String.format(
                     "Error configuring data source '%s'. The driver class '%s' was not found on the classpath",
                     this.basicJdbcConfiguration.getName(),
                     driverClassName
                  )
               );
            }

            this.calculatedDriverClassName = driverClassName;
         } else {
            String url = this.basicJdbcConfiguration.getUrl();
            if (StringUtils.hasText(url)) {
               JdbcDatabaseManager.findDatabase(url).ifPresent(db -> this.calculatedDriverClassName = db.getDriverClassName());
            }

            if (!StringUtils.hasText(this.calculatedDriverClassName) && this.embeddedDatabaseConnection.isPresent()) {
               this.calculatedDriverClassName = ((JdbcDatabaseManager.EmbeddedJdbcDatabase)this.embeddedDatabaseConnection.get()).getDriverClassName();
            }

            if (!StringUtils.hasText(this.calculatedDriverClassName)) {
               throw new ConfigurationException(
                  String.format("Error configuring data source '%s'. No driver class name specified", this.basicJdbcConfiguration.getName())
               );
            }
         }
      }

      return this.calculatedDriverClassName;
   }

   public String getUrl() {
      String url = this.basicJdbcConfiguration.getConfiguredUrl();
      if (this.calculatedUrl == null || StringUtils.hasText(url)) {
         this.calculatedUrl = url;
         if (!StringUtils.hasText(this.calculatedUrl) && this.embeddedDatabaseConnection.isPresent()) {
            this.calculatedUrl = ((JdbcDatabaseManager.EmbeddedJdbcDatabase)this.embeddedDatabaseConnection.get())
               .getUrl(this.basicJdbcConfiguration.getName());
         }

         if (!StringUtils.hasText(this.calculatedUrl)) {
            throw new ConfigurationException(String.format("Error configuring data source '%s'. No URL specified", this.basicJdbcConfiguration.getName()));
         }
      }

      return this.calculatedUrl;
   }

   public String getUsername() {
      String username = this.basicJdbcConfiguration.getConfiguredUsername();
      if (this.calculatedUsername == null || StringUtils.hasText(username)) {
         this.calculatedUsername = username;
         if (!StringUtils.hasText(this.calculatedUsername) && JdbcDatabaseManager.isEmbedded(this.basicJdbcConfiguration.getDriverClassName())) {
            this.calculatedUsername = "sa";
         }
      }

      return this.calculatedUsername;
   }

   public String getPassword() {
      String password = this.basicJdbcConfiguration.getConfiguredPassword();
      if (this.calculatedPassword == null || StringUtils.hasText(password)) {
         this.calculatedPassword = password;
         if (!StringUtils.hasText(this.calculatedPassword) && JdbcDatabaseManager.isEmbedded(this.basicJdbcConfiguration.getDriverClassName())) {
            this.calculatedPassword = "";
         }
      }

      return this.calculatedPassword;
   }

   public String getValidationQuery() {
      String validationQuery = this.basicJdbcConfiguration.getConfiguredValidationQuery();
      if (this.calculatedValidationQuery == null || StringUtils.hasText(validationQuery)) {
         this.calculatedValidationQuery = validationQuery;
         if (!StringUtils.hasText(this.calculatedValidationQuery)) {
            JdbcDatabaseManager.findDatabase(this.getUrl()).ifPresent(db -> this.calculatedValidationQuery = db.getValidationQuery());
         }
      }

      return this.calculatedValidationQuery;
   }

   private boolean driverClassIsPresent(String className) {
      return ClassUtils.isPresent(className, this.getClass().getClassLoader());
   }
}
