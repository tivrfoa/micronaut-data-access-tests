package io.micronaut.management.endpoint.health;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpStatus;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HealthEndpoint$StatusConfiguration$Definition
   extends AbstractInitializableBeanDefinition<HealthEndpoint.StatusConfiguration>
   implements BeanFactory<HealthEndpoint.StatusConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HealthEndpoint.StatusConfiguration.class, "<init>", null, null, false
   );

   @Override
   public HealthEndpoint.StatusConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HealthEndpoint.StatusConfiguration var4 = new HealthEndpoint.StatusConfiguration();
      return (HealthEndpoint.StatusConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HealthEndpoint.StatusConfiguration var4 = (HealthEndpoint.StatusConfiguration)var3;
         if (this.containsPropertiesValue(var1, var2, "endpoints.health.status.http-mapping")) {
            var4.setHttpMapping(
               (Map<String, HttpStatus>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHttpMapping",
                  Argument.of(Map.class, "httpMapping", null, Argument.ofTypeVariable(String.class, "K"), Argument.of(HttpStatus.class, "V")),
                  "endpoints.health.status.http-mapping",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $HealthEndpoint$StatusConfiguration$Definition() {
      this(HealthEndpoint.StatusConfiguration.class, $CONSTRUCTOR);
   }

   protected $HealthEndpoint$StatusConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HealthEndpoint$StatusConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
