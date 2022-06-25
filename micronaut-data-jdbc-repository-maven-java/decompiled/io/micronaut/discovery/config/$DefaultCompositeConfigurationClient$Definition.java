package io.micronaut.discovery.config;

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
class $DefaultCompositeConfigurationClient$Definition
   extends AbstractInitializableBeanDefinition<DefaultCompositeConfigurationClient>
   implements BeanFactory<DefaultCompositeConfigurationClient> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultCompositeConfigurationClient.class, "<init>", new Argument[]{Argument.of(ConfigurationClient[].class, "configurationClients")}, null, false
   );

   @Override
   public DefaultCompositeConfigurationClient build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultCompositeConfigurationClient var4 = new DefaultCompositeConfigurationClient(
         (ConfigurationClient[])super.getBeansOfTypeForConstructorArgument(var1, var2, 0, Argument.of(ConfigurationClient.class, null), null)
            .toArray(new ConfigurationClient[0])
      );
      return (DefaultCompositeConfigurationClient)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultCompositeConfigurationClient var4 = (DefaultCompositeConfigurationClient)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultCompositeConfigurationClient$Definition() {
      this(DefaultCompositeConfigurationClient.class, $CONSTRUCTOR);
   }

   protected $DefaultCompositeConfigurationClient$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultCompositeConfigurationClient$Definition$Reference.$ANNOTATION_METADATA,
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
         false,
         false,
         false
      );
   }
}
