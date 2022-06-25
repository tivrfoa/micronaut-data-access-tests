package io.micronaut.web.router.version.resolution;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ParameterVersionResolverConfiguration$Definition
   extends AbstractInitializableBeanDefinition<ParameterVersionResolverConfiguration>
   implements BeanFactory<ParameterVersionResolverConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ParameterVersionResolverConfiguration.class, "<init>", null, null, false
   );

   @Override
   public ParameterVersionResolverConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ParameterVersionResolverConfiguration var4 = new ParameterVersionResolverConfiguration();
      return (ParameterVersionResolverConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         ParameterVersionResolverConfiguration var4 = (ParameterVersionResolverConfiguration)var3;
         if (this.containsPropertiesValue(var1, var2, "micronaut.router.versioning.parameter.names")) {
            var4.setNames(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setNames",
                  Argument.of(List.class, "names", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.router.versioning.parameter.names",
                  null
               )
            );
         }

         if (this.containsPropertyValue(var1, var2, "micronaut.router.versioning.parameter.enabled")) {
            var4.setEnabled(
               super.getPropertyValueForSetter(
                  var1, var2, "setEnabled", Argument.of(Boolean.TYPE, "enabled"), "micronaut.router.versioning.parameter.enabled", null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $ParameterVersionResolverConfiguration$Definition() {
      this(ParameterVersionResolverConfiguration.class, $CONSTRUCTOR);
   }

   protected $ParameterVersionResolverConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ParameterVersionResolverConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
