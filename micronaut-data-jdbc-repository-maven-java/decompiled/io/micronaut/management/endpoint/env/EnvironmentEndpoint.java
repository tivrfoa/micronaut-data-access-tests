package io.micronaut.management.endpoint.env;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.Ordered;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.management.endpoint.annotation.Selector;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Endpoint(
   id = "env",
   defaultEnabled = false
)
public class EnvironmentEndpoint {
   public static final String NAME = "env";
   private static final String MASK_VALUE = "*****";
   private final Environment environment;
   private final EnvironmentEndpointFilter environmentFilter;

   public EnvironmentEndpoint(Environment environment) {
      this(environment, null);
   }

   @Inject
   public EnvironmentEndpoint(Environment environment, @Nullable EnvironmentEndpointFilter environmentFilter) {
      this.environment = environment;
      this.environmentFilter = environmentFilter;
   }

   @Read
   public Map<String, Object> getEnvironmentInfo() {
      EnvironmentFilterSpecification filter = this.createFilterSpecification();
      Map<String, Object> result = new LinkedHashMap();
      result.put("activeEnvironments", this.environment.getActiveNames());
      result.put("packages", this.environment.getPackages());
      Collection<Map<String, Object>> propertySources = new ArrayList();
      this.environment
         .getPropertySources()
         .stream()
         .sorted(Comparator.comparing(Ordered::getOrder))
         .map(ps -> this.buildPropertySourceInfo(ps, filter))
         .forEach(propertySources::add);
      result.put("propertySources", propertySources);
      return result;
   }

   @Read
   public Map<String, Object> getProperties(@Selector String propertySourceName) {
      EnvironmentFilterSpecification filter = this.createFilterSpecification();
      return (Map<String, Object>)this.environment
         .getPropertySources()
         .stream()
         .filter(ps -> ps.getName().equals(propertySourceName))
         .findFirst()
         .map(ps -> this.buildPropertySourceInfo(ps, filter))
         .orElse(null);
   }

   private EnvironmentFilterSpecification createFilterSpecification() {
      EnvironmentFilterSpecification filter = new EnvironmentFilterSpecification();
      if (this.environmentFilter != null) {
         this.environmentFilter.specifyFiltering(filter);
      }

      return filter;
   }

   private Map<String, Object> getAllProperties(PropertySource propertySource, EnvironmentFilterSpecification filter) {
      Map<String, Object> properties = new LinkedHashMap();
      propertySource.forEach(k -> {
         EnvironmentFilterSpecification.FilterResult test = filter.test(k);
         if (test != EnvironmentFilterSpecification.FilterResult.HIDE) {
            properties.put(k, test == EnvironmentFilterSpecification.FilterResult.MASK ? "*****" : propertySource.get(k));
         }

      });
      return properties;
   }

   private Map<String, Object> buildPropertySourceInfo(PropertySource propertySource, EnvironmentFilterSpecification filter) {
      Map<String, Object> propertySourceInfo = new LinkedHashMap();
      propertySourceInfo.put("name", propertySource.getName());
      propertySourceInfo.put("order", propertySource.getOrder());
      propertySourceInfo.put("convention", propertySource.getConvention().name());
      propertySourceInfo.put("properties", this.getAllProperties(propertySource, filter));
      return propertySourceInfo;
   }
}
