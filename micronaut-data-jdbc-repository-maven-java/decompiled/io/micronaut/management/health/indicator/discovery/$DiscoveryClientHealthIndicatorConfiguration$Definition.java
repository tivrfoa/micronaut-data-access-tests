package io.micronaut.management.health.indicator.discovery;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DiscoveryClientHealthIndicatorConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DiscoveryClientHealthIndicatorConfiguration>
   implements BeanFactory<DiscoveryClientHealthIndicatorConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DiscoveryClientHealthIndicatorConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DiscoveryClientHealthIndicatorConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DiscoveryClientHealthIndicatorConfiguration var4 = new DiscoveryClientHealthIndicatorConfiguration();
      return (DiscoveryClientHealthIndicatorConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DiscoveryClientHealthIndicatorConfiguration var4 = (DiscoveryClientHealthIndicatorConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "endpoints.health.discovery-client.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(
                  var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "endpoints.health.discovery-client.enabled", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DiscoveryClientHealthIndicatorConfiguration$Definition() {
      this(DiscoveryClientHealthIndicatorConfiguration.class, $CONSTRUCTOR);
   }

   protected $DiscoveryClientHealthIndicatorConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DiscoveryClientHealthIndicatorConfiguration$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.of("javax.inject.Singleton"),
         false,
         false,
         false,
         true,
         false,
         true,
         false,
         false
      );
   }
}
