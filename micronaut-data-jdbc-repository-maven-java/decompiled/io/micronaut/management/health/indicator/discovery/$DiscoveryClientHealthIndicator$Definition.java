package io.micronaut.management.health.indicator.discovery;

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
class $DiscoveryClientHealthIndicator$Definition
   extends AbstractInitializableBeanDefinition<DiscoveryClientHealthIndicator>
   implements BeanFactory<DiscoveryClientHealthIndicator> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DiscoveryClientHealthIndicator.class, "<init>", new Argument[]{Argument.of(DiscoveryClient.class, "discoveryClient")}, null, false
   );

   @Override
   public DiscoveryClientHealthIndicator build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DiscoveryClientHealthIndicator var4 = new DiscoveryClientHealthIndicator((DiscoveryClient)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (DiscoveryClientHealthIndicator)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DiscoveryClientHealthIndicator var4 = (DiscoveryClientHealthIndicator)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DiscoveryClientHealthIndicator$Definition() {
      this(DiscoveryClientHealthIndicator.class, $CONSTRUCTOR);
   }

   protected $DiscoveryClientHealthIndicator$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DiscoveryClientHealthIndicator$Definition$Reference.$ANNOTATION_METADATA,
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
