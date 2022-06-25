package io.micronaut.management.endpoint;

import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Sensitive;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class EndpointSensitivityProcessor implements ExecutableMethodProcessor<Endpoint> {
   private final List<EndpointConfiguration> endpointConfigurations;
   private final EndpointDefaultConfiguration defaultConfiguration;
   private final PropertyResolver propertyResolver;
   private Map<ExecutableMethod, Boolean> endpointMethods = new HashMap();

   @Inject
   public EndpointSensitivityProcessor(
      List<EndpointConfiguration> endpointConfigurations, EndpointDefaultConfiguration defaultConfiguration, PropertyResolver propertyResolver
   ) {
      this.endpointConfigurations = CollectionUtils.unmodifiableList(endpointConfigurations);
      this.defaultConfiguration = defaultConfiguration;
      this.propertyResolver = propertyResolver;
   }

   public Map<ExecutableMethod, Boolean> getEndpointMethods() {
      return this.endpointMethods;
   }

   @Override
   public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
      Optional<String> optionalId = beanDefinition.stringValue(Endpoint.class);
      optionalId.ifPresent(
         id -> {
            boolean sensitive;
            if (method.hasDeclaredAnnotation(Sensitive.class)) {
               String prefix = (String)beanDefinition.stringValue(Endpoint.class, "prefix").orElse("endpoints");
               sensitive = method.booleanValue(Sensitive.class)
                  .orElseGet(
                     () -> {
                        boolean defaultValue = method.booleanValue(Sensitive.class, "defaultValue").orElse(true);
                        return this.propertyResolver != null
                           ? (Boolean)method.stringValue(Sensitive.class, "property")
                              .map(key -> (Boolean)this.propertyResolver.get(prefix + "." + id + "." + key, Boolean.class).orElse(defaultValue))
                              .orElse(defaultValue)
                           : defaultValue;
                     }
                  );
            } else {
               EndpointConfiguration configuration = (EndpointConfiguration)this.endpointConfigurations
                  .stream()
                  .filter(c -> c.getId().equals(id))
                  .findFirst()
                  .orElseGet(() -> new EndpointConfiguration(id, this.defaultConfiguration));
               sensitive = configuration.isSensitive()
                  .orElseGet(
                     () -> (Boolean)beanDefinition.booleanValue(Endpoint.class, "defaultSensitive")
                           .orElseGet(() -> (Boolean)beanDefinition.getDefaultValue(Endpoint.class, "defaultSensitive", Boolean.class).orElse(true))
                  );
            }
   
            this.endpointMethods.put(method, sensitive);
         }
      );
   }
}
