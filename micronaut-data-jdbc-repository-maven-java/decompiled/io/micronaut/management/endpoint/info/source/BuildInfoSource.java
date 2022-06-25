package io.micronaut.management.endpoint.info.source;

import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.management.endpoint.info.InfoEndpoint;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
@Requirements({@Requires(
   beans = {InfoEndpoint.class}
), @Requires(
   property = "endpoints.info.build.enabled",
   notEquals = "false"
)})
public class BuildInfoSource implements PropertiesInfoSource {
   private static final String EXTENSION = ".properties";
   private static final String PREFIX = "classpath:";
   private final String buildPropertiesPath;
   private final ResourceResolver resourceResolver;
   private final Supplier<Optional<PropertySource>> supplier;

   public BuildInfoSource(
      ResourceResolver resourceResolver, @Value("${endpoints.info.build.location:META-INF/build-info.properties}") String buildPropertiesPath
   ) {
      this.resourceResolver = resourceResolver;
      this.supplier = SupplierUtil.memoized(this::retrieveBuildInfo);
      this.buildPropertiesPath = buildPropertiesPath;
   }

   @Override
   public Publisher<PropertySource> getSource() {
      Optional<PropertySource> propertySource = (Optional)this.supplier.get();
      return (Publisher<PropertySource>)propertySource.map(Flux::just).orElse(Flux.empty());
   }

   private Optional<PropertySource> retrieveBuildInfo() {
      return this.retrievePropertiesPropertySource(this.buildPropertiesPath, "classpath:", ".properties", this.resourceResolver);
   }
}
