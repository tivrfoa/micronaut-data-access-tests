package io.micronaut.management.endpoint;

import io.micronaut.context.BeanContext;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.management.endpoint.annotation.Endpoint;
import java.util.Optional;

@Introspected
public class EndpointEnabledCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      AnnotationMetadataProvider component = context.getComponent();
      AnnotationMetadata annotationMetadata = component.getAnnotationMetadata();
      if (annotationMetadata.hasDeclaredAnnotation(Endpoint.class)) {
         Boolean defaultEnabled = (Boolean)annotationMetadata.booleanValue(Endpoint.class, "defaultEnabled").orElse(true);
         String prefix = (String)annotationMetadata.stringValue(Endpoint.class, "prefix").orElse("endpoints");
         String id = (String)annotationMetadata.stringValue(Endpoint.class).orElse(null);
         String defaultId = (String)annotationMetadata.stringValue(Endpoint.class, "defaultConfigurationId").orElse("all");
         BeanContext beanContext = context.getBeanContext();
         if (beanContext instanceof PropertyResolver) {
            PropertyResolver propertyResolver = (PropertyResolver)beanContext;
            Optional<Boolean> enabled = propertyResolver.getProperty(String.format("%s.%s.enabled", prefix, id), Boolean.class);
            if (enabled.isPresent()) {
               return enabled.get();
            }

            enabled = propertyResolver.getProperty(String.format("%s.%s.enabled", prefix, defaultId), Boolean.class);
            return enabled.orElse(defaultEnabled);
         }
      }

      return true;
   }
}
