package io.micronaut.jdbc;

import java.util.Map;

public interface BasicJdbcConfiguration {
   String PREFIX = "datasources";

   String getName();

   String getConfiguredUrl();

   String getUrl();

   void setUrl(String url);

   String getConfiguredDriverClassName();

   String getDriverClassName();

   void setDriverClassName(String driverClassName);

   String getConfiguredUsername();

   String getUsername();

   void setUsername(String username);

   String getConfiguredPassword();

   String getPassword();

   void setPassword(String password);

   String getConfiguredValidationQuery();

   String getValidationQuery();

   void setDataSourceProperties(Map<String, ?> dsProperties);
}
