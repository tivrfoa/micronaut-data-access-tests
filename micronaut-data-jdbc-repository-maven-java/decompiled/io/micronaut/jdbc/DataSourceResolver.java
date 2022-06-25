package io.micronaut.jdbc;

import javax.sql.DataSource;

public interface DataSourceResolver {
   DataSourceResolver DEFAULT = new DataSourceResolver() {
   };

   default DataSource resolve(DataSource dataSource) {
      return dataSource;
   }
}
