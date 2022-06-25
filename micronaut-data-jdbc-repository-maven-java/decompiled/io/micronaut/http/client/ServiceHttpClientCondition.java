package io.micronaut.http.client;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.naming.Named;
import io.micronaut.core.value.ValueResolver;
import java.util.Optional;

@Internal
final class ServiceHttpClientCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      AnnotationMetadataProvider component = context.getComponent();
      BeanContext beanContext = context.getBeanContext();
      if (beanContext instanceof ApplicationContext) {
         Environment env = ((ApplicationContext)beanContext).getEnvironment();
         if (component instanceof ValueResolver) {
            Optional<String> optional = ((ValueResolver)component).get(Named.class.getName(), String.class);
            if (optional.isPresent()) {
               String serviceName = (String)optional.get();
               String urlProp = "micronaut.http.services." + serviceName + ".url";
               return env.containsProperty(urlProp) || env.containsProperty(urlProp + "s");
            }
         }
      }

      return true;
   }
}
