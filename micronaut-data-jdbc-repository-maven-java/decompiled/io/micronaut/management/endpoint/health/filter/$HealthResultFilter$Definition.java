package io.micronaut.management.endpoint.health.filter;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $HealthResultFilter$Definition extends AbstractInitializableBeanDefinition<HealthResultFilter> implements BeanFactory<HealthResultFilter> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      HealthResultFilter.class, "<init>", new Argument[]{Argument.of(HealthEndpoint.class, "healthEndpoint")}, null, false
   );

   @Override
   public HealthResultFilter build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      HealthResultFilter var4 = new HealthResultFilter((HealthEndpoint)super.getBeanForConstructorArgument(var1, var2, 0, null));
      return (HealthResultFilter)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      HealthResultFilter var4 = (HealthResultFilter)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $HealthResultFilter$Definition() {
      this(HealthResultFilter.class, $CONSTRUCTOR);
   }

   protected $HealthResultFilter$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $HealthResultFilter$Definition$Reference.$ANNOTATION_METADATA,
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
