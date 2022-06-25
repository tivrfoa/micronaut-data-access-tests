package io.micronaut.web.router.version;

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
class $ConfigurationDefaultVersionProvider$Definition
   extends AbstractInitializableBeanDefinition<ConfigurationDefaultVersionProvider>
   implements BeanFactory<ConfigurationDefaultVersionProvider> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ConfigurationDefaultVersionProvider.class,
      "<init>",
      new Argument[]{Argument.of(RoutesVersioningConfiguration.class, "routesVersioningConfiguration")},
      null,
      false
   );

   @Override
   public ConfigurationDefaultVersionProvider build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ConfigurationDefaultVersionProvider var4 = new ConfigurationDefaultVersionProvider(
         (RoutesVersioningConfiguration)super.getBeanForConstructorArgument(var1, var2, 0, null)
      );
      return (ConfigurationDefaultVersionProvider)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ConfigurationDefaultVersionProvider var4 = (ConfigurationDefaultVersionProvider)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ConfigurationDefaultVersionProvider$Definition() {
      this(ConfigurationDefaultVersionProvider.class, $CONSTRUCTOR);
   }

   protected $ConfigurationDefaultVersionProvider$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ConfigurationDefaultVersionProvider$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false
      );
   }
}
