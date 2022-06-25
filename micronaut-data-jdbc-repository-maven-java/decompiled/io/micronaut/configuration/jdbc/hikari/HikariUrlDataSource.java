package io.micronaut.configuration.jdbc.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class HikariUrlDataSource extends HikariDataSource {
   public HikariUrlDataSource(HikariConfig configuration) {
      super(configuration);
   }

   public String getUrl() {
      return this.getJdbcUrl();
   }

   public void setUrl(String url) {
      this.setJdbcUrl(url);
   }
}
