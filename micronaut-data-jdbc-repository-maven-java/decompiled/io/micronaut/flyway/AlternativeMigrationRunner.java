package io.micronaut.flyway;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import javax.sql.DataSource;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlternativeMigrationRunner extends AbstractFlywayMigration implements BeanCreatedEventListener<FlywayConfigurationProperties> {
   private static final Logger LOG = LoggerFactory.getLogger(Condition.class);

   public AlternativeMigrationRunner(ApplicationContext applicationContext, ApplicationEventPublisher eventPublisher) {
      super(applicationContext, eventPublisher);
   }

   public FlywayConfigurationProperties onCreated(BeanCreatedEvent<FlywayConfigurationProperties> event) {
      FlywayConfigurationProperties config = event.getBean();
      String name = config.getNameQualifier();
      if (config.isEnabled()) {
         if (config.hasAlternativeDatabaseConfiguration()) {
            DataSource dataSource = new DriverDataSource(
               Thread.currentThread().getContextClassLoader(), null, config.getUrl(), config.getUser(), config.getPassword()
            );
            this.run(config, dataSource);
         } else if (!this.applicationContext.containsBean(DataSource.class, Qualifiers.byName(name))) {
            LOG.debug("* Flyway bean not created for identifier [{}] because no data source was found with a named qualifier of the same name.", name);
         }
      }

      return config;
   }
}
