package io.micronaut.management.endpoint.info.source;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.MapPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.management.endpoint.info.InfoEndpoint;
import io.micronaut.management.endpoint.info.InfoSource;
import io.micronaut.runtime.context.scope.Refreshable;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Refreshable
@Requirements({@Requires(
   beans = {InfoEndpoint.class}
), @Requires(
   property = "endpoints.info.config.enabled",
   notEquals = "false"
)})
public class ConfigurationInfoSource implements InfoSource {
   private final Environment environment;
   private final Supplier<MapPropertySource> supplier;

   public ConfigurationInfoSource(Environment environment) {
      this.environment = environment;
      this.supplier = SupplierUtil.memoized(this::retrieveConfigurationInfo);
   }

   @Override
   public Publisher<PropertySource> getSource() {
      return Flux.just((PropertySource)this.supplier.get());
   }

   private MapPropertySource retrieveConfigurationInfo() {
      return new MapPropertySource("info", (Map)this.environment.getProperty("info", Map.class).orElse(Collections.emptyMap()));
   }
}
