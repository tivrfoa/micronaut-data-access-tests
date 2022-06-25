package io.micronaut.jdbc.spring;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.jdbc.DataSourceResolver;
import jakarta.inject.Singleton;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

@Singleton
@Internal
@Requires(
   classes = {DelegatingDataSource.class}
)
public final class SpringDataSourceResolver implements DataSourceResolver {
   @Override
   public DataSource resolve(DataSource dataSource) {
      while(dataSource instanceof DelegatingDataSource) {
         dataSource = ((DelegatingDataSource)dataSource).getTargetDataSource();
      }

      return dataSource;
   }
}
