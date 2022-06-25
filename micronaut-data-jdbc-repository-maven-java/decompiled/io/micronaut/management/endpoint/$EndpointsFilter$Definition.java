package io.micronaut.management.endpoint;

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
class $EndpointsFilter$Definition extends AbstractInitializableBeanDefinition<EndpointsFilter> implements BeanFactory<EndpointsFilter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      EndpointsFilter.class, "<init>", new Argument[]{Argument.of(EndpointSensitivityProcessor.class, "endpointSensitivityProcessor")}, null, false
   );

   @Override
   public EndpointsFilter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      EndpointsFilter var4 = new EndpointsFilter((EndpointSensitivityProcessor)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (EndpointsFilter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      EndpointsFilter var4 = (EndpointsFilter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $EndpointsFilter$Definition() {
      this(EndpointsFilter.class, $CONSTRUCTOR);
   }

   protected $EndpointsFilter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $EndpointsFilter$Definition$Reference.$ANNOTATION_METADATA,
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
