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
class $IOExecutorServiceConfig$Definition extends AbstractInitializableBeanDefinition<IOExecutorServiceConfig> implements BeanFactory<IOExecutorServiceConfig> {
   private static final AbstractInitializableBeanDefinition.MethodOrFieldReference $CONSTRUCTOR = new AbstractInitializableBeanDefinition.MethodReference(
      IOExecutorServiceConfig.class, "<init>", null, null, false
   );

   @Override
   public IOExecutorServiceConfig build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
      IOExecutorServiceConfig var4 = new IOExecutorServiceConfig();
      return (IOExecutorServiceConfig)this.injectBean(var1, var2, var4);
   }

   @Override
   protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
      IOExecutorServiceConfig var4 = (IOExecutorServiceConfig)var3;
      return super.injectBean(var1, var2, var3);
   }

   public $IOExecutorServiceConfig$Definition() {
      this(IOExecutorServiceConfig.class, $CONSTRUCTOR);
   }

   protected $IOExecutorServiceConfig$Definition(Class var1, AbstractInitializableBeanDefinition.MethodOrFieldReference var2) {
      super(
         var1,
         var2,
         $IOExecutorServiceConfig$Definition$Reference.$ANNOTATION_METADATA,
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
