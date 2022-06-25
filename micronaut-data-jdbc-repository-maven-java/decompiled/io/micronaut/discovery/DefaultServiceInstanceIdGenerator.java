package io.micronaut.discovery;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.StringJoiner;

@Singleton
public class DefaultServiceInstanceIdGenerator implements ServiceInstanceIdGenerator {
   protected DefaultServiceInstanceIdGenerator() {
   }

   @NonNull
   @Override
   public String generateId(Environment environment, ServiceInstance serviceInstance) {
      Optional<String> cloudFoundryId = environment.getProperty("vcap.application.instance_id", String.class);
      if (cloudFoundryId.isPresent()) {
         return (String)cloudFoundryId.get();
      } else {
         StringJoiner joiner = new StringJoiner(":");
         String applicationName = serviceInstance.getId();
         joiner.add(applicationName);
         if (serviceInstance instanceof EmbeddedServerInstance) {
            EmbeddedServerInstance esi = (EmbeddedServerInstance)serviceInstance;
            Optional<String> id = esi.getEmbeddedServer().getApplicationConfiguration().getInstance().getId();
            if (id.isPresent()) {
               joiner.add((CharSequence)id.get());
            } else {
               joiner.add(String.valueOf(esi.getPort()));
            }
         } else {
            joiner.add(String.valueOf(serviceInstance.getPort()));
         }

         return joiner.toString();
      }
   }
}
