package io.micronaut.flyway;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.flyway.event.MigrationFinishedEvent;
import io.micronaut.flyway.event.SchemaCleanedEvent;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class AbstractFlywayMigration {
   private static final Logger LOG = LoggerFactory.getLogger(AbstractFlywayMigration.class);
   protected final ApplicationContext applicationContext;
   private final ApplicationEventPublisher eventPublisher;

   AbstractFlywayMigration(ApplicationContext applicationContext, ApplicationEventPublisher eventPublisher) {
      this.applicationContext = applicationContext;
      this.eventPublisher = eventPublisher;
   }

   void run(FlywayConfigurationProperties config, DataSource dataSource) {
      if (config.isEnabled()) {
         this.forceRun(config, dataSource);
      }

   }

   void forceRun(FlywayConfigurationProperties config, DataSource dataSource) {
      FluentConfiguration fluentConfiguration = config.getFluentConfiguration();
      fluentConfiguration.dataSource(dataSource);
      Flyway flyway = fluentConfiguration.load();
      this.applicationContext.registerSingleton(Flyway.class, flyway, Qualifiers.byName(config.getNameQualifier()), false);
      if (config.isAsync()) {
         this.runAsync(config, flyway);
      } else {
         this.runFlyway(config, flyway);
      }

   }

   private void runFlyway(FlywayConfigurationProperties config, Flyway flyway) {
      if (config.isCleanSchema()) {
         LOG.info("Cleaning schema for database with qualifier [{}]", config.getNameQualifier());
         flyway.clean();
         this.eventPublisher.publishEvent(new SchemaCleanedEvent(config));
      }

      LOG.info("Running migrations for database with qualifier [{}]", config.getNameQualifier());
      flyway.migrate();
      this.eventPublisher.publishEvent(new MigrationFinishedEvent(config));
   }

   @Async("io")
   void runAsync(FlywayConfigurationProperties config, Flyway flyway) {
      this.runFlyway(config, flyway);
   }
}
