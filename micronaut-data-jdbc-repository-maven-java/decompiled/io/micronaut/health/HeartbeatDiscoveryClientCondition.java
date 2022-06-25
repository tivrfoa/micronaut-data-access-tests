package io.micronaut.health;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.discovery.CompositeDiscoveryClient;
import io.micronaut.discovery.DiscoveryClient;

@Introspected
@ReflectiveAccess
public final class HeartbeatDiscoveryClientCondition implements Condition {
   @Override
   public boolean matches(ConditionContext context) {
      boolean hasDiscovery = context.getBeanContext()
         .getBeanDefinitions(DiscoveryClient.class)
         .stream()
         .filter(bd -> !CompositeDiscoveryClient.class.isAssignableFrom(bd.getBeanType()))
         .findFirst()
         .isPresent();
      if (hasDiscovery) {
         return true;
      } else {
         Boolean enabled = (Boolean)context.getProperty("micronaut.heartbeat.enabled", ConversionContext.BOOLEAN).orElse(Boolean.FALSE);
         if (!enabled) {
            context.fail("Heartbeat not enabled since no Discovery client active");
         }

         return enabled;
      }
   }
}
