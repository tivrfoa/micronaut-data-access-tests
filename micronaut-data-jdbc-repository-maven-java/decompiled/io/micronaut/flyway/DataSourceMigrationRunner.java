package io.micronaut.flyway;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jdbc.DataSourceResolver;
import jakarta.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class DataSourceMigrationRunner extends AbstractFlywayMigration implements BeanCreatedEventListener<DataSource> {
   private final DataSourceResolver dataSourceResolver;

   public DataSourceMigrationRunner(
      ApplicationContext applicationContext, ApplicationEventPublisher eventPublisher, @Nullable DataSourceResolver dataSourceResolver
   ) {
      super(applicationContext, eventPublisher);
      this.dataSourceResolver = dataSourceResolver != null ? dataSourceResolver : DataSourceResolver.DEFAULT;
   }

   public DataSource onCreated(BeanCreatedEvent<DataSource> event) {
      DataSource dataSource = (DataSource)event.getBean();
      if (event.getBeanDefinition() instanceof NameResolver) {
         ((NameResolver)event.getBeanDefinition())
            .resolveName()
            .flatMap(name -> this.applicationContext.findBean(FlywayConfigurationProperties.class, Qualifiers.byName(name)))
            .ifPresent(flywayConfig -> {
               DataSource unwrappedDataSource = this.dataSourceResolver.resolve(dataSource);
               this.run(flywayConfig, unwrappedDataSource);
            });
      }

      return dataSource;
   }
}
