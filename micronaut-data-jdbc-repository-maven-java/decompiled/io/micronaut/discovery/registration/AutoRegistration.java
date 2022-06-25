package io.micronaut.discovery.registration;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.event.AbstractServiceInstanceEvent;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.discovery.event.ServiceStoppedEvent;
import io.micronaut.discovery.exceptions.DiscoveryException;
import io.micronaut.health.HealthStatus;
import io.micronaut.health.HeartbeatEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AutoRegistration implements ApplicationEventListener<AbstractServiceInstanceEvent> {
   protected static final Logger LOG = LoggerFactory.getLogger(AutoRegistration.class);
   private static final Pattern APPLICATION_NAME_PATTERN = Pattern.compile("^[a-zA-Z][\\w\\d-]*[a-zA-Z\\d]$");
   protected final AtomicBoolean registered = new AtomicBoolean(false);
   private final RegistrationConfiguration registrationConfiguration;

   protected AutoRegistration(RegistrationConfiguration registrationConfiguration) {
      this.registrationConfiguration = registrationConfiguration;
   }

   public void onApplicationEvent(AbstractServiceInstanceEvent event) {
      if (this.registrationConfiguration.isEnabled()) {
         if (event instanceof ServiceReadyEvent) {
            this.register(event.getSource());
         } else if (event instanceof ServiceStoppedEvent) {
            if (this.registrationConfiguration.isDeregister()) {
               this.deregister(event.getSource());
            }
         } else if (event instanceof HeartbeatEvent) {
            HeartbeatEvent heartbeatEvent = (HeartbeatEvent)event;
            this.pulsate(event.getSource(), heartbeatEvent.getStatus());
         }
      }

   }

   protected abstract void pulsate(ServiceInstance instance, HealthStatus status);

   protected abstract void deregister(ServiceInstance instance);

   protected abstract void register(ServiceInstance instance);

   protected void validateApplicationName(String name) {
      String typeDescription = "Application name";
      this.validateName(name, typeDescription);
   }

   protected void validateName(String name, String typeDescription) {
      if (!APPLICATION_NAME_PATTERN.matcher(name).matches()) {
         throw new DiscoveryException(
            typeDescription
               + " ["
               + name
               + "] must start with a letter, end with a letter or digit and contain only letters, digits or hyphens. Example: foo-bar"
         );
      }
   }
}
