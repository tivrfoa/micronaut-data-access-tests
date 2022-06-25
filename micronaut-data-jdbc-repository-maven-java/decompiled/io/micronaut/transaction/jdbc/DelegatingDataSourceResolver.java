package io.micronaut.transaction.jdbc;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.jdbc.DataSourceResolver;
import jakarta.inject.Singleton;
import javax.sql.DataSource;

@Singleton
@Internal
@Requirements({@Requires(
   missingClasses = {"org.springframework.jdbc.datasource.DataSourceTransactionManager"}
), @Requires(
   classes = {DataSourceResolver.class}
)})
public class DelegatingDataSourceResolver implements DataSourceResolver {
   @Override
   public DataSource resolve(DataSource dataSource) {
      return DelegatingDataSource.unwrapDataSource(dataSource);
   }
}
