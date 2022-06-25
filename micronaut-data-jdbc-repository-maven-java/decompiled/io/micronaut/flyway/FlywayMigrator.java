package io.micronaut.flyway;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class FlywayMigrator extends AbstractFlywayMigration {
   FlywayMigrator(ApplicationContext applicationContext, ApplicationEventPublisher eventPublisher) {
      super(applicationContext, eventPublisher);
   }

   @Override
   public void run(FlywayConfigurationProperties config, DataSource dataSource) {
      super.forceRun(config, dataSource);
   }
}
