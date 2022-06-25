package io.micronaut.http.client.loadbalance;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $LoadBalancerConverters$Definition extends AbstractInitializableBeanDefinition<LoadBalancerConverters> implements BeanFactory<LoadBalancerConverters> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      LoadBalancerConverters.class, "<init>", null, null, false
   );

   @Override
   public LoadBalancerConverters build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      LoadBalancerConverters var4 = new LoadBalancerConverters();
      return (LoadBalancerConverters)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      LoadBalancerConverters var4 = (LoadBalancerConverters)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $LoadBalancerConverters$Definition() {
      this(LoadBalancerConverters.class, $CONSTRUCTOR);
   }

   protected $LoadBalancerConverters$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $LoadBalancerConverters$Definition$Reference.$ANNOTATION_METADATA,
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
