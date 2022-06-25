package io.micronaut.discovery;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultServiceInstanceIdGenerator$Definition
   extends AbstractInitializableBeanDefinition<DefaultServiceInstanceIdGenerator>
   implements BeanFactory<DefaultServiceInstanceIdGenerator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultServiceInstanceIdGenerator.class, "<init>", null, null, false
   );

   @Override
   public DefaultServiceInstanceIdGenerator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultServiceInstanceIdGenerator var4 = new DefaultServiceInstanceIdGenerator();
      return (DefaultServiceInstanceIdGenerator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultServiceInstanceIdGenerator var4 = (DefaultServiceInstanceIdGenerator)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultServiceInstanceIdGenerator$Definition() {
      this(DefaultServiceInstanceIdGenerator.class, $CONSTRUCTOR);
   }

   protected $DefaultServiceInstanceIdGenerator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultServiceInstanceIdGenerator$Definition$Reference.$ANNOTATION_METADATA,
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
