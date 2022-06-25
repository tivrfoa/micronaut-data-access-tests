package io.micronaut.http.client.interceptor.configuration;

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
class $DefaultClientVersioningConfiguration$Definition
   extends AbstractInitializableBeanDefinition<DefaultClientVersioningConfiguration>
   implements BeanFactory<DefaultClientVersioningConfiguration> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultClientVersioningConfiguration.class, "<init>", null, null, false
   );

   @Override
   public DefaultClientVersioningConfiguration build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultClientVersioningConfiguration var4 = new DefaultClientVersioningConfiguration();
      return (DefaultClientVersioningConfiguration)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         DefaultClientVersioningConfiguration var4 = (DefaultClientVersioningConfiguration)var3;
         if (this.containsPropertiesValue(var1, var2, "micronaut.http.client.versioning.default.headers")) {
            var4.setHeaders(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setHeaders",
                  Argument.of(List.class, "headerNames", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.http.client.versioning.default.headers",
                  null
               )
            );
         }

         if (this.containsPropertiesValue(var1, var2, "micronaut.http.client.versioning.default.parameters")) {
            var4.setParameters(
               (List<String>)super.getPropertyValueForSetter(
                  var1,
                  var2,
                  "setParameters",
                  Argument.of(List.class, "parameterNames", null, Argument.ofTypeVariable(String.class, "E")),
                  "micronaut.http.client.versioning.default.parameters",
                  null
               )
            );
         }
      }

      return super.injectBean(var1, var2, var3);
   }

   public $DefaultClientVersioningConfiguration$Definition() {
      this(DefaultClientVersioningConfiguration.class, $CONSTRUCTOR);
   }

   protected $DefaultClientVersioningConfiguration$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultClientVersioningConfiguration$Definition$Reference.$ANNOTATION_METADATA,
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
         true,
         true,
         false,
         false
      );
   }
}
