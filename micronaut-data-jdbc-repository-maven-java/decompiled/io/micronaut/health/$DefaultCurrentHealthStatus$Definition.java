package io.micronaut.health;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultCurrentHealthStatus$Definition
   extends AbstractInitializableBeanDefinition<DefaultCurrentHealthStatus>
   implements BeanFactory<DefaultCurrentHealthStatus> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultCurrentHealthStatus.class, "<init>", null, null, false
   );

   @Override
   public DefaultCurrentHealthStatus build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultCurrentHealthStatus var4 = new DefaultCurrentHealthStatus();
      return (DefaultCurrentHealthStatus)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultCurrentHealthStatus var4 = (DefaultCurrentHealthStatus)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultCurrentHealthStatus$Definition() {
      this(DefaultCurrentHealthStatus.class, $CONSTRUCTOR);
   }

   protected $DefaultCurrentHealthStatus$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultCurrentHealthStatus$Definition$Reference.$ANNOTATION_METADATA,
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
