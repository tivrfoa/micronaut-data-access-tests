package io.micronaut.flyway.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.flyway.FlywayConfigurationProperties;

public class MigrationFinishedEvent extends ApplicationEvent {
   public MigrationFinishedEvent(FlywayConfigurationProperties config) {
      super(config);
   }
}
