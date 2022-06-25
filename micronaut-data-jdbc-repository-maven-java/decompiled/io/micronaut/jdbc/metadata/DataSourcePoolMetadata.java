package io.micronaut.jdbc.metadata;

import javax.sql.DataSource;

public interface DataSourcePoolMetadata<T extends DataSource> {
   T getDataSource();

   Integer getIdle();

   Float getUsage();

   Integer getActive();

   Integer getMax();

   Integer getMin();

   String getValidationQuery();

   Boolean getDefaultAutoCommit();
}
