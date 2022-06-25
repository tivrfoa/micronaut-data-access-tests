package io.micronaut.configuration.jdbc.hikari;

import com.zaxxer.hikari.HikariConfig;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.jdbc.BasicJdbcConfiguration;
import io.micronaut.jdbc.CalculatedSettings;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;

@EachProperty(
   value = "datasources",
   primary = "default"
)
public class DatasourceConfiguration extends HikariConfig implements BasicJdbcConfiguration {
   private CalculatedSettings calculatedSettings;
   private String name;
   private boolean automaticValidationQuery = true;

   public DatasourceConfiguration(@Parameter String name) {
      this.name = name;
      this.calculatedSettings = new CalculatedSettings(this);
   }

   @PostConstruct
   void postConstruct() {
      if (this.getConfiguredUrl() == null) {
         this.setUrl(this.getUrl());
      }

      if (this.getConfiguredDriverClassName() == null) {
         this.setDriverClassName(this.getDriverClassName());
      }

      if (this.getConfiguredUsername() == null) {
         this.setUsername(this.getUsername());
      }

      if (this.getConfiguredPassword() == null) {
         this.setPassword(this.getPassword());
      }

      if (this.getConfiguredValidationQuery() == null && this.isAutomaticValidationQuery()) {
         this.setValidationQuery(this.getValidationQuery());
      }

   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getConfiguredUrl() {
      return this.getJdbcUrl();
   }

   @Override
   public String getUrl() {
      return this.calculatedSettings.getUrl();
   }

   @Override
   public void setUrl(String url) {
      this.setJdbcUrl(url);
   }

   @Override
   public String getConfiguredDriverClassName() {
      return super.getDriverClassName();
   }

   @Override
   public String getDriverClassName() {
      return this.calculatedSettings.getDriverClassName();
   }

   @Override
   public String getConfiguredUsername() {
      return super.getUsername();
   }

   @Override
   public String getUsername() {
      return this.calculatedSettings.getUsername();
   }

   @Override
   public String getConfiguredPassword() {
      return super.getPassword();
   }

   @Override
   public String getPassword() {
      return this.calculatedSettings.getPassword();
   }

   @Override
   public String getConfiguredValidationQuery() {
      return this.getConnectionTestQuery();
   }

   @Override
   public String getValidationQuery() {
      return this.calculatedSettings.getValidationQuery();
   }

   public void setValidationQuery(String validationQuery) {
      this.setConnectionTestQuery(validationQuery);
   }

   public String getJndiName() {
      return this.getDataSourceJNDI();
   }

   public void setJndiName(String jndiName) {
      this.setDataSourceJNDI(jndiName);
   }

   @Override
   public void setDataSourceProperties(
      @MapFormat(transformation = MapFormat.MapTransformation.FLAT,keyFormat = StringConvention.RAW) Map<String, ?> dsProperties
   ) {
      super.getDataSourceProperties().putAll(dsProperties);
   }

   @Deprecated
   @Override
   public void setDataSourceProperties(Properties dsProperties) {
   }

   public boolean isAutomaticValidationQuery() {
      return this.automaticValidationQuery;
   }

   public void setAutomaticValidationQuery(boolean automaticValidationQuery) {
      this.automaticValidationQuery = automaticValidationQuery;
   }
}
