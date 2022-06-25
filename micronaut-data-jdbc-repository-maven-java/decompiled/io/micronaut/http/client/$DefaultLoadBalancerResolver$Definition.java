package io.micronaut.http.client;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.ServiceInstanceList;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultLoadBalancerResolver$Definition
   extends AbstractInitializableBeanDefinition<DefaultLoadBalancerResolver>
   implements BeanFactory<DefaultLoadBalancerResolver> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultLoadBalancerResolver.class,
      "<init>",
      new Argument[]{
         Argument.of(BeanContext.class, "beanContext"),
         Argument.of(List.class, "serviceInstanceLists", null, Argument.ofTypeVariable(ServiceInstanceList.class, "E"))
      },
      new DefaultAnnotationMetadata(
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         Collections.EMPTY_MAP,
         AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
         Collections.EMPTY_MAP,
         false,
         true
      ),
      false
   );

   @Override
   public DefaultLoadBalancerResolver build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultLoadBalancerResolver var4 = new DefaultLoadBalancerResolver(
         var2,
         (List<ServiceInstanceList>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 1, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[1].getTypeParameters()[0], null
         )
      );
      return (DefaultLoadBalancerResolver)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultLoadBalancerResolver var4 = (DefaultLoadBalancerResolver)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultLoadBalancerResolver$Definition() {
      this(DefaultLoadBalancerResolver.class, $CONSTRUCTOR);
   }

   protected $DefaultLoadBalancerResolver$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultLoadBalancerResolver$Definition$Reference.$ANNOTATION_METADATA,
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
