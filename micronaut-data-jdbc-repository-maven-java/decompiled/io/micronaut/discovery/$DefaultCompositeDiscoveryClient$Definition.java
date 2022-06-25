package io.micronaut.discovery;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $DefaultCompositeDiscoveryClient$Definition
   extends AbstractInitializableBeanDefinition<DefaultCompositeDiscoveryClient>
   implements BeanFactory<DefaultCompositeDiscoveryClient> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      DefaultCompositeDiscoveryClient.class,
      "<init>",
      new Argument[]{Argument.of(List.class, "discoveryClients", null, Argument.ofTypeVariable(DiscoveryClient.class, "E"))},
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
   public DefaultCompositeDiscoveryClient build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      DefaultCompositeDiscoveryClient var4 = new DefaultCompositeDiscoveryClient(
         (List<DiscoveryClient>)super.getBeansOfTypeForConstructorArgument(
            var1, var2, 0, ((AbstractInitializableBeanDefinition.MethodReference)$CONSTRUCTOR).arguments[0].getTypeParameters()[0], null
         )
      );
      return (DefaultCompositeDiscoveryClient)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      DefaultCompositeDiscoveryClient var4 = (DefaultCompositeDiscoveryClient)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $DefaultCompositeDiscoveryClient$Definition() {
      this(DefaultCompositeDiscoveryClient.class, $CONSTRUCTOR);
   }

   protected $DefaultCompositeDiscoveryClient$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $DefaultCompositeDiscoveryClient$Definition$Reference.$ANNOTATION_METADATA,
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
