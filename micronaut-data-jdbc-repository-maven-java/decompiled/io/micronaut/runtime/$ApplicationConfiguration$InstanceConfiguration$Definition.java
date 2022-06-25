package io.micronaut.runtime;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Map;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ApplicationConfiguration$InstanceConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ApplicationConfiguration.InstanceConfiguration>
   implements BeanFactory<ApplicationConfiguration.InstanceConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ApplicationConfiguration.InstanceConfiguration.class, "<init>", null, null, false
   );

   @Override
   public ApplicationConfiguration.InstanceConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ApplicationConfiguration.InstanceConfiguration var4 = new ApplicationConfiguration.InstanceConfiguration();
      return (ApplicationConfiguration.InstanceConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ApplicationConfiguration.InstanceConfiguration var4 = (ApplicationConfiguration.InstanceConfiguration)var3;
         if (this.containsPropertyValue(var1, var2, "micronaut.application.instance.id")) {
            var4.setId((String)super.getPropertyValueForSetter(var1, var2, "setId", Argument.of(String.class, "id"), "micronaut.application.instance.id", null));
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.application.instance.metadata")) {
            var4.setMetadata(
               (Map<String, String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setMetadata",
                  Argument.of(Map.class, "metadata", null, Argument.ofTypeVariable(String.class, "K"), Argument.ofTypeVariable(String.class, "V")),
                  "micronaut.application.instance.metadata",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.application.instance.group")) {
            var4.setGroup(
               (String)super.getPropertyValueForSetter(var1, var2, "setGroup", Argument.of(String.class, "group"), "micronaut.application.instance.group", null)
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.application.instance.zone")) {
            var4.setZone(
               (String)super.getPropertyValueForSetter(var1, var2, "setZone", Argument.of(String.class, "zone"), "micronaut.application.instance.zone", null)
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ApplicationConfiguration$InstanceConfiguration$Definition() {
      this(ApplicationConfiguration.InstanceConfiguration.class, $CONSTRUCTOR);
   }

   protected $ApplicationConfiguration$InstanceConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ApplicationConfiguration$InstanceConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
