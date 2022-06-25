package io.micronaut.health;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.time.Duration;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HeartbeatConfiguration$Definition extends AbstractInitializableBeanDefinition<HeartbeatConfiguration> implements BeanFactory<HeartbeatConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HeartbeatConfiguration.class, "<init>", null, null, false
   );

   @Override
   public HeartbeatConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HeartbeatConfiguration var4 = new HeartbeatConfiguration();
      return (HeartbeatConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         HeartbeatConfiguration var4 = (HeartbeatConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.heartbeat.interval")) {
            var4.setInterval(
               (Duration)super.getPropertyValueForSetter(
                  var1, var2, "setInterval", Argument.of(Duration.class, "interval"), "micronaut.heartbeat.interval", null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.heartbeat.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.heartbeat.enabled", null)
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $HeartbeatConfiguration$Definition() {
      this(HeartbeatConfiguration.class, $CONSTRUCTOR);
   }

   protected $HeartbeatConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HeartbeatConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
