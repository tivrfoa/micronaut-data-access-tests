package io.micronaut.flyway.endpoint;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.flyway.FlywayConfigurationProperties;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Collection;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $FlywayEndpoint$Definition extends AbstractInitializableBeanDefinition<FlywayEndpoint> implements BeanFactory<FlywayEndpoint> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      FlywayEndpoint.class,
      "<init>",
      new Argument[]{
         Argument.of(ApplicationContext.class, "applicationContext"),
         Argument.of(Collection.class, "flywayConfigurationProperties", null, Argument.ofTypeVariable(FlywayConfigurationProperties.class, "E"))
      },
      null,
      false
   );

   @Override
   public FlywayEndpoint build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      FlywayEndpoint var4 = new FlywayEndpoint(
         var2,
         super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (FlywayEndpoint)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      if (this.containsProperties(var1, var2)) {
         FlywayEndpoint var4 = (FlywayEndpoint)var3;
      }

      return super.injectBean(var1, var2, var3);
   }

   public $FlywayEndpoint$Definition() {
      this(FlywayEndpoint.class, $CONSTRUCTOR);
   }

   protected $FlywayEndpoint$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $FlywayEndpoint$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $FlywayEndpoint$Definition$Exec(),
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
