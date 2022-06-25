package io.micronaut.http.client.loadbalance;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.DiscoveryClient;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DiscoveryClientLoadBalancerFactory$Definition
   extends AbstractInitializableBeanDefinition<DiscoveryClientLoadBalancerFactory>
   implements BeanFactory<DiscoveryClientLoadBalancerFactory> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DiscoveryClientLoadBalancerFactory.class, "<init>", new Argument[]{Argument.of(DiscoveryClient.class, "discoveryClient")}, null, false
   );

   @Override
   public DiscoveryClientLoadBalancerFactory build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DiscoveryClientLoadBalancerFactory var4 = new DiscoveryClientLoadBalancerFactory(
         (DiscoveryClient)super.getBeanForConstructorArgument(var1, var2, 0, null)
      );
      return (DiscoveryClientLoadBalancerFactory)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DiscoveryClientLoadBalancerFactory var4 = (DiscoveryClientLoadBalancerFactory)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DiscoveryClientLoadBalancerFactory$Definition() {
      this(DiscoveryClientLoadBalancerFactory.class, $CONSTRUCTOR);
   }

   protected $DiscoveryClientLoadBalancerFactory$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DiscoveryClientLoadBalancerFactory$Definition$Reference.$ANNOTATION_METADATA,
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
