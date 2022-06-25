package io.micronaut.management.endpoint.info.source;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ConfigurationInfoSource$Definition extends AbstractInitializableBeanDefinition<ConfigurationInfoSource> implements BeanFactory<ConfigurationInfoSource> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ConfigurationInfoSource.class, "<init>", new Argument[]{Argument.of(Environment.class, "environment")}, null, false
   );

   @Override
   public ConfigurationInfoSource build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ConfigurationInfoSource var4 = new ConfigurationInfoSource((Environment)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (ConfigurationInfoSource)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ConfigurationInfoSource var4 = (ConfigurationInfoSource)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ConfigurationInfoSource$Definition() {
      this(ConfigurationInfoSource.class, $CONSTRUCTOR);
   }

   protected $ConfigurationInfoSource$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ConfigurationInfoSource$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         new $ConfigurationInfoSource$Definition$Exec(),
         null,
         Optional.of("io.micronaut.runtime.context.scope.Refreshable"),
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }
}
