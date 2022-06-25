package io.micronaut.management.endpoint.refresh;

import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Write;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import java.util.Map;
import java.util.Set;

@Endpoint("refresh")
public class RefreshEndpoint {
   private final Environment environment;
   private final ApplicationEventPublisher<RefreshEvent> eventPublisher;

   public RefreshEndpoint(Environment environment, ApplicationEventPublisher<RefreshEvent> eventPublisher) {
      this.environment = environment;
      this.eventPublisher = eventPublisher;
   }

   @Write
   public String[] refresh(@Nullable Boolean force) {
      if (force != null && force) {
         this.eventPublisher.publishEvent(new RefreshEvent());
         return new String[0];
      } else {
         Map<String, Object> changes = this.environment.refreshAndDiff();
         if (!changes.isEmpty()) {
            this.eventPublisher.publishEvent(new RefreshEvent(changes));
         }

         Set<String> keys = changes.keySet();
         return (String[])keys.toArray(new String[0]);
      }
   }
}
