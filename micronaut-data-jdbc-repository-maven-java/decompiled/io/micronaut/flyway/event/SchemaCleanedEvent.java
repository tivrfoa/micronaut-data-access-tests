package io.micronaut.flyway.event;

import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.flyway.FlywayConfigurationProperties;

public class SchemaCleanedEvent extends ApplicationEvent {
   public SchemaCleanedEvent(FlywayConfigurationProperties config) {
      super(config);
   }
}
