package io.micronaut.discovery.config;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.naming.Described;
import io.micronaut.core.util.ArrayUtils;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Primary
@BootstrapContextCompatible
public class DefaultCompositeConfigurationClient implements ConfigurationClient {
   private final ConfigurationClient[] configurationClients;

   public DefaultCompositeConfigurationClient(ConfigurationClient[] configurationClients) {
      this.configurationClients = configurationClients;
   }

   @Override
   public String getDescription() {
      return this.toString();
   }

   @Override
   public Publisher<PropertySource> getPropertySources(Environment environment) {
      if (ArrayUtils.isEmpty(this.configurationClients)) {
         return Flux.empty();
      } else {
         List<Publisher<PropertySource>> publishers = (List)Arrays.stream(this.configurationClients)
            .map(configurationClient -> configurationClient.getPropertySources(environment))
            .collect(Collectors.toList());
         return Flux.merge(publishers);
      }
   }

   public String toString() {
      return "compositeConfigurationClient("
         + (String)Arrays.stream(this.configurationClients).map(Described::getDescription).collect(Collectors.joining(","))
         + ")";
   }
}
