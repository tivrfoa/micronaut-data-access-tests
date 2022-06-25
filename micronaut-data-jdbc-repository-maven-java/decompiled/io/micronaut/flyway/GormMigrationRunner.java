package io.micronaut.flyway;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import javax.sql.DataSource;
import org.grails.orm.hibernate.HibernateDatastore;
import org.grails.orm.hibernate.connections.HibernateConnectionSource;

@Singleton
@Requirements({@Requires(
   classes = {HibernateDatastore.class}
), @Requires(
   property = "data-source"
)})
public class GormMigrationRunner extends AbstractFlywayMigration implements BeanCreatedEventListener<HibernateDatastore> {
   GormMigrationRunner(ApplicationContext applicationContext, ApplicationEventPublisher eventPublisher) {
      super(applicationContext, eventPublisher);
   }

   public HibernateDatastore onCreated(BeanCreatedEvent<HibernateDatastore> event) {
      HibernateDatastore hibernateDatastore = (HibernateDatastore)event.getBean();
      hibernateDatastore.getConnectionSources()
         .forEach(
            connectionSource -> {
               String qualifier = connectionSource.getName();
               DataSource dataSource = ((HibernateConnectionSource)connectionSource).getDataSource();
               this.applicationContext
                  .findBean(FlywayConfigurationProperties.class, Qualifiers.byName(qualifier))
                  .ifPresent(flywayConfig -> this.run(flywayConfig, dataSource));
            }
         );
      return hibernateDatastore;
   }
}
