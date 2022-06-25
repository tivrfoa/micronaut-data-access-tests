package io.micronaut.scheduling.executor;

import io.micronaut.context.AbstractInitializableBeanDefinition;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.core.annotation.Generated;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanFactory;
import java.util.Optional;

// $FF: synthetic class
@Generated
class $ScheduledExecutorServiceConfig$Definition
   extends AbstractInitializableBeanDefinition<ScheduledExecutorServiceConfig>
   implements BeanFactory<ScheduledExecutorServiceConfig> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      ScheduledExecutorServiceConfig.class, "<init>", null, null, false
   );

   @Override
   public ScheduledExecutorServiceConfig build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      ScheduledExecutorServiceConfig var4 = new ScheduledExecutorServiceConfig();
      return (ScheduledExecutorServiceConfig)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      ScheduledExecutorServiceConfig var4 = (ScheduledExecutorServiceConfig)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $ScheduledExecutorServiceConfig$Definition() {
      this(ScheduledExecutorServiceConfig.class, $CONSTRUCTOR);
   }

   protected $ScheduledExecutorServiceConfig$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $ScheduledExecutorServiceConfig$Definition$Reference.$ANNOTATION_METADATA,
         null,
         null,
         null,
         null,
         null,
         Optional.empty(),
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
